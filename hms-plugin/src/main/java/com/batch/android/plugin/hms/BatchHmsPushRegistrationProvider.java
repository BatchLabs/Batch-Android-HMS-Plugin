package com.batch.android.plugin.hms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.batch.android.AdsIdentifierProvider;
import com.batch.android.BatchPushService;
import com.batch.android.PushRegistrationProvider;
import com.batch.android.PushRegistrationProviderAvailabilityException;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.push.HmsMessaging;

import java.util.List;

public class BatchHmsPushRegistrationProvider implements PushRegistrationProvider {

    private static final String MANIFEST_APP_ID_KEY = "batch_push_hms_app_id_override";

    private Context context;
    private AdsIdentifierProvider adsIdentifierProvider;
    private String appID;

    private static String cachedToken = null;

    static void setToken(String token) {
        cachedToken = token;
    }

    BatchHmsPushRegistrationProvider(Context context)
    {
        this.context = context;
        this.appID = fetchSenderID();
        this.adsIdentifierProvider = new BatchHmsAdsIdentifierProvider(context);
    }

    public String fetchSenderID()
    {
        try {
            final Bundle metaData = context
                    .getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;

            int valueResource = metaData.getInt(MANIFEST_APP_ID_KEY, -1);
            if (valueResource != -1) {
                String manifestSenderID = context.getString(valueResource);
                if (!TextUtils.isEmpty(manifestSenderID)) {
                    Log.i(BatchHms.TAG, "Push - Using HMS App ID from manifest");
                    return manifestSenderID;
                }
            }

        } catch (Exception ignored) {

        }

        try {
            AGConnectServicesConfig agConnectServicesConfig = AGConnectServicesConfig.fromContext(context);
            if (agConnectServicesConfig == null) {
                Log.e(BatchHms.TAG,
                        "Push - Could not register for HMS Push: Could not get a AGConnect instance. Is your AGConnect/HMSPush project configured?");
                return null;
            }

            String appID = agConnectServicesConfig.getString("client/app_id");
            if (TextUtils.isEmpty(appID)) {
                Log.e(BatchHms.TAG,
                        "Push - Could not register for HMS Push: Could not get a Sender ID for this project. Are notifications well configured in the project's console and your agconnect-services.json up to date?");
                return null;
            }

            return appID;
        } catch (NoClassDefFoundError | Exception e) {
            Log.e(BatchHms.TAG,
                    "Push - Could not register for HMS Push: AGConnect has thrown an exception",
                    e);
        }

        return null;
    }

    @Override
    public String getSenderID()
    {
        return appID;
    }

    @Override
    public String getShortname()
    {
        return "HMS";
    }

    @Override
    public void checkServiceAvailability() throws PushRegistrationProviderAvailabilityException {

        int pushAvailability = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context);

        if (pushAvailability != ConnectionResult.SUCCESS) {
            throw new PushRegistrationProviderAvailabilityException(
                    "Unable to use HMSPush because the Huawei Mobile Service are not available or not up-to-date on this device. ("
                            + HuaweiApiAvailability.getInstance().getErrorString(pushAvailability)
                            + ")");
        }
    }

    @Override
    public void checkLibraryAvailability() throws PushRegistrationProviderAvailabilityException {

        if (!isHMSPushPresent()) {
            throw new PushRegistrationProviderAvailabilityException(
                    "AGConnect/HMSPush is missing. Did you add 'com.huawei.hms:push' to your gradle dependencies?");
        }

        if (!isBatchPushServiceAvailable()) {
            throw new PushRegistrationProviderAvailabilityException(
                    "com.batch.android.BatchPushService is missing from the manifest.");
        }
    }

    @Override
    public AdsIdentifierProvider getAdsIdentifierProvider() {
        return adsIdentifierProvider;
    }

    @Nullable
    @SuppressLint("MissingFirebaseInstanceTokenRefresh")
    public String getRegistration()
    {
        if (!TextUtils.isEmpty(cachedToken)) {
            return cachedToken;
        }

        try {
            if (appID == null) {
                return null;
            }

            HmsInstanceId hmsIID = HmsInstanceId.getInstance(context);
            if (hmsIID == null) {
                Log.e(BatchHms.TAG,
                        "Push - Could not register for HMS Push: Could not get the HmsInstanceId. Is your HMS project configured and initialized?");
                return null;
            }

            return hmsIID.getToken(appID, HmsMessaging.DEFAULT_TOKEN_SCOPE);
        } catch (Exception e) {
            Log.e(BatchHms.TAG, "Push - Could not register for HMS Push.", e);
        }
        return null;
    }

    private boolean isHMSPushPresent()
    {
        try {
            Class.forName("com.huawei.agconnect.config.AGConnectServicesConfig");
            Class.forName("com.huawei.hms.aaid.HmsInstanceId");
            Class.forName("com.huawei.hms.push.HmsMessaging");
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public boolean isBatchPushServiceAvailable()
    {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final Intent intent = new Intent(context, BatchPushService.class);
            List<ResolveInfo> resolveInfo = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);

            return resolveInfo.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

}

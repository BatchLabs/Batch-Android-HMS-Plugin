package com.batch.android.plugin.hms;

import android.content.Context;

import com.batch.android.AdsIdentifierProvider;
import com.batch.android.AdsIdentifierProviderAvailabilityException;
import com.huawei.hms.ads.identifier.AdvertisingIdClient;

import java.io.IOException;

public class BatchHmsAdsIdentifierProvider implements AdsIdentifierProvider {

    private Context context;

    public BatchHmsAdsIdentifierProvider(Context context) {
        this.context = context;
    }

    @Override
    public void checkAvailability() throws AdsIdentifierProviderAvailabilityException {
        if (!isHMSAdvertisingIdClientPresent()) {
            throw new AdsIdentifierProviderAvailabilityException("HMS Ads Identifier is missing. Did you add 'com.huawei.hms:ads-identifier' to your gradle dependencies?");
        }
    }

    public boolean isHMSAdvertisingIdClientPresent()
    {
        try {
            Class.forName("com.huawei.hms.ads.identifier.AdvertisingIdClient");
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public void getAdsIdentifier(AdsIdentifierListener adsIdentifierListener) {

        if (adsIdentifierListener == null) {
            throw new NullPointerException("Null listener");
        }

        final Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
                if (info != null) {
                    adsIdentifierListener.onSuccess(info.getId(), info.isLimitAdTrackingEnabled());
                } else {
                    adsIdentifierListener.onError(new Exception("OAID is null"));
                }
            } catch (IOException e) {
                adsIdentifierListener.onError(e);
            }
        }).start();
    }

}

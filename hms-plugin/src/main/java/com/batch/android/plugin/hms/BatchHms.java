package com.batch.android.plugin.hms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchNotificationInterceptor;
import com.huawei.hms.push.RemoteMessage;

import java.util.Map;

public final class BatchHms {

    public static final String TAG = "BatchHms";

    public final static class Push {

        private Push()
        {
        }


        /**
         * Key used in a push bundle
         */
        public static final String BATCH_BUNDLE_KEY = "com.batch";

        /**
         * Check if the received push is a Batch one. If you have a custom push implementation into your app you should
         * call this method before doing anything else.
         * If it returns true, you should not handle the push.
         *
         * @param remoteMessage Huawei RemoteMessage
         * @return true if the push is for Batch and you shouldn't handle it, false otherwise
         */
        public static boolean isBatchPush(RemoteMessage remoteMessage)
        {
            if (remoteMessage == null) {
                return false;
            }

            Map<String, String> data = remoteMessage.getDataOfMap();
            if (data == null || data.size() == 0) {
                return false;
            }

            return data.get(BATCH_BUNDLE_KEY) != null;
        }

        /**
         * Append Batch data to your open intent so that opens from this push will be tracked by Batch and displayed into your dashboard.
         * It also powers other features, such as but not limited to mobile landings.
         *
         * @param remoteMessage the Huawei message content
         * @param openIntent    the intent of the notification the will be triggered when the user clicks on it
         */
        public static void appendBatchData(@NonNull RemoteMessage remoteMessage,
                                           @NonNull Intent openIntent)
        {
            Bundle extras = PushHelper.huaweiMessageToReceiverBundle(remoteMessage);
            if (extras == null) {
                Log.e(TAG, "Could not read data from Huawei message");
                return;
            }

            Batch.Push.appendBatchData(extras, openIntent);
        }

        /**
         * Make a PendingIntent suitable for notifications from a given Intent.
         * This is useful for custom receivers, or {@link BatchNotificationInterceptor} implementations.
         * <p>
         * Warning: it will override the intent's action with a unique name, to ensure that existing notifications are not updated with this PendingIntent's content.
         * If you rely on a custom action, you will have to make your own PendingIntent.
         *
         * @param context       Context. Cannot be null.
         * @param intent        The intent you want to be triggered when performing the pending intent. Must be an intent compatible with {@link PendingIntent#getActivity(Context, int, Intent, int)}. Cannot be null.
         * @param remoteMessage Raw Huawei message, used to copy data used by Batch to power features such as direct opens, or mobile landings. Cannot be null.
         *                      If these extras don't have valid Batch data in it, a valid PendingIntent will still be returned, but some features might not work correctly.
         * @return A PendingIntent instance, wrapping the given Intent.
         */
        @NonNull
        public static PendingIntent makePendingIntent(@NonNull Context context,
                                                      @NonNull Intent intent,
                                                      @NonNull RemoteMessage remoteMessage)
        {
            Bundle extras = PushHelper.huaweiMessageToReceiverBundle(remoteMessage);
            if (extras == null) {
                extras = new Bundle();
            }

            return Batch.Push.makePendingIntent(context, intent, extras);
        }

        /**
         * Make a PendingIntent suitable for notifications from a given deeplink. It will use Batch's builtin action activity.
         * <p>
         * This is useful for custom receivers, or {@link BatchNotificationInterceptor} implementations.
         *
         * @param context       Context. Cannot be null.
         * @param deeplink      Deeplink string. Cannot be null.
         * @param remoteMessage Raw Huawei message content, used to copy data used by Batch to power features such as direct opens, or mobile landings. Cannot be null.
         *                      If these extras don't have valid Batch data in it, a valid PendingIntent will still be returned, but some features might not work correctly.
         * @return A PendingIntent set to open Batch's builtin action activity to open the specified deeplink. Can be null if the deeplink is not valid.
         */
        @Nullable
        public static PendingIntent makePendingIntentForDeeplink(@NonNull Context context,
                                                                 @NonNull String deeplink,
                                                                 @NonNull RemoteMessage remoteMessage)
        {
            Bundle extras = PushHelper.huaweiMessageToReceiverBundle(remoteMessage);
            if (extras == null) {
                extras = new Bundle();
            }

            return Batch.Push.makePendingIntentForDeeplink(context, deeplink, extras);
        }

        /**
         * Should the developer handle and display this push, or will Batch display it?
         * Use this method to know if Batch will ignore this push, and that displaying it is your responsibility
         *
         * @param context Context
         * @param remoteMessage The Huawei message
         * @return true if the push will not be processed by Batch and should be handled, false otherwise
         */
        public static boolean shouldDisplayPush(Context context,
                                                RemoteMessage remoteMessage)
        {
            return Batch.Push.isManualDisplayModeActivated() && isBatchPush(remoteMessage);
        }

        /**
         * Call this method to display the notification for this message.
         *
         * Note that this method will spawn a new thread if ran on the main thread, and thus
         * becomes asynchronous.
         * @param context Context
         * @param remoteMessage The Huawei message
         */
        public static void displayNotification(Context context,
                                               RemoteMessage remoteMessage)
        {
            displayNotification(context, remoteMessage, null);
        }

        /**
         * Call this method to display the notification for this message.
         * Allows an interceptor to be set for this call, overriding the global one set using {@link Batch.Push#setNotificationInterceptor(BatchNotificationInterceptor)}
         *
         * Note that this method will spawn a new thread if ran on the main thread, and thus
         * becomes asynchronous.
         *
         * @param context Context
         * @param remoteMessage The Huawei message
         * @param interceptor Interceptor to use
         */
        public static void displayNotification(@NonNull Context context,
                                               @NonNull RemoteMessage remoteMessage,
                                               @Nullable BatchNotificationInterceptor interceptor)
        {
            Bundle extras = PushHelper.huaweiMessageToReceiverBundle(remoteMessage);
            if (extras == null) {
                extras = new Bundle();
            }

            Intent intent = new Intent();
            intent.replaceExtras(extras);

            final Runnable r = () -> Batch.Push.displayNotification(context, intent, interceptor, true);
            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                // We're on the main thread, spawn a new one so that image downloads work
                new Thread(r).start();
            } else {
                r.run();
            }
        }

        /**
         * Call this method when you just displayed a Batch push notification by yourself.
         *
         * @param context Context
         * @param remoteMessage The Huawei message
         */
        public static void onNotificationDisplayed(Context context,
                                                   RemoteMessage remoteMessage)
        {
            Bundle extras = PushHelper.huaweiMessageToReceiverBundle(remoteMessage);
            if (extras == null) {
                extras = new Bundle();
            }

            Intent intent = new Intent();
            intent.replaceExtras(extras);

            Batch.Push.onNotificationDisplayed(context, intent);
        }
    }

    /**
     * Call this method in onMessageReceived() if you're implementing a custom HmsMessageService
     *
     * @param context Context
     * @param remoteMessage The Huawei message
     */
    public static void onMessageReceived(Context context, RemoteMessage remoteMessage) {
        BatchHms.Push.displayNotification(context, remoteMessage);
    }

    /**
     * Call this method in onNewToken() if you're implementing a custom HmsMessageService
     *
     * @param context Context
     * @param token New push token
     */
    public static void onNewToken(Context context, String token) {
        BatchHmsPushRegistrationProvider.setToken(token);

        // Not tested
        Batch.onServiceCreate(context, false);
        Batch.Push.refreshRegistration();
        Batch.onServiceDestroy(context);
    }
}

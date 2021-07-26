package com.batch.android.plugin.hms;

import android.os.Bundle;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class BatchHmsMessageService extends HmsMessageService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        BatchHms.onMessageReceived(this, remoteMessage);
    }

    @Override
    public void onNewToken(String s, Bundle bundle) {
        super.onNewToken(s, bundle);
        BatchHms.onNewToken(this, s);
    }
}

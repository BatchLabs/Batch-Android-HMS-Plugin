package com.batch.android.plugin.hms;

import android.content.Context;

import com.batch.android.PushRegistrationProvider;
import com.batch.android.push.PushRegistrationRegistrar;

public class BatchHmsPushRegistrationRegistrar implements PushRegistrationRegistrar {

    @Override
    public PushRegistrationProvider getPushRegistrationProvider(Context context) {
        return new BatchHmsPushRegistrationProvider(context);
    }
}

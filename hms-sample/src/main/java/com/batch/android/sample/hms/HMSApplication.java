package com.batch.android.sample.hms;

import android.app.Application;

import com.batch.android.Batch;
import com.batch.android.BatchActivityLifecycleHelper;
import com.batch.android.Config;

public class HMSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Batch.setConfig(new Config(BuildConfig.API_KEY));
        registerActivityLifecycleCallbacks(new BatchActivityLifecycleHelper());
    }
}

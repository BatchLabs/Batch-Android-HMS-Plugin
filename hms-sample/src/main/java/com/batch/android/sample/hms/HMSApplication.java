package com.batch.android.sample.hms;

import android.app.Application;

import com.batch.android.Batch;
import com.batch.android.BatchActivityLifecycleHelper;

public class HMSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Batch.start(BuildConfig.API_KEY);
        registerActivityLifecycleCallbacks(new BatchActivityLifecycleHelper());
    }
}

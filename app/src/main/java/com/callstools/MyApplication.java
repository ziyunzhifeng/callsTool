package com.callstools;

import android.app.Application;

/**
 * Created by ubuntu on 17-3-2.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppEnv.setContext(this);
    }
}

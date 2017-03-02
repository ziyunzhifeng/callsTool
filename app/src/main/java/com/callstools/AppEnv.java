package com.callstools;

import android.content.Context;

/**
 * Created by ubuntu on 17-3-1.
 */

public class AppEnv {
    private static Context mContext;
    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
    }
}

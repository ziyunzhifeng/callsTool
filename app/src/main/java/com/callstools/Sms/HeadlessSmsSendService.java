package com.callstools.Sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by zhaohongwei on 16-11-10.
 */
public class HeadlessSmsSendService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

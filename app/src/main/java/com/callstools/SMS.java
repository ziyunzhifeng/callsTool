package com.callstools;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ubuntu on 16-11-22.
 */
public class SMS extends Activity {
    private final static String TAG = SMS.class.getSimpleName();

    private static TextView sms = null;
    private int mCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sms);
        sms = (TextView)findViewById(R.id.sms);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        mCount = Integer.valueOf(bd.getString("count"));
        SmsAsyncTask  smstask = new SmsAsyncTask();
        smstask.execute();
    }

    public class SmsAsyncTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sms.setText("start");
        }

        @Override
        protected String doInBackground(Integer... params) {
            long phoneNum = 15901500000L;
            String address = String.valueOf(phoneNum);
            Calendar calendar = Calendar.getInstance();

            long date = Long.valueOf(calendar.getTimeInMillis());
            int type = 1;
            String body = "test " + address;
            Uri sms = Uri.parse("content://sms");
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();
            Log.i(TAG, "start insert sms");
            try {
                for (int i = 0; i < mCount; i++ ) {
                    setSMS(sms, address, date, type, body, ops);
                    if ((ops.size() >= 100) && i < mCount) {
//                        Log.i(TAG, "start insert sms applyBatch");
                        ret = SMS.this.getContentResolver().applyBatch("sms", ops);
                        if (ret == null) {
                            Log.i("zhw", "start backup sms applyBatch fail");
                            break;
                        }
                        ops.clear();
                    } else if (i == (mCount - 1)) {
                        Log.i(TAG, "start backup sms applyBatch end");
                        ret = SMS.this.getContentResolver().applyBatch("sms", ops);
                        if (ret == null) {
                            Log.i(TAG, "start backup sms applyBatch end fail");
                            break;
                        }
                        ops.clear();
                    }
                    phoneNum++;
                    date = Long.valueOf(calendar.getTimeInMillis());
                    address = String.valueOf(phoneNum);
                    curentProgress = i * 100/mCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCount);
                Log.i(TAG, "end insert sms");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            sms.setText(values[0].toString());
//            super.onProgressUpdate(values);
        }
    }

    private void setSMS(Uri sms, String address, long date, int type, String body, ArrayList<ContentProviderOperation> ops) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(sms);
        builder.withValue(Telephony.Sms.ADDRESS, address);
        builder.withValue(Telephony.Sms.DATE, date);
        builder.withValue(Telephony.Sms.TYPE, type);
        builder.withValue(Telephony.Sms.BODY, body);
        ops.add(builder.build());
    }

}

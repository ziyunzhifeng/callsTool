package com.callstools;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ubuntu on 16-11-22.
 */
public class Calls extends Activity {
    private final static String TAG = Calls.class.getSimpleName();
    private TextView count = null;
    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_call);
        count = (TextView) findViewById(R.id.count);
        count.setText("start");
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        mCount = Integer.valueOf(bd.getString("count"));
        CallsAsyncTask callsAsyncTask = new CallsAsyncTask();
        callsAsyncTask.execute();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public class CallsAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            count.setText("start");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();
            Calendar calendar = Calendar.getInstance();
            long phoneNum = 15901500000L;
            String number = String.valueOf(phoneNum);
            long duration = 1L;
            long date = Long.valueOf(calendar.getTimeInMillis());
            int type = 2;
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            Log.i(TAG, "start insert calls");
            try {
                for (int i = 0; i < mCount; i++) {

                    setCalls(number, duration, date, type, ops);
                    if ((ops.size() >= 400) && i < mCount) {
                        Log.i(TAG, "start insert calls applyBatch");
                        ret = Calls.this.getContentResolver().applyBatch(CallLog.AUTHORITY, ops);
                        if (ret == null) {
                            Log.i("zhw", "start backup calls applyBatch fail");
                            break;
                        }
                        ops.clear();
                    } else if (i == (mCount - 1)) {
                        Log.i(TAG, "start backup calls applyBatch end");
                        ret = Calls.this.getContentResolver().applyBatch(CallLog.AUTHORITY, ops);
                        if (ret == null) {
                            Log.i(TAG, "start backup calls applyBatch end fail");
                            break;
                        }
                        ops.clear();
                    }

                    phoneNum++;
                    duration++;
                    number = String.valueOf(phoneNum);
                    date = Long.valueOf(calendar.getTimeInMillis());
                    curentProgress = i * 100/mCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCount);
                Log.i(TAG, "end insert calls");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            count.setText(values[0].toString());
        }
    }

    private void setCalls(String number, long duration, long date, int type, ArrayList<ContentProviderOperation> ops) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(CallLog.Calls.CONTENT_URI);
        builder.withValue(CallLog.Calls.NUMBER, number);
        builder.withValue(CallLog.Calls.DURATION, duration);
        builder.withValue(CallLog.Calls.DATE, date);
        builder.withValue(CallLog.Calls.TYPE, type);
        ops.add(builder.build());
    }

}

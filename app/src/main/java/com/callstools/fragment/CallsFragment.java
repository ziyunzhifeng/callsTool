package com.callstools.fragment;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callstools.AppEnv;
import com.callstools.Calls;
import com.callstools.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ubuntu on 17-3-2.
 */

public class CallsFragment extends Fragment {

    private final static String TAG = CallsFragment.class.getSimpleName();

    private EditText mEditTextBaseNum;
    private EditText mEditTextLong;
    private EditText mEditTextType;
    private EditText mEditTextCreate;
    private Button mButtonCreate;
    private ProgressBar mProgressBar;

    private Long mBaseNum = 15901010000L;
    private Long mCallLong = 10L;
    private int mType = 0;
    private int mCreateCount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calls_frg, container, false);

        mEditTextBaseNum = (EditText) view.findViewById(R.id.call_baseNum_et);
        mEditTextType = (EditText) view.findViewById(R.id.call_type_et);
        mEditTextLong = (EditText) view.findViewById(R.id.call_long_et);
        mEditTextCreate = (EditText) view.findViewById(R.id.call_create_et);
        mButtonCreate = (Button) view.findViewById(R.id.call_create_bt);
        mProgressBar = (ProgressBar) view.findViewById(R.id.call_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String baseNum = mEditTextBaseNum.getText().toString();
                if (null != baseNum) mBaseNum = Long.valueOf(baseNum);

                String callLong = mEditTextLong.getText().toString();
                if (null != callLong) mCallLong = Long.valueOf(callLong);

                String type = mEditTextType.getText().toString();
                if (null != type) mType = Integer.valueOf(type);

                mCreateCount = Integer.valueOf(mEditTextCreate.getText().toString());

                if (mCreateCount > 0) {
                    CallsAsyncTask callsAsyncTask = new CallsAsyncTask();
                    callsAsyncTask.execute();
                }

            }
        });

        return view;
    }

    public class CallsAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(Integer... params) {
            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();
            Calendar calendar = Calendar.getInstance();
            long phoneNum = mBaseNum;
            String number = String.valueOf(phoneNum);
            long duration = mCallLong;
            long date = Long.valueOf(calendar.getTimeInMillis());
            int type = mType;
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            Log.i(TAG, "start insert calls");
            try {
                for (int i = 0; i < mCreateCount; i++) {

                    setCalls(number, duration, date, type, ops);
                    if ((ops.size() >= 400) && i < mCreateCount) {
                        Log.i(TAG, "start insert calls applyBatch");
                        ret = AppEnv.getContext().getContentResolver().applyBatch(CallLog.AUTHORITY, ops);
                        if (ret == null) {
                            Log.i("zhw", "start backup calls applyBatch fail");
                            break;
                        }
                        ops.clear();
                    } else if (i == (mCreateCount - 1)) {
                        Log.i(TAG, "start backup calls applyBatch end");
                        ret = AppEnv.getContext().getContentResolver().applyBatch(CallLog.AUTHORITY, ops);
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
                    curentProgress = i * 100/mCreateCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCreateCount);
                Log.i(TAG, "end insert calls");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0].intValue());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(AppEnv.getContext(), "通话记录已经创建完成", Toast.LENGTH_LONG);
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

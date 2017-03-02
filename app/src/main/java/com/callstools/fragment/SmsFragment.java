package com.callstools.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callstools.AppEnv;
import com.callstools.R;
import com.callstools.SMS;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ubuntu on 17-3-1.
 */

public class SmsFragment extends Fragment {

    private final static String TAG = SmsFragment.class.getSimpleName();

    private EditText mEditTextBaseNum;
    private EditText mEditTextContent;
    private EditText mEditTextType;
    private EditText mEditTextCreate;
    private Button mButtonCreate;
    private ProgressBar mProgressBar;


    private Long mBaseNum = 15901010000L;
    private String mContent = "Hello word!";
    private int mType = 0;
    private int mCreateCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sms_frg, container, false);
        mEditTextBaseNum = (EditText) view.findViewById(R.id.sms_baseNum_et);
        mEditTextContent = (EditText) view.findViewById(R.id.sms_content_et);
        mEditTextType = (EditText) view.findViewById(R.id.sms_type_et);
        mEditTextCreate = (EditText) view.findViewById(R.id.sms_create_et);
        mButtonCreate = (Button) view.findViewById(R.id.sms_create_bt);
        mProgressBar = (ProgressBar) view.findViewById(R.id.sms_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Long baseNum = Long.valueOf(mEditTextBaseNum.getText().toString());
                if (baseNum != 0) mBaseNum = baseNum;

                String content = mEditTextContent.getText().toString();
                if (content != null) mContent = content;

                String type = mEditTextType.getText().toString();
                if (type != null) mType = Integer.valueOf(type);

                mCreateCount = Integer.valueOf(mEditTextCreate.getText().toString());

                if (mCreateCount > 0) {
                    SmsAsyncTask smsAsyncTask = new SmsAsyncTask();
                    smsAsyncTask.execute();
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setDefaultSMSApp();
        }
    }

    public class SmsAsyncTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(Integer... params) {
            long phoneNum = mBaseNum;
            String address = String.valueOf(phoneNum);
            Calendar calendar = Calendar.getInstance();

            long date = Long.valueOf(calendar.getTimeInMillis());
            int type = 1;
            String body =  mContent;
            Uri sms = Uri.parse("content://sms");
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();
            Log.i(TAG, "start insert sms");
            try {
                for (int i = 0; i < mCreateCount; i++ ) {
                    setSMS(sms, address, date, type, body, ops);
                    if ((ops.size() >= 100) && i < mCreateCount) {
//                        Log.i(TAG, "start insert sms applyBatch");
                        ret = AppEnv.getContext().getContentResolver().applyBatch("sms", ops);
                        if (ret == null) {
                            Log.i("zhw", "start backup sms applyBatch fail");
                            break;
                        }
                        ops.clear();
                    } else if (i == (mCreateCount - 1)) {
                        Log.i(TAG, "start backup sms applyBatch end");
                        ret = AppEnv.getContext().getContentResolver().applyBatch("sms", ops);
                        if (ret == null) {
                            Log.i(TAG, "start backup sms applyBatch end fail");
                            break;
                        }
                        ops.clear();
                    }
                    phoneNum++;
                    date = Long.valueOf(calendar.getTimeInMillis());
                    address = String.valueOf(phoneNum);
                    curentProgress = i * 100/mCreateCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCreateCount);
                Log.i(TAG, "end insert sms");
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
            Toast.makeText(AppEnv.getContext(), "短信已经创建完成", Toast.LENGTH_LONG);
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


    public void setDefaultSMSApp() {
        String defaultSmsApp = null;

        String currentPn = AppEnv.getContext().getPackageName();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(AppEnv.getContext());

            if (!TextUtils.isEmpty(defaultSmsApp) && !defaultSmsApp.equals(currentPn)) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, currentPn);
                this.startActivity(intent);
            }
        }
    }

}

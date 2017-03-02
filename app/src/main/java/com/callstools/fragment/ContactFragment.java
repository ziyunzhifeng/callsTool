package com.callstools.fragment;


import android.support.v4.app.Fragment;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.callstools.AppEnv;
import com.callstools.Contacts;
import com.callstools.R;

import java.util.ArrayList;

/**
 * Created by ubuntu on 17-3-1.
 */

public class ContactFragment extends Fragment {

    private final String TAG = ContactFragment.class.getSimpleName();

    private EditText mEditTextBaseName;
    private EditText mEditTextBaseNum;
    private EditText mEditTextAddress;
    private EditText mEditTextEmail;
    private EditText mEditTextCreate;
    private Button mButtonCreate;
    private ProgressBar mProgressBar;

    private String mBaseName = "Aaron";
    private Long mBaseNum = 159001010000L;
    private String mAddress = "北京市朝阳区";
    private String mEmail = "Aaron@gmail.com";
    private int mCreateCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.contact_frg, container, false);

        mEditTextBaseName = (EditText) view.findViewById(R.id.contact_baseName_et);
        mEditTextBaseNum = (EditText) view.findViewById(R.id.contact_baseNum_et);
        mEditTextAddress = (EditText) view.findViewById(R.id.contact_address_et);
        mEditTextEmail = (EditText) view.findViewById(R.id.contact_email_et);
        mEditTextCreate = (EditText) view.findViewById(R.id.contact_create_et);
        mButtonCreate = (Button) view.findViewById(R.id.contact_create_bt);
        mProgressBar = (ProgressBar) view.findViewById(R.id.contact_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String baseName = mEditTextBaseName.getText().toString();
                if (baseName != null) mBaseName = baseName;

                Long baseNum = Long.valueOf(mEditTextBaseNum.getText().toString());
                if (baseNum != null) mBaseNum = baseNum;

                String address = mEditTextAddress.getText().toString();
                if (address != null) mAddress = address;

                String email = mEditTextEmail.getText().toString();
                if (email != null) mEmail = email;

                mCreateCount = Integer.valueOf(mEditTextCreate.getText().toString());

                if (mCreateCount > 0) {
                    ContactsAsyncTask contactsAsyncTask = new ContactsAsyncTask();
                    contactsAsyncTask.execute();
                }
            }
        });

        return view;
    }


    public class ContactsAsyncTask extends AsyncTask<Integer, Integer, String> {

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
            long phoneNum = mBaseNum;
            String number = String.valueOf(phoneNum);
            String name = mBaseName;
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            Log.i(TAG, "start insert calls");
            try {
                for (int i = 0; i < mCreateCount; i++) {
                    setContacts(number, name, ops);
                    phoneNum++;
                    name = mBaseName + String.valueOf(i + 1);
                    number = String.valueOf(phoneNum);
                    curentProgress = i * 100/mCreateCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCreateCount);
                Log.i(TAG, "end insert contacts");
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
            mProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(AppEnv.getContext(), "联系人已经创建完成", Toast.LENGTH_LONG);
            super.onPostExecute(s);
        }
    }

    private void setContacts(String number, String name, ArrayList<ContentProviderOperation> ops) {
        ContentProviderResult[] ret = null;

        try {
            int rawContactInsertIndex = 0;
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //文档位置：reference\android\provider\ContactsContract.Data.html
            ops.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                    .build());
            ops.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "")
                    .build());
            ops.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, mEmail)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
            ops.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.SipAddress.DATA, mAddress)
                    .withValue(ContactsContract.CommonDataKinds.SipAddress.TYPE, ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK)
                    .build());
            ret = AppEnv.getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (ret == null) {
                Log.i("zhw", "start backup contacts applyBatch fail");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ops.clear();
    }

}

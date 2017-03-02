package com.callstools;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ubuntu on 16-11-27.
 */
public class Contacts extends Activity {
    private final static String TAG = Calls.class.getSimpleName();
    private TextView count = null;
    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_counts);
        count = (TextView) findViewById(R.id.contact_tv);
        count.setText("start");
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        mCount = Integer.valueOf(bd.getString("count"));
        ContactsAsyncTask contactsAsyncTask = new ContactsAsyncTask();
        contactsAsyncTask.execute();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public class ContactsAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            count.setText("start");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();
            long phoneNum = 15901500000L;
            String number = String.valueOf(phoneNum);
            String name = "Aaron";
            int curentProgress = 0;
            int lastProgress = -1;
            ContentProviderResult[] ret = null;

            Log.i(TAG, "start insert calls");
            try {
                for (int i = 0; i < mCount; i++) {

                    setContacts(number, name, ops);
//                    if ((ops.size() >= 400) && i < mCount) {
//                        Log.i(TAG, "start insert contacts applyBatch");
//                        ret = Contacts.this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//                        if (ret == null) {
//                            Log.i("zhw", "start backup contacts applyBatch fail");
//                            break;
//                        }
//                        ops.clear();
//                    } else if (i == (mCount - 1)) {
//                        Log.i(TAG, "start backup contacts applyBatch end");
//                        ret = Contacts.this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//                        if (ret == null) {
//                            Log.i(TAG, "start backup contacts applyBatch end fail");
//                            break;
//                        }
//                        ops.clear();
//                    }

                    phoneNum++;
                    name = "Aaron" + String.valueOf(i + 1);
                    number = String.valueOf(phoneNum);
                    curentProgress = i * 100/mCount;
                    if (curentProgress != lastProgress) {
                        publishProgress(i);
                        lastProgress = curentProgress;
                    }
                }
                publishProgress(mCount);
                Log.i(TAG, "end insert contacts");
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

    private void setContacts(String number, String name, ArrayList<ContentProviderOperation> ops) {
        ContentProviderResult[] ret = null;

        try {
            int rawContactInsertIndex = 0;
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

//        ContentValues values = new ContentValues();
//        Uri rawContactUri = this.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
//        rawContactInsertIndex = ContentUris.parseId(rawContactUri);

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
//        ops.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.Email.DATA, "lisi@126.cn")
//                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
//                .build());
            ret = Contacts.this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (ret == null) {
                Log.i("zhw", "start backup contacts applyBatch fail");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ops.clear();
    }

    private void getContacts() {

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);//查询通讯录
        if(cursor.getCount()>0){
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));//联系人id
                Log.i("zhw", "get contacts id : " + id);
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));//联系人名称
                Log.i("zhw", "get contacts name " + name);
                if(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))>0){
                    //Query phone here.  Covered next 在该处查询电话号码
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        // Do something with phones
                    }
                    pCur.close();
                }
            }
        }

    }

}

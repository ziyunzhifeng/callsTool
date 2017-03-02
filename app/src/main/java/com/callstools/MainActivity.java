package com.callstools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;

import android.os.Bundle;

import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.callstools.fragment.CallsFragment;
import com.callstools.fragment.ContactFragment;
import com.callstools.fragment.SmsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private Button mCallBt = null;
    private Button mSmsBt = null;
    private Button mContactBt = null;
    private MyViewPager mViewPager = null;
    private TextView mCurrentTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mViewPager = (MyViewPager) findViewById(R.id.mainView);
        mCurrentTitle = (TextView) findViewById(R.id.currentTitle);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ContactFragment());
        fragmentList.add(new SmsFragment());
        fragmentList.add(new CallsFragment());

        mViewPager.setAdapter(new MyFragmentStateAdaper(getSupportFragmentManager(), fragmentList));
        mViewPager.addOnPageChangeListener(new myOnPageChangeListener());

        mCallBt = (Button) findViewById(R.id.calls_bt);
        mSmsBt = (Button) findViewById(R.id.sms_bt);
        mContactBt = (Button) findViewById(R.id.contact_bt);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calls_bt: {
                Log.i("zhw", "onClick sms_bt");
                mViewPager.setCurrentItem(2);
                mCurrentTitle.setText("创建通话记录");
            }   break;
            case R.id.sms_bt: {
                Log.i("zhw", "onClick sms_bt");
                mViewPager.setCurrentItem(1);
                mCurrentTitle.setText("创建短信");
            }   break;
            case R.id.contact_bt: {
                Log.i("zhw", "onClick contact_bt");
                mViewPager.setCurrentItem(0);
                mCurrentTitle.setText("创建联系人");
            }   break;
            default: break;
        }
    }

    private class myOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            if(position == 0) {
                mCurrentTitle.setText("创建联系人");
            } else if (position == 1) {
                mCurrentTitle.setText("创建短信");
            } else if (position == 2) {
                mCurrentTitle.setText("创建通话记录");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}

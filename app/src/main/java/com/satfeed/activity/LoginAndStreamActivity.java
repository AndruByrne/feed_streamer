package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.widget.PopupWindow;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;

import javax.inject.Inject;

public class LoginAndStreamActivity extends Activity {

    @Inject
    PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.login_and_stream_activity);
//        viewDataBinding..setParkList(observableArrayList);
        ((FeedStreamerApplication) getApplication()).getServiceComponent().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

}
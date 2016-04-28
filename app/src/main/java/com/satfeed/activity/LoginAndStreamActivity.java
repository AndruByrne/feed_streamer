package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

import com.satfeed.BR;
import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;

import java.util.TreeMap;

import javax.inject.Inject;

final public class LoginAndStreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        final ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.login_and_stream_activity);
        // bootstrap into dagger graph with getServiceComponent
        DataBindingUtil.setDefaultComponent(((FeedStreamerApplication) getApplication()).getServiceComponent());
        // set default email in binding (default UI populated by resource ID; hackish!)
        viewDataBinding.setVariable(BR.hailing_email, getResources().getString(R.string.default_email));
        // assign touch handlers
        viewDataBinding.setVariable(BR.touch_handlers, new TouchHandlers());
    }
}
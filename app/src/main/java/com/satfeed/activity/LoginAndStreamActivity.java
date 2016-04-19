package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;

import com.satfeed.BR;
import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.modules.ServiceComponent;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;

public class LoginAndStreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        final ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.login_and_stream_activity);
        // bootstrap into dagger graph
//        serviceComponent.inject(this);
        DataBindingUtil.setDefaultComponent(((FeedStreamerApplication) getApplication()).getServiceComponent());
        // set default email in binding (default UI populated by resource ID)
        viewDataBinding.setVariable(BR.hailing_email, getResources().getString(R.string.default_email));
        // assign touch handlers
        viewDataBinding.setVariable(BR.touch_handlers, new TouchHandlers());
    }
}
package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;

import com.satfeed.BR;
import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.databinding.LoginAndStreamActivityBinding;
import com.satfeed.modules.DownloadAndPlayAdapterComponent;
import com.satfeed.modules.SansUserAdapterComponent;
import com.satfeed.modules.UserComponent;

final public class LoginAndStreamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        SansUserAdapterComponent sansUserAdapterComponent = ((FeedStreamerApplication) getApplication()).createSansUserAdapterComponent();
        // bootstrap into dagger graph
        final ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(
                this,
                R.layout.login_and_stream_activity,
                sansUserAdapterComponent);
        // set default email in binding (default UI populated by resource ID; hackish!)
        viewDataBinding.setVariable(BR.hailing_email, getResources().getString(R.string.default_email));
        // assign touch handlers
        viewDataBinding.setVariable(BR.touch_handlers, sansUserAdapterComponent.getTouchHandlers());
    }
}
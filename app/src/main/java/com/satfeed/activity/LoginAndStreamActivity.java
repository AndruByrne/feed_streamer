package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.BR;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;

import javax.inject.Inject;
import javax.inject.Named;

public class LoginAndStreamActivity extends Activity implements IVLCVout.Callback, LibVLC.OnNativeCrashListener {

//    @Inject PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate and Bind
        final ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.login_and_stream_activity);
        // bootstrap into dagger graph
        ((FeedStreamerApplication) getApplication()).getServiceComponent().inject(this);
        // set default email in binding (default UI populated by resource ID)
        viewDataBinding.setVariable(BR.hailing_email, getResources().getString(R.string.default_email));
        // assign touch handlers
        viewDataBinding.setVariable(BR.touch_handlers, new TouchHandlers());
    }

    @Override
    protected void onResume() {
        super.onResume();
        final LibVLC libVLC = new LibVLC();
    }

    @Override
    public void onBackPressed() {
//        if (popupWindow.isShowing()) {
//            popupWindow.dismiss();
//        } else {
            super.onBackPressed();
//        }
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {

    }

    @Override
    public void onNativeCrash() {

    }
}
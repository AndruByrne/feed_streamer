package com.satfeed.activity;

import android.app.Activity;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.test.mock.MockApplication;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.satfeed.BR;
import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.databinding.LoginAndStreamActivityBinding;

import javax.inject.Inject;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
public class TouchHandlers {

//    @Inject
//    ObservableField<String> hailingEmail;

    public TouchHandlers() { }

    public void onGoClickedWithEmail(View view){
//        final String hailingEmail = ((FeedStreamerApplication) ((Activity) view.getContext()).getApplication())
//                .getServiceComponent().getHailingEmail().get();
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding(view);
        final String hailingEmail = dataBinding.getHailingEmail();
        System.out.println("email: "+hailingEmail);
        view.getContext();
    }

    public void onGoClickedNoEmail(View view){
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }
}

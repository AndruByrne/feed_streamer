package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.Toast;

import com.satfeed.R;
import com.satfeed.databinding.LoginAndStreamActivityBinding;
import com.satfeed.databinding.StreamingAudioBinding;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
public class TouchHandlers {

//    @Inject
//    ObservableField<String> hailingEmail;

    public TouchHandlers() { }

    public void onGoClickedWithEmail(View view){
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding(view);
        final String hailingEmail = dataBinding.getHailingEmail();
        final StreamingAudioBinding audioBinding = StreamingAudioBinding.inflate(
                ((Activity) view.getContext()).getLayoutInflater(),
                dataBinding.videoFrame,
                false);
        audioBinding.setVariable(BindingVariables.hailing_email, hailingEmail);
        dataBinding.videoFrame.addView(audioBinding.seekingBar);
    }

    public void onGoClickedNoEmail(View view){
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }
}

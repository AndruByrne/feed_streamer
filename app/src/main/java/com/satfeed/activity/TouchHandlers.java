package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.databinding.AudioProgressBinding;
import com.satfeed.databinding.LoginAndStreamActivityBinding;
import com.satfeed.databinding.StreamingProgressAndPlayBinding;
import com.satfeed.modules.ServiceComponent;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class TouchHandlers {

    public TouchHandlers() {
    }

    static public void onGoClickedWithEmail(View view) {
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding(view);
        final StreamingProgressAndPlayBinding streamingBinding = StreamingProgressAndPlayBinding.inflate(
                ((Activity) view.getContext()).getLayoutInflater(),
                dataBinding.audioFrame,
                false);
        streamingBinding.setTouchHandlers(dataBinding.getTouchHandlers());
        streamingBinding.setHailingEmail(dataBinding.getHailingEmail());
        dataBinding.audioFrame.removeAllViews();
        dataBinding.audioFrame.addView(streamingBinding.playButton);
    }

    static public void onGoClickedNoEmail(View view) {
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }

    static public void onPlayClicked(View view) {
        Activity context = ((Activity) view.getContext());
        ServiceComponent serviceComponent = ((FeedStreamerApplication) context.getApplication()).getServiceComponent();
        serviceComponent.getStreamingProgressAdapter().closeStream();
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding((View) view.getParent());
        final AudioProgressBinding audioProgressBinding = AudioProgressBinding.inflate(
                context.getLayoutInflater(),
                dataBinding.audioFrame,
                false);
        audioProgressBinding.setTouchHandlers(dataBinding.getTouchHandlers());
        dataBinding.audioFrame.removeAllViews();
        dataBinding.audioFrame.addView(audioProgressBinding.audioProgress);
    }

    static public void onProgressBarClicked(View view) {
        final AudioProgressBinding dataBinding = DataBindingUtil.findBinding(view);
        ServiceComponent serviceComponent = ((FeedStreamerApplication)((Activity)view.getContext()).getApplication()).getServiceComponent();
        serviceComponent.getAudioAdapter().playAudio((ProgressBar)view, true);

    }
}

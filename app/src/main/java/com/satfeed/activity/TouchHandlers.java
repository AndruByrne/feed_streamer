package com.satfeed.activity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.R;
import com.satfeed.databinding.AudioProgressBinding;
import com.satfeed.databinding.DownloadProgressAndPlayBinding;
import com.satfeed.databinding.LoginAndStreamActivityBinding;
import com.satfeed.modules.DownloadAndPlayAdapterComponent;
import com.satfeed.modules.UserComponent;

import javax.inject.Inject;

import dagger.Lazy;

/*
 * Created by Andrew Brin on 4/6/2016.
 */
final public class TouchHandlers {

    private UserComponent userComponent;
    private DownloadAndPlayAdapterComponent downloadAndPlayAdapterComponent;

    public TouchHandlers() {
    }

    public void onGoClickedWithEmail(View view) {
        final FeedStreamerApplication application = FeedStreamerApplication.getInstance();
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding(view);
        userComponent = application.createUserComponent(dataBinding.getHailingEmail()); //  create modules dependent on user email
        downloadAndPlayAdapterComponent = application.createDownloadAndPlayAdapterComponent(userComponent);
        final DownloadProgressAndPlayBinding downloadProgressAndPlayBinding = DownloadProgressAndPlayBinding.inflate(
                ((Activity) view.getContext()).getLayoutInflater(), //  inflate download_progress_and_play.xml
                downloadAndPlayAdapterComponent);
        downloadProgressAndPlayBinding.setTouchHandlers(this); //  transfer touch handlers
        dataBinding.audioFrame.removeAllViews();
        dataBinding.audioFrame.addView(downloadProgressAndPlayBinding.playButton); //  add play_button view
    }

    static public void onGoClickedNoEmail(View view) {
        Toast.makeText(view.getContext(), R.string.no_email_toast, Toast.LENGTH_SHORT).show();
    }

    public void onPlayClicked(View view) {
//        downloadAndPlayAdapterComponent.getDownloadProgressAdapter().closeStream();
        final LoginAndStreamActivityBinding dataBinding = DataBindingUtil.findBinding((View) view.getParent());
        final AudioProgressBinding audioProgressBinding = AudioProgressBinding.inflate( //  inflate audio_progress.xml
                ((Activity) view.getContext()).getLayoutInflater(),
                downloadAndPlayAdapterComponent);
        audioProgressBinding.setTouchHandlers(this); //  transfer handlers
        dataBinding.audioFrame.removeAllViews();
        dataBinding.audioFrame.addView(audioProgressBinding.audioProgress); //  add audio_progress view
    }

    public void onProgressBarClicked(View view) {
        downloadAndPlayAdapterComponent.getAudioAdapter().playAudio(downloadAndPlayAdapterComponent, (ProgressBar) view, true); //  just replay audio
    }
}

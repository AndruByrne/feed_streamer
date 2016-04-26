package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/18/2016.
 */
public class StreamingAudioPopulator {

    private final StreamingClient streamingClient;
    private AudioTrack audioTrack;

    public StreamingAudioPopulator(StreamingClient streamingClient, AudioTrack audioTrack) {
        this.streamingClient = streamingClient;
        this.audioTrack = audioTrack;
    }

    @BindingAdapter("login_and_stream")
    public void loginAndStream(@NonNull final SeekBar view, final String hailing_email){
        streamingClient
                .streamToTrack(hailing_email, audioTrack)
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String>() {
            @Override
            public void call(String success) {
                if(audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) audioTrack.play();
                Toast.makeText(view.getContext(), "Stream: " + success, Toast.LENGTH_SHORT).show();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(view.getContext(), "Having trouble streaming video right now", Toast.LENGTH_LONG).show();
                Log.e(FeedStreamerApplication.TAG, throwable.getMessage());
                throwable.printStackTrace();
            }
        }, new Action0() {
            @Override
            public void call() {
                // probably need to do clean up here
                Log.i(FeedStreamerApplication.TAG, ": stream has ended");
            }
        });
    }
}

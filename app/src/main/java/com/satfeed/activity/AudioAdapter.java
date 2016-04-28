package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.services.AudioPlayer;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/27/2016.
 */
final public class AudioAdapter {

    private AudioPlayer audioPlayer;
    private Subscription subscription;

    public AudioAdapter(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @BindingAdapter("play_audio")
    public void playAudio(final ProgressBar view, Boolean shouldPlay) {
        unsubscribe();
        subscription = audioPlayer
                .playAudio()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                view.setProgress(integer); //  move progres bar towards 100
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(FeedStreamerApplication.TAG, "Error in audio playback: "+throwable.getMessage());
                                unsubscribe();
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                unsubscribe();
                            }
                        });
    }

    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}

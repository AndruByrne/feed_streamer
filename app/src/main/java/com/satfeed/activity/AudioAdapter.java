package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingComponent;
import android.util.Log;
import android.widget.ProgressBar;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.modules.DownloadAndPlayAdapterComponent;
import com.satfeed.services.AudioPlayer;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/27/2016.
 */
final public class AudioAdapter {

    public AudioAdapter() {
    }

    @BindingAdapter("play_audio")
    public static void playAudio(DownloadAndPlayAdapterComponent adapterComponent, final ProgressBar view, Boolean shouldPlay) {
        Observable<Integer> audioObservable = adapterComponent
                .getAudioPlayer().playAudio();
        Subscriber<Integer> audioSubscriber = new Subscriber<Integer>() {

            @Override
            public void onNext(Integer integer) {
                view.setProgress(integer); //  move progres bar towards 100

            }

            @Override
            public void onError(Throwable e) {
                Log.e(FeedStreamerApplication.TAG, "Error in audio playback: " + e.getMessage());

            }

            @Override
            public void onCompleted() {

            }


        };
        adapterComponent.getSubscriptionAccountant().subscribeAudio(audioObservable, audioSubscriber);
    }

}

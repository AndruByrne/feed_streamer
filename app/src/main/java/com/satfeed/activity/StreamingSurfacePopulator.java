package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/18/2016.
 */
public class StreamingSurfacePopulator {

    private final StreamingClient streamingClient;

    public StreamingSurfacePopulator(StreamingClient streamingClient) { this.streamingClient = streamingClient; }

    @BindingAdapter("login_and_stream")
    public void loginAndStream(final SurfaceView view, final String hailing_email){
//        getting error from network on UI when I stream to surface from here, which is odd, given
//        these binding adapters are advertised as off-thread, using an observable.

        Log.d(FeedStreamerApplication.TAG, "surface view is not null? : "+Boolean.toString(view!=null));
        Log.d(FeedStreamerApplication.TAG, "streaming client is not null? : "+Boolean.toString(streamingClient!=null));
        streamingClient
                .streamToSurface(view, hailing_email)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String success) {
                        Toast.makeText(view.getContext(), "Stream: " + success, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(FeedStreamerApplication.TAG, ": error in observable: " + throwable.getMessage());
                        Toast.makeText(view.getContext(), "Having trouble streaming video right now", Toast.LENGTH_LONG).show();
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

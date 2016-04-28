package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.services.StreamingClient;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/18/2016.
 */
final public class StreamingProgressAdapter {

    public static final int TANKS = 500;
    private final StreamingClient streamingClient;
    private Subscription subscription;

    public StreamingProgressAdapter(StreamingClient streamingClient) {
        this.streamingClient = streamingClient;
    }

    @BindingAdapter("login_and_stream")
    public void loginAndStream(@NonNull final Button view, final String hailing_email) {
        closeStream();
        view.setAlpha((float) 24 / TANKS);
        subscription = streamingClient
                .streamToTree(hailing_email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer numberOfGoodPackets) {
//                        Toast.makeText(view.getContext(), "received " + numberOfGoodPackets + " packets", Toast.LENGTH_SHORT).show();
                        view.setAlpha(Math.min((float) numberOfGoodPackets / TANKS, 1));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(view.getContext(), "stream interrupted", Toast.LENGTH_LONG).show();
                        Log.e(FeedStreamerApplication.TAG, "error in obs: " + throwable.getMessage());
                        throwable.printStackTrace();
                        closeStream();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        // /should/ not be called
                        Log.i(FeedStreamerApplication.TAG, ": stream has ended");
                        closeStream();
                    }
                });
    }

    public void closeStream() {
        Log.i(FeedStreamerApplication.TAG, "closing stream");
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }
}

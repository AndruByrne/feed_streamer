package com.satfeed.activity;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingComponent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.modules.DownloadAndPlayAdapterComponent;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/*
 * Created by Andrew Brin on 4/18/2016.
 */
final public class DownloadProgressAdapter {

    public static final int TANKS = 500;
    private Observable<Integer> downloadClientObservable;

    public DownloadProgressAdapter() { }

    @BindingAdapter("login_and_download")
    public static void loginAndStream(DownloadAndPlayAdapterComponent adapterComponent, @NonNull final Button view, final Integer startingAlpha) {
        view.setAlpha((float) 24 / TANKS);
        Observable<Integer> downloadObservable = adapterComponent.getDownloadClient();
        Subscriber<Integer> downloadSubscriber = new Subscriber<Integer>() {

            @Override
            public void onNext(Integer numberOfGoodPackets) {
                view.setAlpha(Math.min((float) numberOfGoodPackets / TANKS, 1));
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(view.getContext(), "stream interrupted", Toast.LENGTH_LONG).show();
                Log.e(FeedStreamerApplication.TAG, "error in obs: " + e.getMessage());
                e.printStackTrace();

            }

            @Override
            public void onCompleted() {
                Log.i(FeedStreamerApplication.TAG, ": stream has ended");

            }

        };
        adapterComponent.getSubscriptionAccountant().subscribeDownload(downloadObservable, downloadSubscriber);
    }
}

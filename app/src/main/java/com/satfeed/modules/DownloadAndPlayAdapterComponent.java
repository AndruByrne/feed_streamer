package com.satfeed.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import android.databinding.DataBindingComponent;

import com.satfeed.activity.AudioAdapter;
import com.satfeed.activity.DownloadProgressAdapter;
import com.satfeed.services.AudioPlayer;
import com.satfeed.services.SubscriptionAccountant;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Component;
import rx.Observable;

@DownloadAndPlayScope
@Component(
        dependencies = UserComponent.class,
        modules = {
                DownloadProgressAdapterModule.class,
                AudioAdapterModule.class,
                EditTextModule.class
        })

public interface DownloadAndPlayAdapterComponent extends DataBindingComponent {

    DownloadProgressAdapter getDownloadProgressAdapter();

    AudioAdapter getAudioAdapter();

    @Named("clientDownloadingToTree")
    Observable<Integer> getDownloadClient();

    AudioPlayer getAudioPlayer();

    SubscriptionAccountant getSubscriptionAccountant();

}

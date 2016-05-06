package com.satfeed.modules;

/*
 * Created by Andrew Brin on 5/3/2016.
 */

import com.satfeed.services.AudioPlayer;
import com.satfeed.services.SubscriptionAccountant;

import javax.inject.Named;

import dagger.Component;
import rx.Observable;

@UserScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                PacketMapModule.class,
                DownloadClientModule.class,
                AudioPlayerModule.class})
public interface UserComponent {

    //  expose methods for downstream adapters
    @Named("clientDownloadingToTree")
    Observable<Integer> getDownloadClient();

    AudioPlayer getAudioPlayer();

    SubscriptionAccountant getSubscriptionAccountant();

}

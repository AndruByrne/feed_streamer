package com.satfeed.modules;

import android.databinding.DataBindingComponent;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        EditTextModule.class,
        PacketMapModule.class,
        StreamingAudioModule.class,
        SocketDownloadModule.class,
        ThreadingModule.class})
public interface ServiceComponent extends DataBindingComponent { }

package com.satfeed.modules;

import android.databinding.DataBindingComponent;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import java.net.InetSocketAddress;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        EditTextModule.class,
        LibVLCModule.class,
        SocketAddressModule.class,
        StreamingSurfaceModule.class,
        ThreadingModule.class})
public interface ServiceComponent extends DataBindingComponent {

    InetSocketAddress getServerAddress();

    MediaPlayer getMediaPlayer();

    IVLCVout getVOut();

    LibVLC getVLC();
}

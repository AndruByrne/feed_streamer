package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import android.app.Application;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LibVLCModule {

    static final ArrayList<String> options = new ArrayList<>();

    static {
        options.add("--aout=opensles");
        options.add("-vvv"); // verbosity
    }

    @Provides
    @Singleton
    public static LibVLC getVLC() {
        return new LibVLC(options);
    }

    @Provides
    @Singleton
    public static MediaPlayer getMediaPlayer(LibVLC libVLC) {
        return new MediaPlayer(libVLC);
    }

    @Provides
    @Singleton
    public static MediaPlayer.EventListener getPlayerListener(final PlayerStopper playerStopper) {
        return new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.EndReached:
                        Log.d(FeedStreamerApplication.TAG, "reached end of stream");
                        Boolean stopSuccess = playerStopper.releasePlayer();
                        Log.d(FeedStreamerApplication.TAG, "Shutdown cleanly? " + Boolean.toString(stopSuccess));
                }
            }
        };
    }

    @Provides
    @Singleton
    public static IVLCVout.Callback getVLCVOutListener() {
        return new IVLCVout.Callback() {
            @Override
            public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

            }

            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {
                // no op
            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {
                // no op
            }

            @Override
            public void onHardwareAccelerationError(IVLCVout vlcVout) {
                Log.d(FeedStreamerApplication.TAG, "Hardware Acceleration Error");
//                Boolean stopSuccess = playerStopper.releasePlayer();
            }
        };
    }

    @Provides
    @Singleton
    public static IVLCVout getVOut(IVLCVout.Callback vOutCallback, MediaPlayer mediaPlayer, MediaPlayer.EventListener eventListener, Application application){
        mediaPlayer.setEventListener(eventListener);
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();
        vlcVout.addCallback(vOutCallback);
        return vlcVout;
    }

    @Provides
    public static PlayerStopper getPlayerStopper(MediaPlayer mediaPlayer, LibVLC libVLC, MediaPlayer.EventListener eventListener) {
        return new PlayerStopper(mediaPlayer, libVLC, eventListener);
    }

    private static class PlayerStopper {
        private final MediaPlayer mediaPlayer;
        private final LibVLC libVLC;
        private final MediaPlayer.EventListener eventListener;

        public PlayerStopper(MediaPlayer mediaPlayer, LibVLC libVLC, MediaPlayer.EventListener eventListener) {
            this.mediaPlayer = mediaPlayer;
            this.libVLC = libVLC;
            this.eventListener = eventListener;
        }

        public boolean releasePlayer() {
            if (libVLC == null) return false;
            mediaPlayer.stop();
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.removeCallback((IVLCVout.Callback) eventListener);
            vout.detachViews();
            libVLC.release();
            //set heights?
            return true;
        }
    }
}
package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.satfeed.FeedStreamerApplication;
import com.satfeed.activity.AudioAdapter;
import com.satfeed.activity.StreamingProgressAdapter;
import com.satfeed.services.AudioPlayer;
import com.satfeed.services.StreamingBufferHandler;
import com.satfeed.services.StreamingClient;

import java.util.TreeMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamingAudioModule {

    @Provides
    @Singleton
    AudioAdapter getAudioAdapter(AudioPlayer audioPlayer) {
        return new AudioAdapter(audioPlayer);
    }

    @Provides
    @Singleton
    AudioPlayer getAudioPlayer(AudioTrack audioTrack, TreeMap<Integer, byte[]> packetTreeMap) {
        return new AudioPlayer(audioTrack, packetTreeMap);
    }

    @Provides
    @Singleton
    AudioTrack getAudioTrack(Application application) {
        final AudioManager systemService = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
        final String sampleRate = systemService.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
//        final String nominalBufferSize = systemService.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);

        Log.d(FeedStreamerApplication.TAG, "sample rate: " + sampleRate);
        return new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(Integer.parseInt(sampleRate)/6)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(4000)
                .build();
    }
}

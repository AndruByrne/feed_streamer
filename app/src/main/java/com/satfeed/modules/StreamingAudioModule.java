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
import com.satfeed.activity.StreamingAudioPopulator;
import com.satfeed.activity.StreamingClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamingAudioModule {


    @Provides
    @Singleton
    StreamingAudioPopulator getSurfacePopulator(StreamingClient streamingClient, AudioTrack audioTrack) {
        return new StreamingAudioPopulator(streamingClient, audioTrack);
    }

    @Provides
    @Singleton
    StreamingClient getStreamingClient(Application application) {
        return new StreamingClient(application);
    }

    @Provides
    @Singleton
    AudioTrack getAudioPlayer(Application application) {
        final AudioManager systemService = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
        final String sampleRate = systemService.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        final String nominalBufferSize = systemService.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);

        Log.d(FeedStreamerApplication.TAG, "ideal sample rate: "+sampleRate);
        Log.d(FeedStreamerApplication.TAG, "nominal buffer size: "+nominalBufferSize);
        return new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(Integer.parseInt(sampleRate))
                        .setChannelMask(AudioFormat.CHANNEL_IN_STEREO).build())
                .setBufferSizeInBytes(5000)
                .build();

    }
}

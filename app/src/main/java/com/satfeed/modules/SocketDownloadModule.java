package com.satfeed.modules;

import android.app.Application;

import com.satfeed.activity.StreamingProgressAdapter;
import com.satfeed.services.StreamingBufferHandler;
import com.satfeed.services.StreamingClient;

import java.util.TreeMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/*
 * Created by Andrew Brin on 4/27/2016.
 */

@Module
public class SocketDownloadModule {

    @Provides
    @Singleton
    StreamingProgressAdapter getProgressAdapter(StreamingClient streamingClient) {
        return new StreamingProgressAdapter(streamingClient);
    }

    @Provides
    @Singleton
    StreamingClient getStreamingClient(
            Application application,
            TreeMap<Integer, byte[]> packetTreeMap,
            StreamingBufferHandler streamingBufferHandler) {
        return new StreamingClient(application, packetTreeMap, streamingBufferHandler);
    }

    @Provides
    @Singleton
    StreamingBufferHandler getStreamingBufferHandler() {
        return new StreamingBufferHandler();
    }
}

package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import com.satfeed.activity.StreamingClient;
import com.satfeed.activity.StreamingSurfacePopulator;

import java.net.InetSocketAddress;

import dagger.Module;
import dagger.Provides;

@Module
public class StreamingSurfaceModule {

    @Provides
    StreamingSurfacePopulator getSurfacePopulator() {
        return new StreamingSurfacePopulator(new StreamingClient());
    }
}

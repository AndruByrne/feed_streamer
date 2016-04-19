package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import java.net.InetSocketAddress;

import dagger.Module;
import dagger.Provides;

@Module
public class SocketAddressModule {

    private final String alienServer;
    private final String streamingPort;

    public SocketAddressModule(String alienServer, String streamingPort) {
        this.alienServer = alienServer;
        this.streamingPort = streamingPort;
    }

    @Provides
    InetSocketAddress getServerAddress() {
        return null;
//        return new InetSocketAddress(
//                alienServer,
//                Integer.parseInt(streamingPort));
    }
}

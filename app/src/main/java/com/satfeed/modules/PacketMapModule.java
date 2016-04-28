package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/26/2016.
 */

import java.util.TreeMap;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PacketMapModule {

    @Provides
    @Singleton
    TreeMap<Integer,byte[]> getPacketTreeMap(){
        return new TreeMap<>();
    }
}

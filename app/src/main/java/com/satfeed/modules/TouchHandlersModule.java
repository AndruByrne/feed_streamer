package com.satfeed.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import com.satfeed.activity.TouchHandlers;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TouchHandlersModule {

    @Provides
    @Singleton
    TouchHandlers getTouchHandlers(){
        return new TouchHandlers();
    }
}

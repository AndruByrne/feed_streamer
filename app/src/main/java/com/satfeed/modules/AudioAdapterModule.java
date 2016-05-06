package com.satfeed.modules;

/*
 * Created by Andrew Brin on 5/5/2016.
 */

import com.satfeed.activity.AudioAdapter;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;

@Module
public class AudioAdapterModule {

    @Inject AudioAdapter audioAdapter;

    @Provides
    @DownloadAndPlayScope
    AudioAdapter getAudioAdapter(){
        return audioAdapter;
    }

}

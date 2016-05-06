package com.satfeed.modules;

import com.satfeed.activity.DownloadProgressAdapter;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

/*
 * Created by Andrew Brin on 5/5/2016.
 */

@Module
public class DownloadProgressAdapterModule {

    @Inject DownloadProgressAdapter downloadProgressAdapter;

    @Provides
    @DownloadAndPlayScope
    DownloadProgressAdapter getProgressAdapter() {
        return downloadProgressAdapter;
    }
}

package com.satfeed.modules;

import android.app.Application;

import com.satfeed.activity.DownloadProgressAdapter;
import com.satfeed.services.DownloadBufferHandler;
import com.satfeed.services.DownloadClient;

import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

/*
 * Created by Andrew Brin on 4/27/2016.
 */

@Module
public class DownloadClientModule {

//    @Inject DownloadProgressAdapter downloadProgressAdapter;
    private String hailingEmail;

    public DownloadClientModule(String hailingEmail) { this.hailingEmail = hailingEmail; }

    @Provides
    @UserScope
    @Named("clientDownloadingToTree")
    Observable<Integer> getDownloadClient(
            Application application,
            TreeMap<Integer, byte[]> packetTreeMap,
            DownloadBufferHandler downloadBufferHandler) {
        return new DownloadClient(application, packetTreeMap, downloadBufferHandler, hailingEmail).streamToTree();
    }

    @Provides
    @UserScope
    DownloadBufferHandler getStreamingBufferHandler() {
        return new DownloadBufferHandler();
    }
}

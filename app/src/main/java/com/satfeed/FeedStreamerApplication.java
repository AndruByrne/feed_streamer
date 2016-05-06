package com.satfeed;

import android.app.Application;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.ApplicationComponent;
import com.satfeed.modules.DaggerApplicationComponent;
import com.satfeed.modules.DaggerDownloadAndPlayAdapterComponent;
import com.satfeed.modules.DaggerSansUserAdapterComponent;
import com.satfeed.modules.DaggerUserComponent;
import com.satfeed.modules.DownloadAndPlayAdapterComponent;
import com.satfeed.modules.SansUserAdapterComponent;
import com.satfeed.modules.DownloadClientModule;
import com.satfeed.modules.AudioPlayerModule;
import com.satfeed.modules.ThreadingModule;
import com.satfeed.modules.UserComponent;


public class FeedStreamerApplication extends Application {
    public static final String STREAMING_PORT = "2323";
    public static String ALIEN_SERVER = "challenge.airtime.com";
    private ApplicationComponent applicationComponent;
    public static String TAG = FeedStreamerApplication.class.getSimpleName();

    private static FeedStreamerApplication instance;
    private UserComponent userComponent = null;
    private DownloadAndPlayAdapterComponent downloadAndPlayAdapterComponent;
    private SansUserAdapterComponent sansUserAdapterComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applicationComponent = DaggerApplicationComponent.builder()
                .appModule(new AppModule(this))
                .threadingModule(getThreadingModule())
                .build();
    }

    public UserComponent createUserComponent(String hailingEmail) {
        userComponent = DaggerUserComponent
                .builder()
                .applicationComponent(applicationComponent)
                .downloadClientModule(new DownloadClientModule(hailingEmail))
                .audioPlayerModule(new AudioPlayerModule())
                .build();
        return userComponent;
    }

    public SansUserAdapterComponent createSansUserAdapterComponent() {
        sansUserAdapterComponent = DaggerSansUserAdapterComponent
                .builder()
                .applicationComponent(applicationComponent)
                .build();
        return sansUserAdapterComponent;
    }

    public DownloadAndPlayAdapterComponent createDownloadAndPlayAdapterComponent(UserComponent userComponent) {
        downloadAndPlayAdapterComponent = DaggerDownloadAndPlayAdapterComponent
                .builder()
                .userComponent(userComponent)
                .build();
        return downloadAndPlayAdapterComponent;
    }

    public static FeedStreamerApplication getInstance() {
        return instance;
    }

    public ThreadingModule getThreadingModule() {
        return new ThreadingModule();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public void releaseUserComponent() {
        userComponent = null;
    }

    public void releaseDownloadAndPlayAdapterComponent() { downloadAndPlayAdapterComponent = null; }

    public void releaseSansUserAdapterComponent() { sansUserAdapterComponent = null; }
}

package com.satfeed;

import android.app.Application;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.DaggerServiceComponent;
import com.satfeed.modules.EditTextModule;
import com.satfeed.modules.PacketMapModule;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.StreamingAudioModule;
import com.satfeed.modules.SocketDownloadModule;
import com.satfeed.modules.ThreadingModule;


public class FeedStreamerApplication extends Application {
    public static final String STREAMING_PORT = "2323";
    public static String ALIEN_SERVER = "challenge.airtime.com";
    private ServiceComponent serviceComponent;
    public static String TAG = FeedStreamerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        serviceComponent = DaggerServiceComponent.builder()
                .appModule(new AppModule(this))
                .editTextModule(new EditTextModule())
                .packetMapModule(new PacketMapModule())
                .streamingAudioModule(new StreamingAudioModule())
                .socketDownloadModule(new SocketDownloadModule())
                .threadingModule(getThreadingModule())
                .build();
    }

    public ThreadingModule getThreadingModule(){ return new ThreadingModule(); }

    public ServiceComponent getServiceComponent(){
        return serviceComponent;
    }
}

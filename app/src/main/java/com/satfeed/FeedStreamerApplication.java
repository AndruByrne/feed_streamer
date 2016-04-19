package com.satfeed;

import android.app.Application;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.DaggerServiceComponent;
import com.satfeed.modules.EditTextModule;
import com.satfeed.modules.LibVLCModule;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.SocketAddressModule;
import com.satfeed.modules.StreamingSurfaceModule;
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
                .libVLCModule(new LibVLCModule())
                .socketAddressModule(getSocketAddressModule())
                .streamingSurfaceModule(new StreamingSurfaceModule())
                .threadingModule(getThreadingModule())
                .build();
    }

    public SocketAddressModule getSocketAddressModule() {
        return new SocketAddressModule(ALIEN_SERVER, STREAMING_PORT);
    }

    public ThreadingModule getThreadingModule(){ return new ThreadingModule(); }

    public ServiceComponent getServiceComponent(){
        return serviceComponent;
    }
}

package com.satfeed;

import android.app.Application;
import android.support.annotation.NonNull;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.DaggerServiceComponent;
import com.satfeed.modules.EditTextModule;
import com.satfeed.modules.LoginAndStreamModule;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.SocketAddressModule;
import com.satfeed.modules.StreamingSurfaceModule;
import com.satfeed.modules.ThreadingModule;

import java.net.SocketAddress;


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
                .streamingSurfaceModule(new StreamingSurfaceModule())
                .socketAddressModule(getSocketAddressModule())
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

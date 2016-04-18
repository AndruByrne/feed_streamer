package com.satfeed;

import android.app.Application;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.DaggerServiceComponent;
import com.satfeed.modules.LoginAndStreamModule;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.ThreadingModule;


public class FeedStreamerApplication extends Application {
    public static String ALIEN_SERVER = "https://data.sfgov.org/resource/";
    private ServiceComponent serviceComponent;
    public static String TAG = FeedStreamerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        serviceComponent = DaggerServiceComponent.builder()
                .appModule(new AppModule(this))
                .loginAndStreamModule(getLoginAndStreamModule(ALIEN_SERVER))
                .threadingModule(getThreadingModule())
                .build();
    }

    public LoginAndStreamModule getLoginAndStreamModule(String sfCityApiBaseUrl) {
        return new LoginAndStreamModule(sfCityApiBaseUrl);
    }

    public ThreadingModule getThreadingModule(){ return new ThreadingModule(); }

    public ServiceComponent getServiceComponent(){
        return serviceComponent;
    }
}

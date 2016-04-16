package com.satfeed;

import android.app.Application;

import com.satfeed.modules.AppModule;
import com.satfeed.modules.DaggerServiceComponent;
import com.satfeed.modules.LoginAndStreamModule;
import com.satfeed.modules.PopupModule;
import com.satfeed.modules.ServiceComponent;
import com.satfeed.modules.ThreadingModule;


public class FeedStreamerApplication extends Application {
    public static String SF_CITY_API_BASE_URL = "https://data.sfgov.org/resource/";
    private ServiceComponent serviceComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceComponent = DaggerServiceComponent.builder()
                .appModule(new AppModule(this))
                .loginAndStreamModule(getLoginAndStreamModule(SF_CITY_API_BASE_URL))
                .popupModule(new PopupModule())
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

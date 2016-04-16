package com.satfeed.modules;

import android.app.Application;
import android.widget.PopupWindow;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/*
 * Created by Andrew Brin on 4/7/2016.
 */
@Module
public class PopupModule {
    public PopupModule(){
    }

    @Provides
    @Singleton
    PopupWindow providesPopupWindow(Application application){
        return new PopupWindow(application);
    }

}

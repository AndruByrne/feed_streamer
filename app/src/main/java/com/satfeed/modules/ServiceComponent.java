package com.satfeed.modules;

import android.widget.PopupWindow;

import com.satfeed.activity.LoginAndStreamActivity;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        LoginAndStreamModule.class,
        PopupModule.class,
        ThreadingModule.class})

public interface ServiceComponent {
    void inject(LoginAndStreamActivity activity);

    PopupWindow popupWindow();
}

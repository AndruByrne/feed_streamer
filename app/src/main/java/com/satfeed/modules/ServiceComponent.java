package com.satfeed.modules;

import android.databinding.ObservableField;

import com.satfeed.activity.LoginAndStreamActivity;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        EmailFieldModule.class,
        LoginAndStreamModule.class,
        ThreadingModule.class})

public interface ServiceComponent {
    void inject(LoginAndStreamActivity activity);

    @Named("HailingEmail")
    ObservableField<String> getHailingEmail();
}

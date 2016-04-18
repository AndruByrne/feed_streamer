package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/17/2016.
 */

import android.databinding.ObservableField;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EmailFieldModule {

    @Singleton
    @Provides
    @Named("HailingEmail")
    ObservableField<String> returnsHailingEmail(){ return new ObservableField<>(); }
}

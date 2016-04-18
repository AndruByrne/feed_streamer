package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import com.satfeed.activity.EditTextLooper;

import dagger.Module;
import dagger.Provides;

@Module
public class EditTextModule {

    @Provides
    EditTextLooper getEditTextLooper(){
        return new EditTextLooper();
    }
}

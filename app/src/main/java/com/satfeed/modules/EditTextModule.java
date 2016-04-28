package com.satfeed.modules;

/*
 * Created by Andrew Brin on 4/18/2016.
 */

import com.satfeed.activity.LoopingEditTextAdapter;

import dagger.Module;
import dagger.Provides;

@Module
public class EditTextModule {

    @Provides
    LoopingEditTextAdapter getEditTextLooper(){
        return new LoopingEditTextAdapter();
    }
}

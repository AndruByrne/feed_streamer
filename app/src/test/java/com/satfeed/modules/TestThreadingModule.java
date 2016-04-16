package com.satfeed.modules;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/*
 * Created by Andrew Brin on 3/21/2016.
 */
public class TestThreadingModule extends ThreadingModule {
    @Override
    public Scheduler providesScheduler(){
        return AndroidSchedulers.mainThread();
    }
}

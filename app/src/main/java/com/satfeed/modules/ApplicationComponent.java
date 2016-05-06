package com.satfeed.modules;

import android.app.Application;

import com.satfeed.activity.TouchHandlers;
import com.satfeed.services.SubscriptionAccountant;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton //  Singleton is the annotation for the Application scope
@Component(modules = {
        AppModule.class,
        SubscriptionAccountantModule.class,
        TouchHandlersModule.class,
        ThreadingModule.class})
public interface ApplicationComponent {

    TouchHandlers getTouchHandlers(); //  for the first View Model

    Application getContext();

    SubscriptionAccountant getSubscriptionAccountant();
}

package com.satfeed.modules;

import com.satfeed.services.SubscriptionAccountant;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/*
 * Created by Andrew Brin on 5/5/2016.
 */
@Module
public class SubscriptionAccountantModule {

    @Provides
    @Singleton
    SubscriptionAccountant getSubscriptionAccountant(){
        return new SubscriptionAccountant();
    }
}

package com.satfeed.modules;

import android.databinding.DataBindingComponent;

import java.net.InetSocketAddress;

import javax.inject.Singleton;

import dagger.Component;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        EditTextModule.class,
        SocketAddressModule.class,
        StreamingSurfaceModule.class,
        ThreadingModule.class})
public interface ServiceComponent extends DataBindingComponent{
        InetSocketAddress getServerAddress();
}

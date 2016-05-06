package com.satfeed.modules;

/*
 * Created by Andrew Brin on 5/4/2016.
 */

import android.databinding.DataBindingComponent;

import com.satfeed.activity.TouchHandlers;

import dagger.Component;

@SansUserScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                EditTextModule.class,
        })

public interface SansUserAdapterComponent extends DataBindingComponent {
    TouchHandlers getTouchHandlers();
}

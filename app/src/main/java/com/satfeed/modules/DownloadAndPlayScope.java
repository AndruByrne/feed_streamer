package com.satfeed.modules;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Scope;

/*
 * Created by Andrew Brin on 5/3/2016.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface DownloadAndPlayScope { }

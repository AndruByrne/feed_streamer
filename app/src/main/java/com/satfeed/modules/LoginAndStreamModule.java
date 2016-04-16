package com.satfeed.modules;

import dagger.Module;

/*
 * Created by Andrew Brin on 3/1/2016.
 */

@Module
public class LoginAndStreamModule {
    private String baseUrl;

    public LoginAndStreamModule(String baseUrl){ this.baseUrl = baseUrl; }

}

package com.satfeed;

/*
 * Created by Andrew Brin on 3/2/2016.
 */

import com.satfeed.modules.LoginAndStreamModule;
import com.satfeed.modules.MockLoginAndStreamModule;
import com.satfeed.modules.TestThreadingModule;
import com.satfeed.modules.ThreadingModule;

public class TestFeedStreamerApplication extends FeedStreamerApplication {

    @Override
    public LoginAndStreamModule getLoginAndStreamModule(String apiBaseUrl) {
        return new MockLoginAndStreamModule(apiBaseUrl);
    }

    @Override
    public ThreadingModule getThreadingModule(){
        return new TestThreadingModule();
    }
}

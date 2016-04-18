package com.satfeed;

/*
 * Created by Andrew Brin on 3/2/2016.
 */

import com.satfeed.modules.SocketAddressModule;
import com.satfeed.modules.TestThreadingModule;
import com.satfeed.modules.ThreadingModule;

public class TestFeedStreamerApplication extends FeedStreamerApplication {

    @Override
    public SocketAddressModule getSocketAddressModule() {
        return new SocketAddressModule("localhost", FeedStreamerApplication.STREAMING_PORT);
    }

    @Override
    public ThreadingModule getThreadingModule(){
        return new TestThreadingModule();
    }
}

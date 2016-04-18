package com.satfeed.activity;

import com.satfeed.BuildConfig;
import com.satfeed.TestFeedStreamerApplication;
import com.satfeed.test_utils.RoboTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.paperdb.Paper;

import static org.junit.Assert.assertTrue;


@RunWith(RoboTestRunner.class)
@Config(constants = BuildConfig.class,
        application = TestFeedStreamerApplication.class,
        sdk = 21)
public class LoginAndStreamActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testOnResumeCallsSFAPI() throws Exception {
        LoginAndStreamActivity activity = Robolectric.setupActivity(LoginAndStreamActivity.class);
        assertTrue(activity != null);

        System.out.println("Keys: " + Paper.book().getAllKeys());

        // assert identification code
        assertTrue(Paper.book().getAllKeys().size() == 6);
        assertTrue(Paper.book().getAllKeys() == Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5}));

    }

    @After
    public void tearDown() throws Exception {
    }
}

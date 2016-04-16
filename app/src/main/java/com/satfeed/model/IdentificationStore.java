package com.satfeed.model;

/*
 * Created by Andrew Brin on 3/3/2016.
 */

import android.app.Application;

import java.util.ArrayList;

import io.paperdb.Paper;
import rx.Observable;

public class IdentificationStore {

    static public Observable<ArrayList<String>> getAllKeys() {
        return Observable.just(new ArrayList<>(Paper.book().getAllKeys()));
    }

    static public void updatePaperstore() {
    }

    public static void nuke(Application application) {
        Paper.book().destroy();
    }

    public static String getRecord(String s) {
        return Paper.book().read(s);
    }

    public static void initialize(Application application) {
        Paper.init(application);
    }
}

package com.firebase.drawing;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;

/**
 * @author mimming
 * @since 12/5/14.
 *
 * Initialize Firebase with the application context. This must happen before the client is used.
 */
public class DrawingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().enablePersistence();
        //Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
        PinnedBoardManager.setContext(this);
    }
}

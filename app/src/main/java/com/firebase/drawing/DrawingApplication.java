package com.firebase.drawing;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Initialize Firebase with the application context and set disk persistence (to ensure our data survives
 * app restarts).
 * These must happen before the Firebase client is used.
 */
public class DrawingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        //Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
        SyncedBoardManager.setContext(this);
    }
}

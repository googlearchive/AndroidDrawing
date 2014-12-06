package com.firebase.drawing;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by mimming on 12/5/14.
 */
public class DrawingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

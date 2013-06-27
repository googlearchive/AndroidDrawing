package com.firebase.drawing;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Logger;
import com.firebase.client.ValueEventListener;

public class DrawingActivity extends Activity {

    private Paint paint;

    private static final String FIREBASE_URL = "https://hiw-gsoltis1.firebaseIO-demo.com";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
        Firebase ref = new Firebase(FIREBASE_URL);
        setContentView(new DrawingView(this, ref));
        ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean)dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(DrawingActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DrawingActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled() {
                // No-op
            }
        });
    }



}

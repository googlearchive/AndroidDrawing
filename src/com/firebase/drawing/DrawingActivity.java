package com.firebase.drawing;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Logger;
import com.firebase.client.ValueEventListener;

public class DrawingActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {

    // TODO: change this to your own Firebase URL
    private static final String FIREBASE_URL = "https://android-drawing.firebaseIO-demo.com";

    private static final int COLOR_MENU_ID = Menu.FIRST;

    private DrawingView drawingView;
    private Firebase ref;
    private ValueEventListener connectedListener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = new Firebase(FIREBASE_URL);
        drawingView = new DrawingView(this, ref);
        setContentView(drawingView);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectedListener = ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
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

    @Override
    public void onStop() {
        super.onStop();

        ref.getRoot().child(".info/connected").removeEventListener(connectedListener);
        drawingView.cleanup();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == COLOR_MENU_ID) {
            new ColorPickerDialog(this, this, 0xFFFF0000).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void colorChanged(int newColor) {
        drawingView.setColor(newColor);
    }
}

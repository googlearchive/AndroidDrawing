package com.firebase.drawing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DrawingActivity extends ActionBarActivity implements ColorPickerDialog.OnColorChangedListener {
    public static final int THUMBNAIL_SIZE = 256;

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int CLEAR_MENU_ID = COLOR_MENU_ID + 1;
    private static final int PIN_MENU_ID = CLEAR_MENU_ID + 1;

    private DrawingView mDrawingView;
    private Firebase mFirebaseRef; // Firebase base URL
    private Firebase mMetadataRef;
    private Firebase mSegmentsRef;
    private ValueEventListener mConnectedListener;
    private String mBoardId;
    private int mBoardWidth;
    private int mBoardHeight;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String url = intent.getStringExtra("FIREBASE_URL");
        final String boardId = intent.getStringExtra("BOARD_ID");
        Log.i("AndroidDrawing", "Adding DrawingView on "+url+" for boardId "+boardId);
        mFirebaseRef = new Firebase(url);
        mBoardId = boardId;
        mMetadataRef = mFirebaseRef.child("boardmetas").child(boardId);
        mSegmentsRef = mFirebaseRef.child("boardsegments").child(mBoardId);
        //((TextView)this.findViewById(R.id.text_field)).setText("Loading board...");
        mMetadataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> boardValues = (Map<String, Object>) dataSnapshot.getValue();
                mBoardWidth = ((Long) boardValues.get("width")).intValue();
                mBoardHeight = ((Long) boardValues.get("height")).intValue();

                mDrawingView = new DrawingView(DrawingActivity.this, mFirebaseRef.child("boardsegments").child(boardId), mBoardWidth, mBoardHeight);
                setContentView(mDrawingView);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up a notification to let us know when we're connected or disconnected from the Firebase servers
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(DrawingActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DrawingActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        // Clean up our listener so we don't have it attached twice.
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mDrawingView.cleanup();
        this.updateThumbnail(mBoardWidth, mBoardHeight, mSegmentsRef, mMetadataRef);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.menu_drawing, menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c').setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, CLEAR_MENU_ID, 2, "Clear").setShortcut('5', 'x');
        menu.add(0, PIN_MENU_ID, 3, "Pin").setShortcut('6', 'p').setIcon(android.R.drawable.ic_lock_lock);

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
        } else if (item.getItemId() == CLEAR_MENU_ID) {
            mDrawingView.cleanup();
            mSegmentsRef.removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        throw firebaseError.toException();
                    }
                    mDrawingView = new DrawingView(DrawingActivity.this, mFirebaseRef.child("boardsegments").child(mBoardId), mBoardWidth, mBoardHeight);
                    setContentView(mDrawingView);
                    //mDrawingView.clear();
                }
            });

            return true;
        } else if (item.getItemId() == PIN_MENU_ID) {
            Log.i("AndroidDrawing", "Pinning board "+mBoardId);
            mSegmentsRef.pin();
            mMetadataRef.pin();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public static void updateThumbnail(int boardWidth, int boardHeight, Firebase segmentsRef, final Firebase metadataRef) {
        final float scale = Math.min(1.0f * THUMBNAIL_SIZE / boardWidth, 1.0f * THUMBNAIL_SIZE / boardHeight);
        final Bitmap b = Bitmap.createBitmap(Math.round(boardWidth * scale), Math.round(boardHeight * scale), Bitmap.Config.ARGB_8888);
        final Canvas buffer = new Canvas(b);

        buffer.drawRect(0, 0, b.getWidth(), b.getHeight(), DrawingView.paintFromColor(Color.WHITE, Paint.Style.FILL_AND_STROKE));
        Log.i("AndroidDrawing", "Generating thumbnail of " + b.getWidth() + "x" + b.getHeight());

        segmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot segmentSnapshot : dataSnapshot.getChildren()) {
                    Segment segment = segmentSnapshot.getValue(Segment.class);
                    buffer.drawPath(
                            DrawingView.getPathForPoints(segment.getPoints(), scale),
                            DrawingView.paintFromColor(segment.getColor())
                    );
                }
                String encoded = encodeToBase64(b);
                metadataRef.child("thumbnail").setValue(encoded, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            throw firebaseError.toException();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = com.firebase.client.utilities.Base64.encodeBytes(b);

        return imageEncoded;
    }
    public static Bitmap decodeFromBase64(String input) throws IOException {
        byte[] decodedByte = com.firebase.client.utilities.Base64.decode(input);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    public void colorChanged(int newColor) {
        mDrawingView.setColor(newColor);
    }
}

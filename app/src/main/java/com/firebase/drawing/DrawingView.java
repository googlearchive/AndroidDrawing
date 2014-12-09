package com.firebase.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author greg
 * @since 6/26/13
 */
public class DrawingView extends View {

    private static final int PIXEL_SIZE = 8;

    private Paint mPaint;
    private int mLastX;
    private int mLastY;
    private Canvas mBuffer;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Firebase mFirebaseRef;
    private ChildEventListener mListener;
    private int mCurrentColor = 0xFFFF0000;
    private Path mPath;
    private Set<String> mOutstandingSegments;
    private Segment mCurrentSegment;
    private Path mChildPath = new Path();

    public DrawingView(Context context, Firebase ref) {
        super(context);

        mOutstandingSegments = new HashSet<String>();
        mPath = new Path();
        this.mFirebaseRef = ref;

        mListener = ref.addChildEventListener(new ChildEventListener() {
            /**
             * @param dataSnapshot The data we need to construct a new Segment
             * @param previousChildName Supplied for ordering, but we don't really care about ordering in this app
             */
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String name = dataSnapshot.getKey();
                // To prevent lag, we draw our own segments as they are created. As a result, we need to check to make
                // sure this event is a segment drawn by another user before we draw it
                if (!mOutstandingSegments.contains(name)) {
                    // Deserialize the data into our Segment class
                    Segment segment = dataSnapshot.getValue(Segment.class);
                    drawSegment(segment, paintFromColor(segment.getColor()));
                    // Tell the view to redraw itself
                    invalidate();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // No-op
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // No-op
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // No-op
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void cleanup() {
        mFirebaseRef.removeEventListener(mListener);
    }

    public void setColor(int color) {
        mCurrentColor = color;
        mPaint.setColor(color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
    }

    private Paint paintFromColor(int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        return p;
    }

    private void drawSegment(Segment segment, Paint paint) {
        mChildPath.reset();
        List<Point> points = segment.getPoints();
        Point current = points.get(0);
        mChildPath.moveTo(current.x * PIXEL_SIZE, current.y * PIXEL_SIZE);
        Point next = null;
        for (int i = 1; i < points.size(); ++i) {
            next = points.get(i);
            mChildPath.quadTo(current.x * PIXEL_SIZE, current.y * PIXEL_SIZE, ((next.x + current.x) * PIXEL_SIZE) / 2, ((next.y + current.y) * PIXEL_SIZE) / 2);
            current = next;
        }
        if (next != null) {
            mChildPath.lineTo(next.x * PIXEL_SIZE, next.y * PIXEL_SIZE);
        }
        mBuffer.drawPath(mChildPath, paint);
    }

    private void onTouchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mCurrentSegment = new Segment(mCurrentColor);
        mLastX = (int) x / PIXEL_SIZE;
        mLastY = (int) y / PIXEL_SIZE;
        mCurrentSegment.addPoint(mLastX, mLastY);
    }

    private void onTouchMove(float x, float y) {

        int x1 = (int) x / PIXEL_SIZE;
        int y1 = (int) y / PIXEL_SIZE;

        float dx = Math.abs(x1 - mLastX);
        float dy = Math.abs(y1 - mLastY);
        if (dx >= 1 || dy >= 1) {
            mPath.quadTo(mLastX * PIXEL_SIZE, mLastY * PIXEL_SIZE, ((x1 + mLastX) * PIXEL_SIZE) / 2, ((y1 + mLastY) * PIXEL_SIZE) / 2);
            mLastX = x1;
            mLastY = y1;
            mCurrentSegment.addPoint(mLastX, mLastY);
        }
    }

    private void onTouchEnd() {
        mPath.lineTo(mLastX * PIXEL_SIZE, mLastY * PIXEL_SIZE);
        mBuffer.drawPath(mPath, mPaint);
        mPath.reset();
        Firebase segmentRef = mFirebaseRef.push();
        final String segmentName = segmentRef.getKey();
        mOutstandingSegments.add(segmentName);
        // Save our segment into Firebase. This will let other clients see the data and add it to their own canvases.
        // Also make a note of the outstanding segment name so we don't do a duplicate draw in our onChildAdded callback.
        // We can remove the name from mOutstandingSegments once the completion listener is triggered, since we will have
        // received the child added event by then.
        segmentRef.setValue(mCurrentSegment, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError error, Firebase firebaseRef) {
                mOutstandingSegments.remove(segmentName);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchEnd();
                invalidate();
                break;
        }
        return true;
    }

}

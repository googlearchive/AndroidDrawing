package com.firebase.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

/**
 * User: greg
 * Date: 6/26/13
 * Time: 10:12 AM
 */
public class DrawingView extends View {

    private static final int PIXEL_SIZE = 8;
    private static final Map<String, Integer> COLOR_MAP = new HashMap<String, Integer>();
    private static final Map<Integer, String> COLOR_NAME_MAP = new HashMap<Integer, String>();
    static {
        // Support the colors in the web drawing example
        COLOR_MAP.put("fff", 0xFFFFFFFF);
        COLOR_NAME_MAP.put(0xFFFFFFFF, "fff");
        COLOR_MAP.put("000", 0xFF000000);
        COLOR_NAME_MAP.put(0xFF000000, "000");
        COLOR_MAP.put("f00", 0xFFFF0000);
        COLOR_NAME_MAP.put(0xFFFF0000, "f00");
        COLOR_MAP.put("0f0", 0xFF00FF00);
        COLOR_NAME_MAP.put(0xFF00FF00, "0f0");
        COLOR_MAP.put("00f", 0xFF0000FF);
        COLOR_NAME_MAP.put(0xFF0000FF, "00f");
        COLOR_MAP.put("88f", 0xFF8888FF);
        COLOR_NAME_MAP.put(0xFF8888FF, "88f");
        COLOR_MAP.put("f8d", 0xFFFF88DD);
        COLOR_NAME_MAP.put(0xFFFF88DD, "f8d");
        COLOR_MAP.put("f88", 0xFFFF8888);
        COLOR_NAME_MAP.put(0xFFFF8888, "f88");
        COLOR_MAP.put("f05", 0xFFFF0055);
        COLOR_NAME_MAP.put(0xFFFF0055, "f05");
        //["f80","0f8","cf0","08f","408","ff8","8ff"];
    }

    private Paint paint;
    private int lastX;
    private int lastY;
    private Canvas buffer;
    private Bitmap bitmap;
    private Paint bitmapPaint;
    private Firebase ref;
    private ChildEventListener listener;
    private int currentColor = 0xFFFF0000;
    private Path path;
    private Firebase currentPathRef;
    private Segment currentSegment;

    public DrawingView(Context context, Firebase ref) {
        super(context);

        path = new Path();
        this.ref = ref;

        listener = ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //drawPixel(dataSnapshot.getName(), (String)dataSnapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onCancelled() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);

        bitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        buffer = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);

        canvas.drawPath(path, paint);
    }

    private void onTouchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        currentSegment = new Segment(COLOR_NAME_MAP.get(currentColor));
        lastX = (int)x / PIXEL_SIZE;
        lastY = (int)y / PIXEL_SIZE;
    }

    private void onTouchMove(float x, float y) {

        int x1 = (int)x / PIXEL_SIZE;
        int y1 = (int)y / PIXEL_SIZE;

        float dx = Math.abs(x1 - lastX);
        float dy = Math.abs(y1 - lastY);
        if (dx >= 1 || dy >= 1) {
            path.quadTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE, ((x1 + lastX) * PIXEL_SIZE) / 2, ((y1 + lastY) * PIXEL_SIZE) /2);
            lastX = x1;
            lastY = y1;
            currentSegment.addPoint(lastX, lastY);
        }
    }

    private void onTouchEnd() {
        path.lineTo(lastX * PIXEL_SIZE, lastY * PIXEL_SIZE);
        buffer.drawPath(path, paint);
        path.reset();
    }

    private void drawPixel(String name, String colorName) {
        Log.d("Drawing", "Drawing: " + name + " " + colorName);
        String[] parts = name.split(":");
        int x = Integer.parseInt(parts[0]) * PIXEL_SIZE;
        int y = Integer.parseInt(parts[1]) * PIXEL_SIZE;
        int color = COLOR_MAP.get(colorName);
        boolean needsInvalidate = false;
        for (int i = 0; i < PIXEL_SIZE; ++i) {
            for (int j = 0; j < PIXEL_SIZE; ++j) {
                int pixelColor = bitmap.getPixel(x + i, y + j);
                if (pixelColor != color) {
                    needsInvalidate = true;
                    bitmap.setPixel(x + i, y + j, color);
                }
            }
        }
        if (needsInvalidate) {
            invalidate();
        }
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

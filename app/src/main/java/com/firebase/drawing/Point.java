package com.firebase.drawing;

/**
 * @author greg
 * @since 6/26/13
 */
public class Point {
    int x;
    int y;

    // Required default constructor for Firebase serialization / deserialization
    @SuppressWarnings("unused")
    private Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

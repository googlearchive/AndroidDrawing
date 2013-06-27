package com.firebase.drawing;

/**
 * User: greg
 * Date: 6/26/13
 * Time: 9:11 PM
 */
public class Point {
    int x;
    int y;

    private Point() {}

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

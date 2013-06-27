package com.firebase.drawing;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * User: greg
 * Date: 6/26/13
 * Time: 6:44 PM
 */
public class Segment {

    private List<Point> points = new ArrayList<Point>();
    private String color;

    private Segment() {}

    public Segment(String color) {
        this.color = color;
    }

    public void addPoint(int x, int y) {
        Point p = new Point(x, y);
        points.add(p);
    }

    public List<Point> getPoints() {
        return points;
    }

    public String getColor() {
        return color;
    }
}

package com.github.drxaos.mpsync.examples.circles.sim;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public class Spawn implements Serializable {
    private double x, y, vx, vy, radius;
    private ArrayList<Accel> accelerations = new ArrayList<Accel>();

    private Spawn() {
    }

    public Spawn(Spawn copyFrom) {
        this.x = copyFrom.x;
        this.y = copyFrom.y;
        this.vx = copyFrom.vx;
        this.vy = copyFrom.vy;
        this.radius = copyFrom.radius;
        for (Accel acceleration : copyFrom.accelerations) {
            this.accelerations.add(new Accel(acceleration));
        }
    }

    public Spawn(int x, int y, double vx, double vy, int radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    public Spawn(int x, int y) {
        this(x, y, 0.0, 0.0, 100);
    }

    public Vector2D velVector() {
        return new Vector2D(this.vx(), this.vy());
    }

    public void applyDrag(double drag) {
        this.vx = (drag * this.vx);
        this.vy = (drag * this.vy);
    }

    public Accel sumAccel() {
        double xAccel = 0, yAccel = 0;
        for (int i = 0; i < this.accelerations.size(); i++) {
            xAccel += this.accelerations.get(i).ax();
            yAccel += this.accelerations.get(i).ay();
        }
        this.accelerations.clear();
        return new Accel(xAccel, yAccel);
    }

    public void addAccel(Accel a) {
        this.accelerations.add(a);
    }

    public void updateVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void updatePos(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    public void updateCenterPos(double newX, double newY) {
        this.x = newX - radius;
        this.y = newY - radius;
    }

    public double vx() {
        return this.vx;
    }

    public double vy() {
        return this.vy;
    }

    public int dimX() {
        return (int) (this.radius * 2);
    }

    public int dimY() {
        return (int) (this.radius * 2);
    }

    public Point2D getCenter() {
        return new Point2D.Double(this.x + (this.dimX() / 2), this.y
                + (this.dimY() / 2));
    }

    public double getRadius() {
        return this.radius;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getX2() {
        return (this.x + this.dimX());
    }

    public double getY2() {
        return (this.y + this.dimY());
    }

    public double getCenterX() {
        return (this.x + this.radius);
    }

    public double getCenterY() {
        return (this.y + this.radius);
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;
    }
}

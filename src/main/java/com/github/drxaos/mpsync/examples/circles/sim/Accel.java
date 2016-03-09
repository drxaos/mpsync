package com.github.drxaos.mpsync.examples.circles.sim;

import java.io.Serializable;

public class Accel implements Serializable {
    private double ax, ay;

    private Accel() {
    }

    public Accel(Accel copyFrom) {
        this.ax = copyFrom.ax;
        this.ay = copyFrom.ay;
    }

    public Accel(double ax, double ay) {
        this.ax = ax;
        this.ay = ay;
    }

    public double ax() {
        return this.ax;
    }

    public double ay() {
        return this.ay;
    }
}
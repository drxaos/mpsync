package com.github.drxaos.mpsync.examples.circles.engine;

import java.io.Serializable;

public class CirclesInfo implements Serializable {
    public double timeFraction;

    public CirclesInfo() {
    }

    public CirclesInfo(double timeFraction) {
        this.timeFraction = timeFraction;
    }
}

package com.github.drxaos.mpsync.examples.circles;

import java.io.Serializable;

public class CirclesInfo implements Serializable {
    public double timeFraction;

    public CirclesInfo() {
    }

    public CirclesInfo(double timeFraction) {
        this.timeFraction = timeFraction;
    }
}

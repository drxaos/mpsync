package com.github.drxaos.mpsync.sync;

import java.io.Serializable;

public class SimState<STATE> implements Serializable {
    public int frame;
    public STATE state;
    public long timestamp;

    public SimState() {
        this.timestamp = System.currentTimeMillis();
    }

    public SimState(int frame, STATE state) {
        this.frame = frame;
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SimState(F:" + frame + ", " + state + ")";
    }
}
package com.github.drxaos.mpsync.sync;

import java.io.Serializable;

public class SimState<STATE> implements Serializable {
    public long frame;
    public STATE state;
    public boolean override;
    public long timestamp;
    public SimState<STATE> prevState = null;

    public SimState() {
        this.timestamp = System.currentTimeMillis();
    }

    public SimState(SimState<STATE> simState) {
        this.timestamp = System.currentTimeMillis();
        this.frame = simState.frame;
        this.state = simState.state;
    }

    public SimState(long frame, STATE state) {
        this.frame = frame;
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimState<?> simState = (SimState<?>) o;

        return frame == simState.frame;

    }

    @Override
    public int hashCode() {
        return (int) frame;
    }

    @Override
    public String toString() {
        return "SimState(F:" + frame + ", " + state + ")";
    }
}
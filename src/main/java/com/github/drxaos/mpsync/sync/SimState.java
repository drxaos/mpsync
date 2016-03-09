package com.github.drxaos.mpsync.sync;

public class SimState<STATE> {
    public int frame;
    public STATE state;

    public SimState() {
    }

    public SimState(int frame, STATE state) {
        this.frame = frame;
        this.state = state;
    }
}
package com.github.drxaos.mpsync.sync;

import java.io.Serializable;

public class SimInput<INPUT> implements Serializable {
    private int idCounter = 1;

    public int id;
    public int frame;
    public INPUT input;
    public long timestamp;

    public SimInput() {
        this.id = idCounter++;
        this.timestamp = System.currentTimeMillis();
    }

    public SimInput(int frame, INPUT input) {
        this.id = idCounter++;
        this.frame = frame;
        this.input = input;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SimInput(F:" + frame + ", id:" + id + ", " + input + ")";
    }
}
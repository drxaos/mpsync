package com.github.drxaos.mpsync.sync;

import java.io.Serializable;

public class SimInput<INPUT> implements Serializable {
    private static int idCounter = 1;

    public long id;
    public long frame;
    public int client = 0;
    public INPUT input;
    public long timestamp;
    transient boolean accepted = false;

    public SimInput() {
        this.id = idCounter++;
        this.timestamp = System.currentTimeMillis();
    }

    public SimInput(long frame, INPUT input) {
        this.id = idCounter++;
        this.frame = frame;
        this.input = input;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimInput<?> simInput = (SimInput<?>) o;

        if (id != simInput.id) return false;
        return client == simInput.client;

    }

    @Override
    public int hashCode() {
        int result = (int) id;
        result = 31 * result + client;
        return result;
    }

    @Override
    public String toString() {
        return "SimInput(F:" + frame + ", id:" + id + ", " + input + ")";
    }
}
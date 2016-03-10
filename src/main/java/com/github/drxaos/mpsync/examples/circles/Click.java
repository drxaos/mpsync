package com.github.drxaos.mpsync.examples.circles;

import java.io.Serializable;

public class Click implements Serializable {
    public Click() {
    }

    public Click(int x, int y, double vx, double vy, int r) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.r = r;
    }

    int x, y, r;
    double vx, vy;
}

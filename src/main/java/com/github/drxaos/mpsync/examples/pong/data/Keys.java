package com.github.drxaos.mpsync.examples.pong.data;

import java.io.Serializable;

public class Keys implements Serializable {
    public boolean key_up = false;
    public boolean key_down = false;

    public Keys() {
    }

    public Keys(boolean key_up, boolean key_down) {
        this.key_up = key_up;
        this.key_down = key_down;
    }
}

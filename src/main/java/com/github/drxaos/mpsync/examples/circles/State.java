package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.examples.circles.sim.Spawn;

import java.io.Serializable;
import java.util.ArrayList;

public class State implements Serializable {

    public State() {
    }

    public ArrayList<Spawn> living = new ArrayList<Spawn>();

}

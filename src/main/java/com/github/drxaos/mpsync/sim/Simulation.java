package com.github.drxaos.mpsync.sim;

import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.util.Collection;

public interface Simulation<STATE, INPUT> {

    STATE getFullState();

    void setFullState(STATE state);

    void step();

    INPUT getInput();

    void input(SimInput<INPUT> simInput);
}

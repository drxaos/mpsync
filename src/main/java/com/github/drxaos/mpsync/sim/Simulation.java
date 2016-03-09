package com.github.drxaos.mpsync.sim;

import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.util.Collection;

public interface Simulation<STATE, INPUT> {

    STATE getFullState();

    void setFullState(STATE state);

    void mergeAndSeek(SimState<STATE> simState, Collection<SimInput<INPUT>> simInputs, int toFrame);

    void step();

    void input(SimInput<INPUT> simInput);
}

package com.github.drxaos.mpsync.bus;

import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.IOException;

public interface Bus<STATE, INPUT> {

    void sendFullState(SimState<STATE> simState);

    SimState<STATE> getFullState();

    void sendInput(SimInput<INPUT> simInput);

    SimInput<INPUT> getInput();

    void start() throws IOException;
}

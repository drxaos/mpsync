package com.github.drxaos.mpsync.sim;

import com.github.drxaos.mpsync.sync.SimInput;

public interface Simulation<STATE, INPUT, INFO> {

    STATE getFullState();

    void setFullState(STATE state);

    void step();

    INPUT getInput();

    INFO getInfo();

    void input(SimInput<INPUT> simInput);

    void setServerInfo(INFO info);
}

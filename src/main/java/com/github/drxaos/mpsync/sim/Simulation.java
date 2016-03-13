package com.github.drxaos.mpsync.sim;

import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;

public interface Simulation<STATE, INPUT, INFO> {

    STATE getFullState();

    void setFullState(STATE state);

    void step();

    INPUT getInput();

    INFO getInfo();

    void input(SimInput<INPUT> simInput);

    void setServerInfo(ServerInfo<INFO> info);

    void lockView();

    void unlockView();
}

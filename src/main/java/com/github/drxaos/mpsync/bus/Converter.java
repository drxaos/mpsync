package com.github.drxaos.mpsync.bus;

import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

public interface Converter<STATE, INPUT, INFO> {

    boolean isState(byte[] data);

    boolean isInput(byte[] data);

    boolean isServerInfo(byte[] data);

    byte[] serializeSimState(SimState<STATE> simState);

    byte[] serializeSimInput(SimInput<INPUT> simInput);

    byte[] serializeServerInfo(ServerInfo<INFO> serverInfo);

    SimState<STATE> deserializeState(byte[] data);

    SimInput<INPUT> deserializeInput(byte[] data);

    ServerInfo<INFO> deserializeServerInfo(byte[] data);

}

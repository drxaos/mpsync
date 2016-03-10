package com.github.drxaos.mpsync.bus.impl;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.IOException;
import java.net.Socket;

public class SimpleTcpClientEndpoint<STATE, INPUT> implements Bus<STATE, INPUT>, SocketOwner {

    String host;
    int port;

    SimpleTcpEndpoint<STATE, INPUT> simpleTcpEndpoint;

    Converter<STATE, INPUT> stateinputConverter;

    public SimpleTcpClientEndpoint(String host, int port, Converter<STATE, INPUT> stateinputConverter) {
        this.host = host;
        this.port = port;
        this.stateinputConverter = stateinputConverter;
    }

    public void sendFullState(SimState<STATE> simState) {
        simpleTcpEndpoint.broadcastFullState(simState);
    }

    public SimState<STATE> getFullState() {
        return simpleTcpEndpoint.getFullState();
    }

    public void sendInput(SimInput<INPUT> simInput) {
        simpleTcpEndpoint.sendInput(simInput);
    }

    public SimInput<INPUT> getInput() {
        return simpleTcpEndpoint.getInput();
    }

    public void start() throws IOException {
        simpleTcpEndpoint = new SimpleTcpEndpoint<STATE, INPUT>(stateinputConverter);
        simpleTcpEndpoint.start(new Socket(host, port), this);
    }

    public void onClose(SimpleTcpEndpoint endpoint) {
        simpleTcpEndpoint.shutdown();
    }
}
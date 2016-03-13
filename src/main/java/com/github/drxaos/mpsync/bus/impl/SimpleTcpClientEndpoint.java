package com.github.drxaos.mpsync.bus.impl;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.IOException;
import java.net.Socket;

public class SimpleTcpClientEndpoint<STATE, INPUT, INFO> implements Bus<STATE, INPUT, INFO>, SocketOwner {

    String host;
    int port;

    volatile SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint;

    Converter<STATE, INPUT, INFO> stateinputConverter;

    public boolean debug = false;

    public SimpleTcpClientEndpoint(String host, int port, Converter<STATE, INPUT, INFO> stateinputConverter) {
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

    public void setServerInfo(ServerInfo<INFO> serverInfo) {
        simpleTcpEndpoint.sendServerInfo(serverInfo);
    }

    public ServerInfo<INFO> getServerInfo() {
        return simpleTcpEndpoint == null ? null : simpleTcpEndpoint.getServerInfo();
    }

    public void start() throws IOException {
        simpleTcpEndpoint = new SimpleTcpEndpoint<STATE, INPUT, INFO>(stateinputConverter, null);
        simpleTcpEndpoint.start(new Socket(host, port), this);
    }

    public void onClose(SimpleTcpEndpoint endpoint) {
        simpleTcpEndpoint.shutdown();
    }

    public boolean isDebugEnabled() {
        return debug;
    }
}
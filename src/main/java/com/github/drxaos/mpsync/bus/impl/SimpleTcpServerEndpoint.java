package com.github.drxaos.mpsync.bus.impl;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SimpleTcpServerEndpoint<STATE, INPUT> implements Bus<STATE, INPUT>, SocketOwner {

    int port;

    Converter<STATE, INPUT> stateinputConverter;

    List<SimpleTcpEndpoint<STATE, INPUT>> simpleTcpEndpoints = Collections.synchronizedList(new LinkedList<SimpleTcpEndpoint<STATE, INPUT>>());

    public SimpleTcpServerEndpoint(int port, Converter<STATE, INPUT> stateinputConverter) throws IOException {
        this.port = port;
        this.stateinputConverter = stateinputConverter;
    }

    public void sendFullState(SimState<STATE> simState) {
        for (SimpleTcpEndpoint<STATE, INPUT> simpleTcpEndpoint : simpleTcpEndpoints) {
            simpleTcpEndpoint.broadcastFullState(simState);
        }
    }

    public SimState<STATE> getFullState() {
        for (SimpleTcpEndpoint<STATE, INPUT> simpleTcpEndpoint : simpleTcpEndpoints) {
            SimState<STATE> simState = simpleTcpEndpoint.getFullState();
            if (simState != null) {
                return simState;
            }
        }
        return null;
    }

    public void sendInput(SimInput<INPUT> simInput) {
        for (SimpleTcpEndpoint<STATE, INPUT> simpleTcpEndpoint : simpleTcpEndpoints) {
            simpleTcpEndpoint.sendInput(simInput);
        }
    }

    public SimInput<INPUT> getInput() {
        for (SimpleTcpEndpoint<STATE, INPUT> simpleTcpEndpoint : simpleTcpEndpoints) {
            SimInput<INPUT> simInput = simpleTcpEndpoint.getInput();
            if (simInput != null) {
                return simInput;
            }
        }
        return null;
    }

    public void start() throws IOException {
        final ServerSocket welcomeSocket = new ServerSocket(port);

        Thread connector = new Thread("SimpleTcpEndpointServer") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket connectionSocket = welcomeSocket.accept();
                        SimpleTcpEndpoint<STATE, INPUT> endpoint = new SimpleTcpEndpoint<STATE, INPUT>(stateinputConverter);
                        endpoint.start(connectionSocket, SimpleTcpServerEndpoint.this);
                        simpleTcpEndpoints.add(endpoint);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        connector.setDaemon(true);
        connector.start();
    }

    public void onClose(SimpleTcpEndpoint endpoint) {
        endpoint.shutdown();
        simpleTcpEndpoints.remove(endpoint);
    }
}
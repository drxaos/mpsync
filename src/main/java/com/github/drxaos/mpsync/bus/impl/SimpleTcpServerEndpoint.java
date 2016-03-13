package com.github.drxaos.mpsync.bus.impl;

import com.github.drxaos.mpsync.bus.Bus;
import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleTcpServerEndpoint<STATE, INPUT, INFO> implements Bus<STATE, INPUT, INFO>, SocketOwner {

    int port;

    Converter<STATE, INPUT, INFO> stateinputConverter;

    List<SimpleTcpEndpoint<STATE, INPUT, INFO>> simpleTcpEndpoints = Collections.synchronizedList(new LinkedList<SimpleTcpEndpoint<STATE, INPUT, INFO>>());

    public SimpleTcpServerEndpoint(int port, Converter<STATE, INPUT, INFO> stateinputConverter) throws IOException {
        this.port = port;
        this.stateinputConverter = stateinputConverter;
    }

    public void sendFullState(SimState<STATE> simState) {
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            simpleTcpEndpoint.broadcastFullState(simState);
        }
    }

    public SimState<STATE> getFullState() {
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            SimState<STATE> simState = simpleTcpEndpoint.getFullState();
            if (simState != null) {
                return simState;
            }
        }
        return null;
    }

    public void sendInput(SimInput<INPUT> simInput) {
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            simpleTcpEndpoint.sendInput(simInput);
        }
    }

    public SimInput<INPUT> getInput() {
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            SimInput<INPUT> simInput = simpleTcpEndpoint.getInput();
            if (simInput != null) {
                return simInput;
            }
        }
        return null;
    }

    ServerInfo<INFO> serverInfo;

    public void setServerInfo(ServerInfo<INFO> serverInfo) {
        this.serverInfo = serverInfo;
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            simpleTcpEndpoint.sendServerInfo(new ServerInfo<INFO>(serverInfo, simpleTcpEndpoint.client));
        }
    }

    public ServerInfo<INFO> getServerInfo() {
        for (SimpleTcpEndpoint<STATE, INPUT, INFO> simpleTcpEndpoint : simpleTcpEndpoints) {
            ServerInfo<INFO> serverInfo = simpleTcpEndpoint.getServerInfo();
            if (serverInfo != null) {
                return serverInfo;
            }
        }
        return null;
    }

    public void start() throws IOException {
        final ServerSocket welcomeSocket = new ServerSocket(port);

        Thread connector = new Thread("SimpleTcpEndpointServer") {
            AtomicInteger clientIdCounter = new AtomicInteger(0);

            @Override
            public void run() {
                while (true) {
                    try {
                        Socket connectionSocket = welcomeSocket.accept();
                        int clientId = clientIdCounter.incrementAndGet();
                        SimpleTcpEndpoint<STATE, INPUT, INFO> endpoint = new SimpleTcpEndpoint<STATE, INPUT, INFO>(stateinputConverter, clientId);
                        endpoint.start(connectionSocket, SimpleTcpServerEndpoint.this);
                        while (serverInfo == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                        simpleTcpEndpoints.add(endpoint);
                        endpoint.sendServerInfo(new ServerInfo<INFO>(serverInfo, clientId));
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
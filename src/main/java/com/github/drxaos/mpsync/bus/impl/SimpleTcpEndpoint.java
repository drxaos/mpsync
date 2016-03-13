package com.github.drxaos.mpsync.bus.impl;

import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleTcpEndpoint<STATE, INPUT, INFO> {

    Socket socket;
    private SocketOwner owner;

    DataOutputStream outToServer;
    BufferedReader inFromServer;

    Converter<STATE, INPUT, INFO> converter;

    ConcurrentLinkedQueue<SimState<STATE>> simStatesIn = new ConcurrentLinkedQueue<SimState<STATE>>();
    ConcurrentLinkedQueue<SimState<STATE>> simStatesOut = new ConcurrentLinkedQueue<SimState<STATE>>();

    ConcurrentLinkedQueue<SimInput<INPUT>> simInputsIn = new ConcurrentLinkedQueue<SimInput<INPUT>>();
    ConcurrentLinkedQueue<SimInput<INPUT>> simInputsOut = new ConcurrentLinkedQueue<SimInput<INPUT>>();

    ConcurrentLinkedQueue<ServerInfo<INFO>> serverInfosIn = new ConcurrentLinkedQueue<ServerInfo<INFO>>();
    ConcurrentLinkedQueue<ServerInfo<INFO>> serverInfosOut = new ConcurrentLinkedQueue<ServerInfo<INFO>>();

    Thread reader, writer;

    Integer client;

    private void debug(String message) {
        if (owner.isDebugEnabled()) {
            System.out.println(message);
        }
    }

    public SimpleTcpEndpoint(Converter<STATE, INPUT, INFO> converter, Integer client) {
        this.converter = converter;
        this.client = client;
    }

    public void broadcastFullState(SimState<STATE> simState) {
        simStatesOut.add(simState);
    }

    public SimState<STATE> getFullState() {
        return simStatesIn.poll();
    }

    public void sendInput(SimInput<INPUT> simInput) {
        simInputsOut.add(simInput);
    }

    public SimInput<INPUT> getInput() {
        return simInputsIn.poll();
    }

    public ServerInfo<INFO> getServerInfo() {
        return serverInfosIn.poll();
    }

    public void sendServerInfo(ServerInfo<INFO> serverInfo) {
        serverInfosOut.add(serverInfo);
    }

    public boolean isAlive() {
        return reader.isAlive() && writer.isAlive();
    }

    public void shutdown() {
        reader.stop();
        writer.stop();
        try {
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public void start(Socket socket, final SocketOwner owner) throws IOException {
        this.socket = socket;
        this.owner = owner;
        outToServer = new DataOutputStream(socket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        reader = new Thread("SimpleTcpEndpointReader") {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] data = inFromServer.readLine().getBytes();
                        debug("IN(" + client + "): " + new String(data));
                        if (converter.isInput(data)) {
                            SimInput<INPUT> simInput = converter.deserializeInput(data);
                            if (client != null) { // i'm a server
                                simInput.client = SimpleTcpEndpoint.this.client;
                            }
                            simInputsIn.add(simInput);
                        } else if (converter.isState(data)) {
                            SimState<STATE> simState = converter.deserializeState(data);
                            simStatesIn.add(simState);
                        } else if (converter.isServerInfo(data)) {
                            ServerInfo<INFO> simState = converter.deserializeServerInfo(data);
                            serverInfosIn.add(simState);
                        }
                    }
                } catch (Exception e) {
                    owner.onClose(SimpleTcpEndpoint.this);
                }
            }
        };

        writer = new Thread("SimpleTcpEndpointWriter") {
            @Override
            public void run() {
                try {
                    while (true) {
                        SimInput<INPUT> simInput = simInputsOut.poll();
                        while (simInput != null) {
                            byte[] data = converter.serializeSimInput(simInput);
                            debug("OUT(" + client + "): " + new String(data));
                            outToServer.write(data);
                            outToServer.writeBytes("\n");
                            simInput = simInputsOut.poll();
                        }
                        SimState<STATE> simState = simStatesOut.poll();
                        while (simState != null) {
                            byte[] data = converter.serializeSimState(simState);
                            debug("OUT(" + client + "): " + new String(data));
                            outToServer.write(data);
                            outToServer.writeBytes("\n");
                            simState = simStatesOut.poll();
                        }
                        ServerInfo<INFO> serverInfo = serverInfosOut.poll();
                        while (serverInfo != null) {
                            byte[] data = converter.serializeServerInfo(serverInfo);
                            debug("OUT(" + client + "): " + new String(data));
                            outToServer.write(data);
                            outToServer.writeBytes("\n");
                            serverInfo = serverInfosOut.poll();
                        }
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    owner.onClose(SimpleTcpEndpoint.this);
                }
            }
        };

        reader.setDaemon(true);
        reader.start();

        writer.setDaemon(true);
        writer.start();
    }

}

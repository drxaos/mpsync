package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpClientEndpoint;
import com.github.drxaos.mpsync.examples.circles.engine.*;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sync.ClientSimSync;

import java.io.IOException;

public class CirclesClient {

    public static void main(String[] args) throws IOException {
        SimpleTcpClientEndpoint<State, Click, CirclesInfo> clientEndpoint = new SimpleTcpClientEndpoint<State, Click, CirclesInfo>("localhost", 5001, new CirclesConverter());
        clientEndpoint.start();

        CirclesEngine engine = new CirclesEngine();
        ClientSimSync<State, Click, CirclesInfo> sync = new ClientSimSync<State, Click, CirclesInfo>(engine, clientEndpoint);
        sync.debug = true;
        sync.start();

        MainWindow ui = new MainWindow();
        ui.setEngine(engine);
        ui.start();
    }
}

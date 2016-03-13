package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpServerEndpoint;
import com.github.drxaos.mpsync.examples.circles.engine.*;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sync.ServerSimSync;

import java.io.IOException;

public class CirclesServer {

    public static void main(String[] args) throws IOException {
        SimpleTcpServerEndpoint<State, Click, CirclesInfo> serverEndpoint = new SimpleTcpServerEndpoint<State, Click, CirclesInfo>(5001, new CirclesConverter());
        serverEndpoint.start();

        CirclesEngine engine = new CirclesEngine();
        ServerSimSync<State, Click, CirclesInfo> sync = new ServerSimSync<State, Click, CirclesInfo>(engine, serverEndpoint);
        sync.debug = true;
        sync.start();

        MainWindow ui = new MainWindow();
        ui.setEngine(engine);
        ui.start();
    }
}

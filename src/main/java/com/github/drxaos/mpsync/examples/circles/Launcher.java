package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpClientEndpoint;
import com.github.drxaos.mpsync.bus.impl.SimpleTcpServerEndpoint;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sync.ClientSimSync;
import com.github.drxaos.mpsync.sync.ServerSimSync;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        boolean isServer = args[0].equals("srv");

        if (isServer) {

            SimpleTcpServerEndpoint<State, Click, CirclesInfo> serverEndpoint = new SimpleTcpServerEndpoint<State, Click, CirclesInfo>(5001, new CirclesConverter());
            serverEndpoint.start();

            CirclesEngine engine = new CirclesEngine();
            ServerSimSync<State, Click, CirclesInfo> sync = new ServerSimSync<State, Click, CirclesInfo>(engine, serverEndpoint);
            sync.start();

            MainWindow ui = new MainWindow();
            ui.setEngine(engine);
            ui.start();

        } else {

            SimpleTcpClientEndpoint<State, Click, CirclesInfo> clientEndpoint = new SimpleTcpClientEndpoint<State, Click, CirclesInfo>("localhost", 5001, new CirclesConverter());
            clientEndpoint.start();

            CirclesEngine engine = new CirclesEngine();
            ClientSimSync<State, Click, CirclesInfo> sync = new ClientSimSync<State, Click, CirclesInfo>(engine, clientEndpoint);
            sync.start();

            MainWindow ui = new MainWindow();
            ui.setEngine(engine);
            ui.start();

        }

    }
}

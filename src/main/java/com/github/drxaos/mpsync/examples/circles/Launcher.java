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

            SimpleTcpServerEndpoint<State, Click> serverEndpoint = new SimpleTcpServerEndpoint<State, Click>(5001, new CirclesConverter());
            serverEndpoint.start();

            CirclesEngine engine = new CirclesEngine();
            ServerSimSync<State, Click> sync = new ServerSimSync<State, Click>(engine, serverEndpoint);
            sync.start();

            MainWindow ui = new MainWindow();
            ui.setEngine(engine);
            ui.start();

        } else {

            SimpleTcpClientEndpoint<State, Click> clientEndpoint = new SimpleTcpClientEndpoint<State, Click>("localhost", 5001, new CirclesConverter());
            clientEndpoint.start();

            CirclesEngine engine = new CirclesEngine();
            ClientSimSync<State, Click> sync = new ClientSimSync<State, Click>(engine, clientEndpoint);
            sync.start();

            MainWindow ui = new MainWindow();
            ui.setEngine(engine);
            ui.start();

        }

    }
}

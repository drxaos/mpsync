package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpClientEndpoint;
import com.github.drxaos.mpsync.bus.impl.SimpleTcpServerEndpoint;
import com.github.drxaos.mpsync.examples.circles.ui.MainWindow;
import com.github.drxaos.mpsync.sync.ClientSimSync;
import com.github.drxaos.mpsync.sync.ServerSimSync;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws InterruptedException {

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpServerEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> serverEndpoint = new SimpleTcpServerEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>(5001, new CirclesConverter());
                    serverEndpoint.start();

                    CirclesEngine engine = new CirclesEngine();
                    ServerSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> sync = new ServerSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>(engine, serverEndpoint);
                    sync.start();

                    MainWindow ui = new MainWindow();
                    ui.setEngine(engine);
                    ui.start();

                    ui.setPosition(20, 120);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread.sleep(1000);

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpClientEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> clientEndpoint = new SimpleTcpClientEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>("localhost", 5001, new CirclesConverter());
                    clientEndpoint.start();

                    CirclesEngine engine = new CirclesEngine();
                    ClientSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> sync = new ClientSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>(engine, clientEndpoint);
                    sync.start();

                    MainWindow ui = new MainWindow();
                    ui.setEngine(engine);
                    ui.start();

                    ui.setPosition(820, 10);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpClientEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> clientEndpoint = new SimpleTcpClientEndpoint<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>("localhost", 5001, new CirclesConverter());
                    clientEndpoint.start();

                    CirclesEngine engine = new CirclesEngine();
                    ClientSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo> sync = new ClientSimSync<com.github.drxaos.mpsync.examples.circles.State, Click, CirclesInfo>(engine, clientEndpoint);
                    sync.start();

                    MainWindow ui = new MainWindow();
                    ui.setEngine(engine);
                    ui.start();

                    ui.setPosition(720, 540);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
}

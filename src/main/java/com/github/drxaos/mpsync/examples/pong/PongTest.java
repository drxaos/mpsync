package com.github.drxaos.mpsync.examples.pong;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpClientEndpoint;
import com.github.drxaos.mpsync.bus.impl.SimpleTcpServerEndpoint;
import com.github.drxaos.mpsync.examples.pong.data.Board;
import com.github.drxaos.mpsync.examples.pong.data.Info;
import com.github.drxaos.mpsync.examples.pong.data.Keys;
import com.github.drxaos.mpsync.examples.pong.data.PongConverter;
import com.github.drxaos.mpsync.sync.ClientSimSync;
import com.github.drxaos.mpsync.sync.ServerSimSync;

import java.io.IOException;

public class PongTest {
    public static void main(String[] args) throws InterruptedException {

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpServerEndpoint<Board, Keys, Info> serverEndpoint = new SimpleTcpServerEndpoint<Board, Keys, Info>(5001, new PongConverter());

                    Pong engine = new Pong();
                    ServerSimSync<Board, Keys, Info> sync = new ServerSimSync<Board, Keys, Info>(engine, serverEndpoint);
                    sync.debug = true;
                    sync.start();

                    PongWindow ui = new PongWindow();
                    ui.setContent(engine);
                    ui.setVisible(true);

                    serverEndpoint.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpClientEndpoint<Board, Keys, Info> clientEndpoint = new SimpleTcpClientEndpoint<Board, Keys, Info>("localhost", 5001, new PongConverter());

                    Pong engine = new Pong();
                    ClientSimSync<Board, Keys, Info> sync = new ClientSimSync<Board, Keys, Info>(engine, clientEndpoint);
                    sync.debug = true;
                    sync.start();

                    PongWindow ui = new PongWindow();
                    ui.setContent(engine);
                    ui.setVisible(true);

                    clientEndpoint.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    SimpleTcpClientEndpoint<Board, Keys, Info> clientEndpoint = new SimpleTcpClientEndpoint<Board, Keys, Info>("localhost", 5001, new PongConverter());

                    Pong engine = new Pong();
                    ClientSimSync<Board, Keys, Info> sync = new ClientSimSync<Board, Keys, Info>(engine, clientEndpoint);
                    sync.debug = true;
                    sync.start();

                    PongWindow ui = new PongWindow();
                    ui.setContent(engine);
                    ui.setVisible(true);

                    clientEndpoint.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

package com.github.drxaos.mpsync.examples.pong;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpClientEndpoint;
import com.github.drxaos.mpsync.examples.pong.data.Board;
import com.github.drxaos.mpsync.examples.pong.data.Info;
import com.github.drxaos.mpsync.examples.pong.data.Keys;
import com.github.drxaos.mpsync.examples.pong.data.PongConverter;
import com.github.drxaos.mpsync.sync.ClientSimSync;

import java.io.IOException;

public class PongClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        SimpleTcpClientEndpoint<Board, Keys, Info> clientEndpoint = new SimpleTcpClientEndpoint<Board, Keys, Info>("192.168.0.100", 5001, new PongConverter());

        Pong engine = new Pong();
        ClientSimSync<Board, Keys, Info> sync = new ClientSimSync<Board, Keys, Info>(engine, clientEndpoint);
        sync.debug = true;
        sync.start();

        PongWindow ui = new PongWindow();
        ui.setContent(engine);
        ui.setVisible(true);

        clientEndpoint.start();
    }
}

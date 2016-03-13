package com.github.drxaos.mpsync.examples.pong;

import com.github.drxaos.mpsync.bus.impl.SimpleTcpServerEndpoint;
import com.github.drxaos.mpsync.examples.pong.data.Board;
import com.github.drxaos.mpsync.examples.pong.data.Info;
import com.github.drxaos.mpsync.examples.pong.data.Keys;
import com.github.drxaos.mpsync.examples.pong.data.PongConverter;
import com.github.drxaos.mpsync.examples.pong.engine.Pong;
import com.github.drxaos.mpsync.examples.pong.engine.PongWindow;
import com.github.drxaos.mpsync.sync.ServerSimSync;

import java.io.IOException;

public class PongServer {
    public static void main(String[] args) throws InterruptedException, IOException {
        SimpleTcpServerEndpoint<Board, Keys, Info> serverEndpoint = new SimpleTcpServerEndpoint<Board, Keys, Info>(5001, new PongConverter());

        Pong engine = new Pong();
        ServerSimSync<Board, Keys, Info> sync = new ServerSimSync<Board, Keys, Info>(engine, serverEndpoint);
        sync.debug = true;
        sync.start();

        PongWindow ui = new PongWindow();
        ui.setContent(engine);
        ui.setVisible(true);

        serverEndpoint.start();
    }
}

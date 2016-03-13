package com.github.drxaos.mpsync.examples.pong.data;

import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class PongConverter implements Converter<Board, Keys, Info> {
    public boolean isState(byte[] data) {
        return data.length > 0 && data[0] == '#';
    }

    public boolean isInput(byte[] data) {
        return data.length > 0 && data[0] == '>';
    }

    public boolean isServerInfo(byte[] data) {
        return data.length > 0 && data[0] == 'i';
    }

    Gson gson = new Gson();

    public byte[] serializeSimState(SimState<Board> simState) {
        return ("#" + gson.toJson(simState)).getBytes();
    }

    public byte[] serializeSimInput(SimInput<Keys> simInput) {
        return (">" + gson.toJson(simInput)).getBytes();
    }

    public byte[] serializeServerInfo(ServerInfo<Info> serverInfo) {
        return ("i" + gson.toJson(serverInfo)).getBytes();
    }

    public SimState<Board> deserializeState(byte[] data) {
        Type type = new TypeToken<SimState<Board>>() {
        }.getType();

        SimState<Board> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }

    public SimInput<Keys> deserializeInput(byte[] data) {
        Type type = new TypeToken<SimInput<Keys>>() {
        }.getType();

        SimInput<Keys> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }

    public ServerInfo<Info> deserializeServerInfo(byte[] data) {
        Type type = new TypeToken<ServerInfo<Info>>() {
        }.getType();

        ServerInfo<Info> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }
}

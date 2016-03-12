package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class CirclesConverter implements Converter<State, Click, CirclesInfo> {
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

    public byte[] serializeSimState(SimState<State> simState) {
        return ("#" + gson.toJson(simState)).getBytes();
    }

    public byte[] serializeSimInput(SimInput<Click> simInput) {
        return (">" + gson.toJson(simInput)).getBytes();
    }

    public byte[] serializeServerInfo(ServerInfo<CirclesInfo> serverInfo) {
        return ("i" + gson.toJson(serverInfo)).getBytes();
    }

    public SimState<State> deserializeState(byte[] data) {
        Type type = new TypeToken<SimState<State>>() {
        }.getType();

        SimState<State> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }

    public SimInput<Click> deserializeInput(byte[] data) {
        Type type = new TypeToken<SimInput<Click>>() {
        }.getType();

        SimInput<Click> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }

    public ServerInfo<CirclesInfo> deserializeServerInfo(byte[] data) {
        Type type = new TypeToken<ServerInfo<CirclesInfo>>() {
        }.getType();

        ServerInfo<CirclesInfo> result = gson.fromJson(new String(data).substring(1), type);
        return result;
    }
}

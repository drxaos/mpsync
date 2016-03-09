package com.github.drxaos.mpsync.examples.circles;

import com.github.drxaos.mpsync.bus.Converter;
import com.github.drxaos.mpsync.sync.SimInput;
import com.github.drxaos.mpsync.sync.SimState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class CirclesConverter implements Converter<State, Click> {
    public boolean isState(byte[] data) {
        return data.length > 0 && data[0] == '#';
    }

    public boolean isInput(byte[] data) {
        return data.length > 0 && data[0] == '>';
    }

    Gson gson = new Gson();

    public byte[] serializeSimState(SimState<State> simState) {
        return ("#" + gson.toJson(simState)).getBytes();
    }

    public byte[] serializeSimInput(SimInput<Click> simInput) {
        return (">" + gson.toJson(simInput)).getBytes();
    }

    public SimState<State> deserializeState(byte[] data) {
        Type type = new TypeToken<SimState<State>>() {
        }.getType();
        return gson.fromJson(new String(data).substring(1), type);
    }

    public SimInput<Click> deserializeInput(byte[] data) {
        Type type = new TypeToken<SimInput<Click>>() {
        }.getType();
        return gson.fromJson(new String(data).substring(1), type);
    }
}

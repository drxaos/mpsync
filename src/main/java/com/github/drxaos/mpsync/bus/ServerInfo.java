package com.github.drxaos.mpsync.bus;

import java.io.Serializable;

public class ServerInfo<INFO> implements Serializable {

    public int clientId;
    public int keyFrameInterval;
    public int keyFrameIntervalTime;
    public int inputLatencyTime;

    public INFO info;

    public ServerInfo() {
    }

    public ServerInfo(ServerInfo<INFO> serverInfo, int clientId) {
        this.clientId = clientId;
        this.keyFrameInterval = serverInfo.keyFrameInterval;
        this.keyFrameIntervalTime = serverInfo.keyFrameIntervalTime;
        this.inputLatencyTime = serverInfo.inputLatencyTime;
        this.info = serverInfo.info;
    }

    public ServerInfo(int keyFrameInterval, int keyFrameIntervalTime, int inputLatencyTime, INFO info) {
        this.keyFrameInterval = keyFrameInterval;
        this.keyFrameIntervalTime = keyFrameIntervalTime;
        this.inputLatencyTime = inputLatencyTime;
        this.info = info;
    }
}

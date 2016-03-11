package com.github.drxaos.mpsync.bus;

import java.io.Serializable;

public class ServerInfo<INFO> implements Serializable {

    public int clientId;
    public int fullStateFramesInterval;
    public long oneFrameInterval;
    public INFO info;

    public ServerInfo() {
    }

    public ServerInfo(ServerInfo<INFO> serverInfo, int clientId) {
        this.clientId = clientId;
        this.fullStateFramesInterval = serverInfo.fullStateFramesInterval;
        this.oneFrameInterval = serverInfo.oneFrameInterval;
        this.info = serverInfo.info;
    }

    public ServerInfo(int fullStateFramesInterval, long oneFrameInterval, INFO info) {
        this.fullStateFramesInterval = fullStateFramesInterval;
        this.oneFrameInterval = oneFrameInterval;
        this.info = info;
    }
}

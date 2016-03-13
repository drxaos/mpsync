package com.github.drxaos.mpsync.bus.impl;

public interface SocketOwner {

    void onClose(SimpleTcpEndpoint endpoint);

    boolean isDebugEnabled();

}

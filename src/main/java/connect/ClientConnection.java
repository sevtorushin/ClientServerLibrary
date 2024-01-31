package connect;

import java.net.InetSocketAddress;

public abstract class ClientConnection implements TCPConnection, Reconnectable, Transmitter, AutoCloseable {
    InetSocketAddress endpoint;
    volatile boolean isConnected;

    public boolean isConnected(){
        return isConnected;
    }
}

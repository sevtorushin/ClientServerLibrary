package connect;

import java.net.InetSocketAddress;

public abstract class ClientConnection implements TCPConnection, Reconnectable, Transmitter {
    InetSocketAddress endpoint;
    boolean isConnected;

    public boolean isConnected(){
        return isConnected;
    }
}

package connect;

public interface TCPConnection {
    boolean connect();
    boolean disconnect();
    boolean isConnected();
}

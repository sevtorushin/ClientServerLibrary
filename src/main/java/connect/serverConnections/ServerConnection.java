package connect.serverConnections;

import lombok.Getter;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class ServerConnection {

    @Getter
    private final Integer port;

    public ServerConnection(Integer port) {
        this.port = port;
    }

    public abstract SocketChannel accept() throws IOException;

    public abstract void close() throws IOException;
}

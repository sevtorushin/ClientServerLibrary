package connect.serverConnections;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@ToString
@EqualsAndHashCode
public abstract class ServerConnection implements AutoCloseable{

    @Getter
    private final Integer port;

    public ServerConnection(Integer port) {
        this.port = port;
    }

    public abstract SocketChannel accept() throws IOException;

    public abstract void close() throws IOException;
}

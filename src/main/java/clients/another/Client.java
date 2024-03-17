package clients.another;

import connect.clientConnections.ClientConnection;
import connect.clientConnections.SocketChannelConnection;
import connect.clientConnections.SocketConnection;
import entity.Net;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.containers.MessageStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString
@EqualsAndHashCode
public class Client implements AutoCloseable, Net {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Object id;
    private static int clientCount = 0;
    @Getter
    protected ClientConnection clientConnection;

    private static final Logger log = LogManager.getLogger(Client.class.getSimpleName());

    public Client(SocketChannel socketChannel) {
        this.clientConnection = new SocketChannelConnection(socketChannel);
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public Client(Socket socket) {
        this.clientConnection = new SocketConnection(socket);
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public Client(InetSocketAddress endpoint) {
        this.clientConnection = new SocketChannelConnection(endpoint);
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public Client(String host, int port) {
        this.clientConnection = new SocketChannelConnection(host, port);
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public boolean connect() {
        try {
            return clientConnection.connect();
        } catch (IOException e) {
            log.warn(String.format("%s:%d connection error",
                    clientConnection.getEndpoint().getHostString(), clientConnection.getEndpoint().getPort()), e);
        }
        return false;
    }

    public boolean disconnect() {
        try {
            return clientConnection.disconnect();
        } catch (IOException e) {
            log.error(String.format("Disconnect error. Maybe channel or socket for endpoint %s is closed",
                    getClientConnection()), e);
        }
        return false;
    }

    public boolean isConnected() {
        return clientConnection.isConnected();
    }

    public int receiveMessage(ByteBuffer buffer) throws IOException {
        return clientConnection.read(buffer);
    }

    public void sendMessage(ByteBuffer message) throws IOException {
        clientConnection.write(message);
    }

    @Override
    public void close() {
        try {
            clientConnection.close();
        } catch (IOException e) {
            log.error(String.format("Connection closing error. Channel or socket for endpoint %s closing error",
                    getClientConnection()), e);
        }
    }
}

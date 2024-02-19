package clients.another;

import connect.ClientConnection;
import connect.SocketChannelConnection;
import entity.Net;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.MessageStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(exclude = {"storage"})
@EqualsAndHashCode(exclude = {"storage"})
public class Client implements AutoCloseable, Net {
    @Getter
    @Setter
    private String name;
    private final long id;
    private static int clientCount = 0;
    @Getter
    protected ClientConnection clientConnection;
    private final MessageStorage storage;

    private static final Logger log = LogManager.getLogger(Client.class.getSimpleName());

    public Client(SocketChannel socketChannel) {
        this.clientConnection = new SocketChannelConnection(socketChannel);
        this.storage = new MessageStorage();
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public Client(InetSocketAddress endpoint) {
        this.clientConnection = new SocketChannelConnection(endpoint);
        this.storage = new MessageStorage();
        this.name = getClass().getSimpleName();
        this.id = ++clientCount;
    }

    public Client(String host, int port) {
        this.clientConnection = new SocketChannelConnection(host, port);
        this.storage = new MessageStorage();
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

    public ByteBuffer receiveMessage() {
        ByteBuffer storageTempBuffer = storage.getTempBuffer();
        int n = clientConnection.read(storageTempBuffer);
        storageTempBuffer.flip();
        if (n < 1) {
            return storage.getEmptyBuffer();
        }

        return storage.retrieveFromStorage();
    }

    public void sendMessage(ByteBuffer message) {
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

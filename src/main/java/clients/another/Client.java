package clients.another;

import connect.ClientConnection;
import connect.SocketChannelConnection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(exclude = {"buffer", "emptyBuffer"})
public class Client implements AutoCloseable {
    @Getter
    @Setter
    private String name;
    private final long id;
    private static int clientCount = 0;
    @Getter
    protected ClientConnection clientConnection;
    private final ByteBuffer buffer;
    private final ByteBuffer emptyBuffer;
    @Getter
    @Setter
    private int bufferSize = 8192;

    private static final Logger log = LogManager.getLogger(Client.class.getSimpleName());

    {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
    }


    public Client(SocketChannel socketChannel) {
        this.clientConnection = new SocketChannelConnection(socketChannel);
        this.name = "";
        this.id = ++clientCount;
    }//todo имя для каждого нового клиента

    public Client(InetSocketAddress endpoint) {
        this.clientConnection = new SocketChannelConnection(endpoint);
        this.name = "";
        this.id = ++clientCount;
    }

    public Client(String host, int port) {
        this.clientConnection = new SocketChannelConnection(host, port);
        this.name = "";
        this.id = ++clientCount;
    }

    public void connect() {
        try {
            clientConnection.connect();
        } catch (IOException e) {
            log.warn(String.format("%s:%d connection error",
                    clientConnection.getEndpoint().getHostString(), clientConnection.getEndpoint().getPort()), e);
        }
    }

    public void disconnect() {
        try {
            clientConnection.disconnect();
        } catch (IOException e) {
            log.error(String.format("Disconnect error. Maybe channel or socket for endpoint %s is closed",
                    getClientConnection()), e);
        }
    }

    public boolean isConnected() {
        return clientConnection.isConnected();
    }

    public ByteBuffer receiveMessage() {
        int n = clientConnection.read(buffer);
        if (n < 1) {
            return emptyBuffer;
        }
        return buffer.duplicate();
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

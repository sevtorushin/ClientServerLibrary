package clients.another;

import connect.ClientConnection;
import connect.SocketChannelConnection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(exclude = {"buffer", "emptyBuffer"})
public class Client implements AutoCloseable {
    @Getter @Setter
    private String name;
    private final long id;
    private static int clientCount = 0;
    @Getter
    protected ClientConnection clientConnection;
    private final ByteBuffer buffer;
    private final ByteBuffer emptyBuffer;
    @Getter @Setter
    private int bufferSize = 8192;

    {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
    }


    public Client(SocketChannel socketChannel) {
        this.clientConnection = new SocketChannelConnection(socketChannel);
        this.name = "";
        this.id = ++clientCount;
    }

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
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            clientConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return clientConnection.isConnected();
    }

    public ByteBuffer receiveMessage() {
        if (!isConnected()) {
            return emptyBuffer;
        }
        try {
            int n = clientConnection.read(buffer);
            if (n < 1) {
                return emptyBuffer;
            }
        } catch (IOException e) {
            try {
                clientConnection.disconnect();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            clientConnection.reconnect();
            return emptyBuffer;
        }
        return buffer.duplicate();
    }

    public void sendMessage(ByteBuffer message) {
        if (!isConnected())
            return;
        try {
            clientConnection.write(message);
        } catch (IOException e) {
            try {
                clientConnection.disconnect();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            clientConnection.reconnect();
        }
    }

    @Override
    public void close() {
        try {
            clientConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

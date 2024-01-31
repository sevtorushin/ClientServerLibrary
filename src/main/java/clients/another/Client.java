package clients.another;

import connect.ClientConnection;
import connect.SocketChannelConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client implements AutoCloseable {
    private String name;
    private final long id;
    private static int clientCount = 0;
    protected ClientConnection clientConnection;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private ByteBuffer emptyBuffer = ByteBuffer.allocate(0);


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
        clientConnection.connect();
    }

    public void disconnect() {
        clientConnection.disconnect();
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
            clientConnection.disconnect();
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
            clientConnection.disconnect();
            clientConnection.reconnect();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    @Override
    public String toString() {
        return "ClientTest{" +
                ", connection=" + clientConnection +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public void close() throws Exception {
        clientConnection.close();
    }
}

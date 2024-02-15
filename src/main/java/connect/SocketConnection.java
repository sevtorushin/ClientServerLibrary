package connect;

import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

@ToString(callSuper = true)
public class SocketConnection extends ClientConnection {
    @ToString.Exclude
    private Socket socket;

    public SocketConnection(Socket socket) {
        this.socket = socket;
        this.endpoint = (InetSocketAddress) socket.getRemoteSocketAddress();
    }

    public SocketConnection(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    public SocketConnection(String host, int port) {
        this.endpoint = new InetSocketAddress(host, port);
    }

    protected boolean con() throws IOException {
        socket = new Socket();
        socket.connect(endpoint);
        isConnected = true;
        return isConnected;
    }

    @Override
    public boolean disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            isConnected = false;
        }
        return !isConnected();
    }

    @Override
    protected int read0(ByteBuffer buffer) throws IOException {
        return socket.getInputStream().read(buffer.array());
    }

    @Override
    protected void write0(ByteBuffer buffer) throws IOException {
        socket.getOutputStream().write(buffer.array());
    }
}
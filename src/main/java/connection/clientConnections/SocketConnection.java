package connection.clientConnections;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true,
        onlyExplicitlyIncluded = true)
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

    protected boolean connect0() throws IOException {
        if (socket != null && !socket.isClosed()) {
            isConnected = true;
            isClosed = false;
            return isConnected;
        }
        socket = new Socket();
        socket.connect(endpoint);
        isConnected = true;
        isClosed = false;
        return isConnected;
    }

    @Override
    public synchronized boolean disconnect() throws IOException {
        if (this.socket != null) {
            socket.close();
            isConnected = false;
        }
        return !isConnected;
    }

    @Override
    protected int read0(ByteBuffer buffer) throws IOException {
        buffer.clear();
        int bytes = socket.getInputStream().read(buffer.array());
        if (bytes == -1)
            return bytes;
        buffer.position(bytes);
        return bytes;
    }

    @Override
    protected void write0(ByteBuffer buffer) throws IOException {
        buffer.flip();
        byte[] b = ArrayUtils.toArrayAndTrim(buffer);
        try {
            socket.getOutputStream().write(b);
        } catch (IOException e) {
            buffer.position(buffer.limit());
            throw new IOException(e);
        }
    }

    @Override
    @ToString.Include(name = "localPort")
    public int getLocalPort() {
        if (socket == null)
            return 0;
        else return socket.getLocalPort();
    }

    @Override
    @ToString.Include(name = "remotePort")
    public int getRemotePort() {
        if (socket == null)
            return 0;
        else return socket.getPort();
    }
}

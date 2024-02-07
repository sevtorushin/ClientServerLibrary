package connect;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

@ToString(callSuper = true,
        exclude = {"socket", "reconnectPeriod"})
public class SocketConnection extends ClientConnection {
    private Socket socket;
    @Getter
    @Setter
    private long reconnectPeriod = 5000;

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

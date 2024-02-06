package connect;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    public int read(ByteBuffer buffer) {
        if (!isConnected)
            return 0;
        int bytes;
        try {
            buffer.clear();
            bytes = socket.getInputStream().read(buffer.array());
        } catch (IOException e) {
            try {
                disconnect();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            reconnect();
            return 0;
        }
        return bytes;
    }

    @Override
    public void write(ByteBuffer buffer) {
        if (!isConnected)
            return;
        try {
            socket.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            try {
                disconnect();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            reconnect();
        }
    }
}

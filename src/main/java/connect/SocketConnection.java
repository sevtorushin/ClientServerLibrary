package connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class SocketConnection extends ClientConnection {
    private Socket socket;

    public SocketConnection(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(endpoint);
            isConnected = true;
        } catch (IOException e) {
            reconnect();
        }
        return isConnected;
    }

    @Override
    public boolean disconnect() {
        try {
            socket.close();
            isConnected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !isConnected();
    }

    @Override
    public void reconnect() {
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected) {
                    t.cancel();
                    return;
                }
                System.out.println("Reconnect...");
                try {
                    socket = new Socket(); //todo вынести в отдельный метод con()
                    socket.connect(endpoint);
                    isConnected = true;
                    System.out.println("Connected");
                } catch (IOException e) {
                    System.out.println("Connection failed");
                }
            }
        }, 0, 5000);
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
            disconnect();
            reconnect();
            return 0;
        }
        return bytes;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if (!isConnected)
            return;
        try {
            socket.getOutputStream().write(buffer.array());
        } catch (IOException e) {
            disconnect();
            reconnect();
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}

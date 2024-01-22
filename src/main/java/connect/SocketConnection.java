package connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class SocketConnection extends ClientConnection {
    private Socket socket;
    private InetSocketAddress endpoint;
    private boolean isConnected;

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
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void reconnect() {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected) {
                    this.cancel();
                    return;
                }
                System.out.println("Reconnect...");
                try {
                    socket = new Socket();
                    socket.connect(endpoint);
                    isConnected = true;
                    System.out.println("Connected");
                } catch (IOException e){
                    System.out.println("Connection failed");
                }
            }
        }, 0, 5000);
    }

    @Override
    public int read(byte[] buffer) {
        if (!isConnected)
            return 0;
        int bytes = 0;
        try {
            bytes = socket.getInputStream().read(buffer);
        } catch (IOException e) {
            disconnect();
            reconnect();
            return 0;
        }
        return bytes;
    }

    @Override
    public void write(byte[] buffer) throws IOException {

    }
}

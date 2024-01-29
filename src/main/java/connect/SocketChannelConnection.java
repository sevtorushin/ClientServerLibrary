package connect;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

public class SocketChannelConnection extends ClientConnection {
    private SocketChannel channel;

    public SocketChannelConnection(SocketChannel channel) {
        this.channel = channel;
        try {
            this.endpoint = (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            throw new IllegalStateException("Channel is closed");
        }
    }

    public SocketChannelConnection(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    public SocketChannelConnection(String host, int port) {
        this.endpoint = new InetSocketAddress(host, port);
    }

    private boolean con() throws IOException {
        channel = SocketChannel.open();
        try {
            isConnected = channel.connect(endpoint);
            channel.configureBlocking(false);
        } catch (ConnectException e) {
            if (e.getMessage().contains("Connection refused")) {
                isConnected = false;
                throw new IOException("Not running server on specified endpoint " +
                        endpoint.getHostString() + ": " + endpoint.getPort());
            }
        }
        return isConnected;
    }

    @Override
    public boolean connect() {
        if (channel != null && isConnected) {
            throw new IllegalStateException("Calling the connect method again is not allowed");
        }
        try {
            con();
        } catch (IOException e) {
            reconnect();
        }
        return isConnected;
    }

    @Override
    public boolean disconnect() {
        if (this.channel != null) {
            try {
                this.channel.close();
                isConnected = false;
            } catch (IOException e) {
                System.err.println("I/O channel close exception");
            }
        }
        return !isConnected;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "SocketChannelConnection{" +
                "endpoint=" + endpoint +
                '}';
    }

    @Override
    public int read(ByteBuffer buffer) {
        if (!isConnected)
            return 0;
        int bytes;
        try {
            buffer.clear();
            bytes = channel.read(buffer);
        } catch (IOException e) {
            disconnect();
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
            channel.write(buffer);
        } catch (IOException e) {
            disconnect();
            reconnect();
        }
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
                    con();
                    System.out.println("Connected");
                } catch (IOException e) {
                    System.out.println("Connection failed");
                }
            }
        }, 0, 5000);
    }
}

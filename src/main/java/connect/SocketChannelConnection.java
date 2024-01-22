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
    private InetSocketAddress endpoint;
    private boolean isConnected;
    private boolean isAlive;
    private boolean autoReconnect;

    public SocketChannelConnection(SocketChannel channel) {
        this.channel = channel;
        try {
            this.endpoint = (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            throw new IllegalStateException("Channel is closed");
        }
        isAlive = true;
    }

    public SocketChannelConnection(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
        isAlive = true;
    }

    public SocketChannelConnection(String host, int port) {
        this.endpoint = new InetSocketAddress(host, port);
        isAlive = true;
    }

    private boolean con() throws IOException {
        channel = SocketChannel.open();
        try {
            isConnected = channel.connect(endpoint);
            isAlive = true;
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
        if (channel != null && channel.isConnected()) {
            throw new IllegalStateException("Calling the connect method again is not allowed");
        }
        try {
            con();
        } catch (IOException e) { //todo логировать и убрать if
            if (autoReconnect)
                checkConnection();
            return isConnected;
        }
        if (autoReconnect)
            checkConnection();
        return isConnected;
    }

    public boolean reset() {
        if (this.channel.isConnected()) {
            try {
                this.channel.close();
                isConnected = false;
            } catch (IOException e) {
                System.err.println("I/O channel close exception");
            }
        }
        return isReset();
    }

    @Override
    public boolean disconnect() {
        reset();
        isAlive = false;
        return isReset() && !isAlive;
    }

    private void checkConnection() {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isAlive) {
                    this.cancel();
                    return;
                }
                try {
                    channel.write(ByteBuffer.wrap(new byte[1]));
                } catch (IOException e) {
                    try {
                        if (!isAlive) {
                            this.cancel();
                            return;
                        }
                        reset();
                        if (con())
                            System.out.println("Connected: " + isConnected());
                        else throw new IOException();
                    } catch (IOException ioException) {
                        System.out.println("Connected: " + isConnected() + ". Reconnect...");
                    }
                    return;
                }
                System.out.println("Connected: " + isConnected());
            }
        }, 0, 5000);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    public boolean isReset() {
        return !channel.isConnected() && !isConnected;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    @Override
    public String toString() {
        return "ClientConnection{" +
                "endpoint=" + endpoint +
                '}';
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return channel.read(ByteBuffer.wrap(buffer));
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        channel.write(ByteBuffer.wrap(buffer));
    }

    @Override
    public void reconnect() { //todo хуйня реализация
        setAutoReconnect(true);
    }
}

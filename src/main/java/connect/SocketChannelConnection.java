package connect;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(callSuper = true,
        exclude = {"channel", "reconnectPeriod"})
public class SocketChannelConnection extends ClientConnection {
    private SocketChannel channel;
    @Getter
    @Setter
    private long reconnectPeriod = 5000;

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

    protected boolean con() throws IOException {
        channel = SocketChannel.open();
        isConnected = channel.connect(endpoint);
        channel.configureBlocking(false);
        return isConnected;
    }

    @Override
    public boolean disconnect() {
        if (this.channel != null) {
            try {
                channel.close();
                isConnected = false;
            } catch (IOException e) {
                System.err.println("I/O channel close exception");
            }
        }
        return !isConnected;
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
}

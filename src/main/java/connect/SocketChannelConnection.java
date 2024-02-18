package connect;

import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(callSuper = true)
public class SocketChannelConnection extends ClientConnection {
    @ToString.Exclude
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

    protected boolean con() throws IOException {
        if (channel!=null && channel.isConnected()) {
            isConnected = true;
            return isConnected;
        }
        channel = SocketChannel.open();
        isConnected = channel.connect(endpoint);
        channel.configureBlocking(false);
        return isConnected;
    }

    @Override
    public boolean disconnect() throws IOException {
        if (this.channel != null) {
            channel.close();
            isConnected = false;
        }
        return !isConnected;
    }

    @Override
    protected int read0(ByteBuffer buffer) throws IOException {
        return channel.read(buffer);
    }

    @Override
    protected void write0(ByteBuffer buffer) throws IOException {
        channel.write(buffer);
    }

    @Override
    public int getLocalPort(){
        return channel.socket().getLocalPort();
    }
}

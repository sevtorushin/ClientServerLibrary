package connection.clientConnections;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true,
        onlyExplicitlyIncluded = true)
public class SocketChannelConnection extends ClientConnection {
    @ToString.Exclude
    private SocketChannel channel;
    @ToString.Exclude
    private Selector selector;
    @ToString.Exclude
    private SelectionKey key;

    public SocketChannelConnection(SocketChannel channel) {
        this.channel = channel;
        try {
            this.endpoint = (InetSocketAddress) channel.getRemoteAddress();
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new IllegalStateException("Channel is closed");
        }
    }

    public SocketChannelConnection(InetSocketAddress endpoint) {
        try {
            this.endpoint = endpoint;
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketChannelConnection(String host, int port) {
        try {
            this.endpoint = new InetSocketAddress(host, port);
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean connect0() throws IOException {
        if (channel != null && channel.isConnected()) {
            key = channel.register(selector, SelectionKey.OP_READ);
            isConnected = true;
            isClosed = false;
            return isConnected;
        }
        channel = SocketChannel.open();
        selector = Selector.open();
        isConnected = channel.connect(endpoint);
        isClosed = !isConnected;
        channel.configureBlocking(false);
        key = channel.register(selector, SelectionKey.OP_READ);
        return isConnected;
    }

    @Override
    public synchronized boolean disconnect() throws IOException {
        if (this.channel != null) {
            channel.close();
            selector.close();
            isConnected = false;
        }
        return !isConnected;
    }

    @Override
    protected int read0(ByteBuffer buffer) throws IOException {
        if (selector.selectNow() < 1)
            return 0;
        selector.selectedKeys().clear();
        if (key.isReadable()) {
            buffer.clear();
            return channel.read(buffer);
        } else return 0;
    }

    @Override
    protected void write0(ByteBuffer buffer) throws IOException {
        buffer.flip();
        try {
            channel.write(buffer);
        } catch (IOException e) {
            buffer.position(buffer.limit());
            throw new IOException(e);
        }
    }

    @Override
    @ToString.Include(name = "localPort")
    public int getLocalPort() {
        if (channel == null)
            return 0;
        else return channel.socket().getLocalPort();
    }

    @Override
    @ToString.Include(name = "remotePort")
    public int getRemotePort() {
        if (channel == null)
            return 0;
        else return channel.socket().getPort();
    }
}

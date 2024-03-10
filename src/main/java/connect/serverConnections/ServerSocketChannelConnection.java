package connect.serverConnections;

import service.DefaultClientManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelConnection extends ServerConnection{
    private final ServerSocketChannel serverSocketChannel;

    public ServerSocketChannelConnection(Integer port) throws IOException {
        super(port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
    }

    @Override
    public SocketChannel accept() throws IOException {
        return serverSocketChannel.accept();
    }

    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
    }
}

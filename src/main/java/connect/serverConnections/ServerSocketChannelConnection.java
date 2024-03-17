package connect.serverConnections;

import service.DefaultClientManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelConnection extends ServerConnection{
    private final ServerSocketChannel serverSocketChannel;
    private final boolean blockingMode;
    private final long acceptTimeout = 500;

    public ServerSocketChannelConnection(Integer port, boolean blockingMode) throws IOException {
        super(port);
        this.blockingMode = blockingMode;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(blockingMode);
    }

    @Override
    public SocketChannel accept() throws IOException {
        if (!blockingMode)
            pause(acceptTimeout);
        return serverSocketChannel.accept();
    }

    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
    }

    private void pause(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

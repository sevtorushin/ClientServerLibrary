package connection.serverConnections;

import service.ReadProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelConnection extends ServerConnection {
    private final ServerSocketChannel serverSocketChannel;
    private final boolean blockingMode;

    private final ReadProperties propertiesReader = ReadProperties.getInstance();
    private final long acceptTimeout = Long.parseLong(propertiesReader.getValue("server.ServerSocketChannelConnection.acceptTimeout"));

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

    @Override
    public boolean isClosed() {
        return !serverSocketChannel.isOpen();
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

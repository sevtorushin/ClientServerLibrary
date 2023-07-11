package servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleServer implements Runnable {
    private final ServerSocketChannel serverSocketChannel;
    private InetSocketAddress endpoint;
    private final Selector selector;
    private int DEFAULT_SOCKET_POOL_SIZE = 1;
    private final ArrayBlockingQueue<SocketChannel> socketPool;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private boolean stopped;

    public SimpleServer(int port) throws IOException {
        this.endpoint = new InetSocketAddress(port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(endpoint);
        this.serverSocketChannel.configureBlocking(false);
        this.socketPool = new ArrayBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE, true);
        this.selector = Selector.open();
    }

    public SimpleServer(int port, int DEFAULT_SOCKET_POOL_SIZE) throws IOException {
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
        this.endpoint = new InetSocketAddress(port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(endpoint);
        this.serverSocketChannel.configureBlocking(false);
        this.socketPool = new ArrayBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE, true);
        this.selector = Selector.open();
    }

    @Override
    public void run() {
        SocketChannel clientSocket;
        stopped = false;
        while (!isStopped()) {
            try {
                clientSocket = serverSocketChannel.accept();
                if (clientSocket == null) {
                    Thread.sleep(500);
                    continue;
                }
                connectSocket(clientSocket);
                checkAliveSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server stopped");
    }


    private boolean connectSocket(SocketChannel socketChannel) {
        if (socketChannel == null)
            return false;
        boolean isAdded;
        try {
            isAdded = socketPool.offer(socketChannel);
            if (!isAdded) {
                socketChannel.close();
                System.out.println("Connection is rejected");
                return false;
            } else {
                socketChannel.configureBlocking(false);
                socketChannel.register(getSelector(), SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean rejectSocket(SocketChannel socketChannel) throws IOException {
        if (socketChannel == null)
            return false;
        socketChannel.close();
        return socketPool.remove(socketChannel);
    }

    public boolean rejectAllSockets() {
        socketPool.forEach(socketChannel -> {
            try {
                rejectSocket(socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return socketPool.isEmpty();
    }

    public void saveToCache(byte[] data) {
        try {
            cache.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAliveSocket() {
        return socketPool.removeIf(socketChannel -> !socketChannel.isConnected());
    }

    public BlockingQueue<SocketChannel> getSocketPool() {
        return socketPool;
    }

    public void stopServer() {
        try {
            stopped = true;
            rejectAllSockets();
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getSocketAmount() {
        return socketPool.size();
    }

    public LinkedBlockingQueue<byte[]> getCache() {
        return cache;
    }

//    public void restartServer(){
//        rejectAllSockets();
//        restart = true;
//    }

    public boolean isStopped() {
        return stopped;
    }

    public Selector getSelector() {
        return selector;
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        if (DEFAULT_SOCKET_POOL_SIZE > 1)
            return "SimpleMultiThreadServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "MultiThread Server" + "; " +
                    "Max clients=" + DEFAULT_SOCKET_POOL_SIZE +
                    '}';
        else
            return "SimpleMultiThreadServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "SingleThread Server" +
                    '}';
    }
}

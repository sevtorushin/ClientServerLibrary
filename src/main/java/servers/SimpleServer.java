package servers;

import entity.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleServer implements Runnable, Cached {
    private final ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress endpoint;
    private final Selector selector;
    private int DEFAULT_SOCKET_POOL_SIZE = 1;
    private final LinkedBlockingQueue<SocketChannel> socketPool;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private boolean stopped;

    public SimpleServer(int port) throws IOException {
        this.endpoint = new InetSocketAddress(port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(endpoint);
        this.serverSocketChannel.configureBlocking(false);
        this.socketPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
        this.selector = Selector.open();
    }

    public SimpleServer(int port, int DEFAULT_SOCKET_POOL_SIZE) throws IOException {
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
        this.endpoint = new InetSocketAddress(port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(endpoint);
        this.serverSocketChannel.configureBlocking(false);
        this.socketPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
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
                System.err.println(e.getMessage());
            }
        }
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
                System.out.println("Client connected");
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
            System.out.println("Server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCaching() {
        ByteBuffer buffer = ByteBuffer.allocate(512); //todo вынести емкость в константу
        try {
            while (!stopped) {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0) {
                    Thread.sleep(100);
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (stopped)
                        return;
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        try {
                            if (sc.read(buffer) == -1) {
                                rejectSocket(sc);
                                break;
                            }
                        } catch (IOException e) {
                            rejectSocket(sc);
                            break;
                        }
                        byte[] bytes = ArrayUtils.arrayTrim(buffer);
                        saveToCache(bytes);
//                                System.out.println(Arrays.toString(bytes));
                                System.out.println("Cache size = " + getCacheSize());
                        buffer.clear();
                    }
                }
            }
            System.out.println("Reading completed");
        } catch (IOException | InterruptedException e) {
            System.err.println("Caching interrupted\n" + e.getMessage());
        }
    }

    @Override
    public void saveToCache(byte[] data) {
        try {
            cache.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] readAllCache() {
        byte[] data;
        if (cache.size() == 0)
            data = new byte[0];
        else if (cache.size() == 1) {
            try {
                data = cache.take();
            } catch (InterruptedException e) {
                data = new byte[0];
            }
        } else
            data = cache.stream().reduce((bytes, bytes2) -> {
                byte[] b = Arrays.copyOf(bytes, bytes.length + bytes2.length);
                System.arraycopy(bytes2, 0, b, bytes.length, bytes2.length);
                return b;
            }).orElse(new byte[0]);
        cache.clear();
        return data;
    }

    @Override
    public byte[] readElementCache() {
        byte[] data;
        if (cache.size() == 0)
            data = new byte[0];
        else
            try {
                data = cache.take();
            } catch (InterruptedException e) {
                data = new byte[0];
            }
        return data;
    }

    LinkedBlockingQueue<byte[]> getCache() {
        return cache;
    }

    public int getSocketAmount() {
        return socketPool.size();
    }

    public int getCacheSize() {
        return cache.size();
    }

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
            return "SimpleServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "MultiThread Server" + "; " +
                    "Max clients=" + DEFAULT_SOCKET_POOL_SIZE +
                    '}';
        else
            return "SimpleServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "SingleThread Server" +
                    '}';
    }
}

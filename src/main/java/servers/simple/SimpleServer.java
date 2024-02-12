package servers.simple;

import service.Cached;
import exceptions.ConnectClientException;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SimpleServer implements Runnable, Cached<byte[]> {
    private final ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress endpoint;
    private final Selector selector;
    private int DEFAULT_SOCKET_POOL_SIZE = 1;
    private final LinkedBlockingQueue<SocketChannel> socketPool;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private final Map<String, Consumer<byte[]>> handlers = new ConcurrentHashMap<>();
    private boolean stopped;
    private final int TEMP_BUFFER_SIZE = 512;

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
        new Thread(() -> {
            try {
                processDataFromClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                return;
            } catch (ConnectClientException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    boolean connectSocket(SocketChannel socketChannel) throws IOException, ConnectClientException {
        if (socketChannel == null)
            return false;
        boolean isAdded;
        try {
            isAdded = socketPool.offer(socketChannel);
            if (!isAdded) {
                socketChannel.close();
                throw new ConnectClientException("Client rejected");
            } else {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            rejectSocket(socketChannel);
        }
        return true;
    }

    boolean rejectSocket(SocketChannel socketChannel) throws IOException {
        if (socketChannel == null)
            return false;
        socketChannel.close();
        return socketPool.remove(socketChannel);
    }

    boolean rejectAllSockets() throws IOException {
        for (SocketChannel channel : socketPool) {
            rejectSocket(channel);
        }
        return socketPool.isEmpty();
    }

    private boolean checkAliveSocket() {
        return socketPool.removeIf(socketChannel -> !socketChannel.isConnected());
    }

    BlockingQueue<SocketChannel> getSocketPool() {
        return socketPool;
    }

    void stopServer() throws IOException {
        stopped = true;
        rejectAllSockets();
        selector.close();
        serverSocketChannel.close();
    }

    void processDataFromClient() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(TEMP_BUFFER_SIZE);
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
                    if (stopped) {
                        return;
                    }
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
                        if (!handlers.isEmpty())
                            handlers.forEach((s, consumer) -> consumer.accept(bytes));
                        buffer.clear();
                    }
                }
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    @Override
    public void saveToCache(byte[] data) {
        cache.add(data);
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
            data = cache.poll();
        if (data == null)
            data = new byte[0];
        return data;
    }

    void addTask(String command, Consumer<byte[]> task) {
        if (handlers.containsKey(command))
            throw new IllegalArgumentException("Task list already contains " + command);
        handlers.put(command, task);
    }

    void removeTask(String command) {
        if (!handlers.containsKey(command))
            throw new IllegalArgumentException("Task list not contains this task " + command);
        handlers.remove(command);
    }

    int getSocketAmount() {
        return socketPool.size();
    }

    int getCacheSize() {
        return cache.size();
    }

    public boolean isStopped() {
        return stopped;
    }

    InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public String getHost() {
        return endpoint.getHostString();
    }

    public int getPort() {
        return endpoint.getPort();
    }

    Map<String, Consumer<byte[]>> getHandlers() {
        return handlers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleServer server = (SimpleServer) o;
        return Objects.equals(endpoint, server.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint);
    }

    @Override
    public String toString() {
        if (DEFAULT_SOCKET_POOL_SIZE > 1)
            return "SimpleServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "MultiClient Server" + "; " +
                    "Max clients=" + DEFAULT_SOCKET_POOL_SIZE +
                    '}';
        else
            return "SimpleServer{" +
                    "host=" + getEndpoint().getHostString() + "; " +
                    "port=" + getEndpoint().getPort() + "; " +
                    "SingleClient Server" +
                    '}';
    }
}

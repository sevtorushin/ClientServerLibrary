package clients;

import consoleControl.HandlersCommand;
import entity.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SimpleClient implements Runnable, Cached {
    private SocketChannel channel;
    private final InetSocketAddress endpoint;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private final Map<HandlersCommand, Consumer<byte[]>> handlers = new HashMap<>();
    private final Selector selector;
    private boolean isConnected;
    private final int TEMP_BUFFER_SIZE = 512;

    public SimpleClient(InetSocketAddress endpoint) throws IOException {
        this.endpoint = endpoint;
        this.selector = Selector.open();
    }

    public SimpleClient(String host, int port) throws IOException {
        this.endpoint = new InetSocketAddress(host, port);
        this.selector = Selector.open();
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace(); //todo логировать
        }
    }

    public void connect() throws IOException {
        channel = SocketChannel.open();
        channel.connect(endpoint);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        isConnected = true;
    }

    public void disconnect() throws IOException {
        if (channel != null) {
            selector.close();
            channel.close();
        }
        isConnected = false;
    }

    public void write(ByteBuffer srcBuf) throws IOException {
        channel.write(srcBuf);
    }

    public void processDataFromClient() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(TEMP_BUFFER_SIZE);
        try {
            while (!isConnected) {
                Thread.sleep(100);
            }
            while (isConnected) {
                int i = channel.read(buffer);
                if (i == 0) {
                    Thread.sleep(100);
                    continue;
                }
                if (i == -1) {
                    throw new IOException();
                }
                byte[] bytes = ArrayUtils.arrayTrim(buffer);
                if (!handlers.isEmpty())
                    handlers.forEach((s, consumer) -> consumer.accept(bytes));
                buffer.clear();
            }
        } catch (InterruptedException e) {
            disconnect();
        }
    }

    @Override
    public void saveToCache(byte[] data) {
        cache.add(data);
        System.out.println("Cache size = " + getCacheSize()); //todo удалить
    }

    public int read(ByteBuffer dstBuf) throws IOException {
        return channel.read(dstBuf);
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

    public void addTask(HandlersCommand command, Consumer<byte[]> task) {
        if (handlers.containsKey(command))
            throw new IllegalArgumentException("Task list already contains " + command);
        handlers.put(command, task);
    }

    public void removeTask(HandlersCommand command) {
        handlers.remove(command);
    }

    public Map<HandlersCommand, Consumer<byte[]>> getHandlers() {
        return handlers;
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public int getCacheSize() {
        return cache.size();
    }

    public boolean isStopped() {
        return !isConnected;
    }

    public SocketChannel getChannel() { //todo убрать
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleClient client = (SimpleClient) o;
        return Objects.equals(endpoint, client.endpoint) &&
                Objects.equals(channel.socket().getLocalPort(), client.channel.socket().getLocalPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, channel.socket().getLocalPort());
    }

    @Override
    public String toString() {
        return "SimpleClient{" +
                "host=" + endpoint.getHostString() + "; " +
                "port=" + endpoint.getPort() +
                '}';
    }
}

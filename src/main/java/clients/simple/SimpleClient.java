package clients.simple;

import service.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SimpleClient implements Cached<byte[]> {
    private SocketChannel channel;
    private final InetSocketAddress endpoint;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private final Map<String, Consumer<byte[]>> handlers = new ConcurrentHashMap<>();
    private final Selector selector;
    private boolean isConnected;
    private final int TEMP_BUFFER_SIZE = 512;
    private int clientId;
    static int clientCount;

    public SimpleClient(InetSocketAddress endpoint) throws IOException {
        this.endpoint = endpoint;
        this.selector = Selector.open();
    }

    public SimpleClient(String host, int port) throws IOException {
        this.endpoint = new InetSocketAddress(host, port);
        this.selector = Selector.open();
    }


    void connect() throws IOException {
        channel = SocketChannel.open();
        try {
            channel.connect(endpoint);
        } catch (ConnectException e) {
            if (e.getMessage().contains("Connection refused"))
                throw new IOException("Not running server on specified endpoint " +
                        endpoint.getHostString() + ": " + endpoint.getPort());
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        isConnected = true;
    }

    void disconnect() throws IOException {
        if (channel != null) {
            selector.close();
            channel.close();
        }
        isConnected = false;
    }

    void write(ByteBuffer srcBuf) throws IOException {
        channel.write(srcBuf);
    }

    void processDataFromClient() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(TEMP_BUFFER_SIZE);
        try {
            while (!isConnected) {
                Thread.sleep(100);
            }
            while (isConnected) {
                int i = 0;
                try {
                    i = channel.read(buffer);
                }catch (IOException e){
                    isConnected = false;
                    dropTransferTask();
//                    throw new IOException();
                }
                if (i == 0) {
                    Thread.sleep(100);
                    continue;
                }
                if (i == -1) {
                    isConnected = false;
                    dropTransferTask();
//                    throw new IOException();
                }
                byte[] bytes = ArrayUtils.toArrayAndTrim(buffer);
                if (!handlers.isEmpty())
                    handlers.forEach((s, consumer) -> consumer.accept(bytes));
                buffer.clear();
            }
        } catch (InterruptedException e) {
            disconnect();
        }
    }

    private void dropTransferTask() throws IOException {
        handlers.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains("transfer from client"))
                .findFirst()
                .orElseThrow(IOException::new)
                .getValue()
                .accept(new byte[0]);
    }

    @Override
    public void saveToCache(byte[] data) {
        cache.add(data);
    }

    int read(ByteBuffer dstBuf) throws IOException {
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

    Map<String, Consumer<byte[]>> getHandlers() {
        return handlers;
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

    int getCacheSize() {
        return cache.size();
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean isStopped() {
        return !isConnected;
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
                "port=" + endpoint.getPort() + "; " +
                "id=" + clientId +
                '}';
    }
}

package clients;

import entity.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClient implements Runnable, Cached {
    private SocketChannel channel;
    private final InetSocketAddress endpoint;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private boolean isConnected;
    private boolean cached;
    private final int TEMP_BUFFER_SIZE = 512;

    public SimpleClient(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    public SimpleClient(String host, int port) {
        this.endpoint = new InetSocketAddress(host, port);
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
        isConnected = true;
    }

    public void disconnect() throws IOException {
        if (channel != null)
            channel.close();
        isConnected = false;
        cached = false;
    }

    public void write(ByteBuffer srcBuf) throws IOException {
        channel.write(srcBuf);
    }

    public void startCaching() throws IOException {
        cached = true;
        ByteBuffer buffer = ByteBuffer.allocate(TEMP_BUFFER_SIZE);
        while (isConnected && cached) {
            read(buffer);
            byte[] bytes = ArrayUtils.arrayTrim(buffer);
            saveToCache(bytes);
//                        System.out.println(Arrays.toString(bytes));
            System.out.println("Cache size = " + getCacheSize());
            buffer.clear();
        }
        cached = false;
    }

    @Override
    public void saveToCache(byte[] data) {
        cache.add(data);
    }

    public void read(ByteBuffer dstBuf) throws IOException {
        channel.read(dstBuf);
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

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public int getCacheSize() {
        return cache.size();
    }

    public boolean isStopped() {
        return !isConnected;
    }

    public boolean isCached() {
        return cached;
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

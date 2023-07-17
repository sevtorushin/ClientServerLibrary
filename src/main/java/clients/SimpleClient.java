package clients;

import entity.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClient implements Runnable, Cached {
    private SocketChannel channel;
    private final InetSocketAddress endpoint;
    private final LinkedBlockingQueue<byte[]> cache = new LinkedBlockingQueue<>();
    private boolean isConnected;

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
            e.printStackTrace();
        }
    }

    public void connect() throws IOException {
        try {
            channel = SocketChannel.open();
//            channel.configureBlocking(false);
            channel.connect(endpoint);
            isConnected = true;
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + channel.socket().getInetAddress().getHostAddress());
        } catch (ConnectException e) {
            if (e.getMessage().contains("timed out")) {
                System.err.println("Connection to server " + channel.socket().getInetAddress().getHostAddress() + " timed out");
            }
        } catch (IOException e) {
            if (e.getMessage().equals("Connection refused: connect"))
                System.err.println("The server is not running on the specified endpoint " + channel.socket().getPort());
            else throw new IOException();
        }
    }

    public void disconnect() {
        try {
            channel.close();
            isConnected = false;
            System.out.println("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(ByteBuffer srcBuf) {
        try {
            channel.write(srcBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCaching() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(512); //todo вынести емкость в константу
        while (isConnected) {
            read(buffer);
            byte[] bytes = ArrayUtils.arrayTrim(buffer);
            saveToCache(bytes);
                        System.out.println(Arrays.toString(bytes));
                        System.out.println("Cache size = " + getCacheSize());
            buffer.clear();
        }
        System.out.println("Reading completed");
    }

    @Override
    public void saveToCache(byte[] data) {
        try {
            cache.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    LinkedBlockingQueue<byte[]> getCache() {
        return cache;
    }

    public int getCacheSize() {
        return cache.size();
    }

    public boolean isStopped() {
        return !isConnected;
    }

    @Override
    public String toString() {
        return "SimpleClient{" +
                "host=" + endpoint.getHostString() + "; " +
                "port=" + endpoint.getPort() +
                '}';
    }
}

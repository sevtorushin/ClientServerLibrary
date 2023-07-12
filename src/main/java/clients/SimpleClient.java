package clients;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClient {
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

    public void connect() {
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

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public LinkedBlockingQueue<byte[]> getCache() {
        return cache;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public String toString() {
        return "SimpleClient{" +
                "host=" + endpoint.getHostString() + "; " +
                "port=" + endpoint.getPort() +
                '}';
    }
}

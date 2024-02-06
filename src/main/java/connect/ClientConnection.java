package connect;

import exceptions.ConnectClientException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

@ToString(exclude = {"isConnected", "lock", "reconnectPeriod"})
public abstract class ClientConnection implements TCPConnection, Reconnectable, Transmitter, AutoCloseable {
    volatile boolean isConnected;
    private final ReentrantLock lock = new ReentrantLock();
    InetSocketAddress endpoint;
    @Getter @Setter
    private long reconnectPeriod = 5000;

    protected abstract boolean con() throws IOException;

    public boolean isConnected(){
        return isConnected;
    }

    @Override
    public boolean connect() throws IOException {
        if (isConnected) {
            throw new IllegalStateException("Client already connected");
        }
        try {
            con();
        } catch (IOException e) {
            if (e.getMessage().contains("Connection refused")) {
                isConnected = false;
                System.err.println("Not running server on specified endpoint " +
                        endpoint.getHostString() + ": " + endpoint.getPort());
            } else throw new IOException(e.getCause());
            reconnect();
        }
        return isConnected;
    }

    @Override
    public void reconnect() {
        new Thread(() -> {
            lock.tryLock();
            while (!isConnected) {
                System.out.println("Reconnect...");
                try {
                    con();
                    System.out.println("Connected");
                } catch (IOException e) {
                    System.out.println("Connection failed");
                    try {
                        Thread.sleep(reconnectPeriod);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            lock.unlock();
        }).start();
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }
}

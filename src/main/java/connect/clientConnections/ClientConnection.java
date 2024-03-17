package connect.clientConnections;

import connect.Reconnectable;
import connect.TCPConnection;
import connect.Transmitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.ReadProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@ToString(exclude = {"isConnected", "lock", "reconnectPeriod", "reconnectionMode"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ClientConnection implements TCPConnection, Reconnectable, Transmitter<ByteBuffer>, AutoCloseable {
    volatile boolean isConnected;
    private final ReentrantLock lock = new ReentrantLock();
    @Getter
    @EqualsAndHashCode.Include
    InetSocketAddress endpoint;
    @Getter
    @Setter
    private long reconnectPeriod;
    @Getter
    @Setter
    private boolean reconnectionMode;

    public ClientConnection() {
        ReadProperties properties = ReadProperties.getInstance();
        reconnectPeriod = Long.parseLong(properties.getValue("connection.reconnectPeriod"));
    }

    public ClientConnection(boolean reconnectionMode) {
        ReadProperties properties = ReadProperties.getInstance();
        reconnectPeriod = Long.parseLong(properties.getValue("connection.reconnectPeriod"));
        this.reconnectionMode = reconnectionMode;
    }

    private static final Logger log = LogManager.getLogger(ClientConnection.class.getSimpleName());

    protected abstract boolean connect0() throws IOException;

    protected abstract int read0(ByteBuffer buffer) throws IOException;

    protected abstract void write0(ByteBuffer buffer) throws IOException;

    public abstract int getLocalPort();

    public abstract int getRemotePort();

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean connect() throws IOException {
        if (isConnected) {
            log.warn(String.format("Client already connected to endpoint %s:%d",
                    endpoint.getHostString(), endpoint.getPort()));
            throw new IllegalStateException("Client already connected");
        }
        try {
            if (connect0()) {
                log.info(String.format("Connected to server endpoint %s:%d",
                        endpoint.getHostString(), endpoint.getPort()));
            } else {
                log.warn(String.format("Not connected to server endpoint %s:%d",
                        endpoint.getHostString(), endpoint.getPort()));
            }
        } catch (IOException e) {
            if (e.getMessage().contains("Connection refused")) {
                isConnected = false;
                log.warn(String.format("Not running server on specified endpoint %s:%d",
                        endpoint.getHostString(), endpoint.getPort()));
                reconnect();
            } else {
                throw new IOException(e.getCause());
            }
        }
        return isConnected;
    }

    @Override
    public void reconnect() {
        if (!reconnectionMode)
            return;
        Thread reconnectThread = new Thread(() -> {
            lock.tryLock();
            while (!isConnected) {
                log.info(String.format("Reconnect to server endpoint %s:%d",
                        endpoint.getHostString(), endpoint.getPort()));
                try {
                    if (connect0()) {
                        log.info(String.format("Connected to server endpoint %s:%d",
                                endpoint.getHostString(), endpoint.getPort()));
                    }
                } catch (IOException e) {
                    log.warn(String.format("Connection to server endpoint %s:%d failed",
                            endpoint.getHostString(), endpoint.getPort()));
                    try {
                        Thread.sleep(reconnectPeriod);
                    } catch (InterruptedException ie) {
                        log.error("This thread interrupted. Reconnect error");
                    }
                }
            }
            lock.unlock();
        });
        reconnectThread.setName("ReconnectThread");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        if (!isConnected) {
            log.trace(String.format("Reading impossible from %s:%d/%d, client is not connected",
                    endpoint.getHostString(), getRemotePort(), getLocalPort()));
            throw new IOException("Client is not connected");
        }
        int bytes;
        try {
            buffer.clear();
            bytes = read0(buffer);
            if (bytes == -1)
                throw new IOException("Channel reached end of stream");
            else
                log.trace(String.format("Reading successful from %s:%d/%d",
                        endpoint.getHostString(), getRemotePort(), getLocalPort()));
        } catch (IOException e) {
            handleReadWriteException();
            throw new IOException(e);
        }
        return bytes;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if (!isConnected) {
            log.trace(String.format("Writing impossible to %s:%d/%d, client is not connected",
                    endpoint.getHostString(), getLocalPort(), getRemotePort()));
            throw new IOException("Client is not connected");
        }
        try {
            write0(buffer);
            log.trace(String.format("Writing successful to %s:%d/%d",
                    endpoint.getHostString(), getLocalPort(), getRemotePort()));
        } catch (IOException e) {
            handleReadWriteException();
            throw new IOException(e);
        }
    }

    private void handleReadWriteException() {
        try {
            log.warn(String.format("Client %s:%d/%d disconnected",
                    endpoint.getHostString(), getLocalPort(), getRemotePort()));
            disconnect();
        } catch (IOException ioe) {
            log.error(String.format("Closing of socket error for client %s", this), ioe);
        }
        reconnect();
    }

    @Override
    public void close() throws IOException {
        if (disconnect())
            log.info(String.format("Disconnect from server endpoint %s:%d",
                    endpoint.getHostString(), endpoint.getPort()));
    }
}

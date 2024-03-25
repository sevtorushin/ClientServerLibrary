package servers.another;

import clients.another.Client;
import clients.another.ExtendedClient;
import connection.serverConnections.ServerConnection;
import connection.serverConnections.ServerSocketChannelConnection;
import entity.Net;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.ReadProperties;
import service.containers.AbstractNetEntityPool;
import service.containers.ExtendedClientPool;
import utils.ConnectionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Server implements Runnable, Net {
    @Getter
    @Setter
    @ToString.Include
    @EqualsAndHashCode.Include
    private String name;
    @Getter
    @Setter
    @ToString.Include
    @EqualsAndHashCode.Include
    private Object id;
    private static int serverCount = 0;
    @Getter
    private boolean stopped = true;
    @ToString.Include
    @EqualsAndHashCode.Include
    private ServerConnection connection;
    @Getter
    private final AbstractNetEntityPool<Object, Client> clientPool;
    @Getter
    @Setter
    private boolean checkClients;
    private final ExecutorService executor;
    private final ReadProperties propertiesReader = ReadProperties.getInstance();

    private static final Logger log = LogManager.getLogger(Server.class.getSimpleName());

    public Server(Integer port) throws IOException {
        this.connection = new ServerSocketChannelConnection(port, false);
        this.clientPool = new ExtendedClientPool();
        this.executor = Executors.newCachedThreadPool();
        this.name = getClass().getSimpleName();
        this.id = ++serverCount;
    }

    public Server(Integer port, boolean checkClients) throws IOException {
        this(port);
        this.checkClients = checkClients;
    }

    private void init() throws IOException {
        if (connection.isClosed()) {
            this.connection = new ServerSocketChannelConnection(getLocalPort(), false);
        }
        if (checkClients)
            executor.submit(this::checkAliveClient);
    }

    @Override
    public void run() {
        log.info(String.format("%s is running", this));
        while (!stopped) {
            SocketChannel clientSocket;
            try {
                clientSocket = connection.accept();
                if (clientSocket != null) {
                    connectClient(clientSocket);
                }
            } catch (IOException e) {
                log.trace("Fail of 'accept' method. ServerConnection is closed", e);
            }
        }
    }

    /**
     *
     */
    private void checkAliveClient() {
        log.debug("Check connections for connected clients started");
        long checkPeriod = Long.parseLong(propertiesReader.getValue("server.checkingClientPeriod"));
        while (!stopped) {
            try {
                List<Client> clientList = clientPool.getAll();
                clientList.forEach(client -> {
                    boolean isAlive = ConnectionUtils.isAliveConnection(client.getClientConnection());
                    if (!isAlive) {
                        clientPool.remove(client);
                        log.debug(String.format("%s removed from pool", client));
                    }
                });
                Thread.sleep(checkPeriod);
            } catch (InterruptedException e) {
                log.debug("Thread is interrupted", e);
            }
        }
    }


    public synchronized void start() {
        if (!stopped)
            return;
        stopped = false;
        try {
            init();
        } catch (IOException e) {
            log.error("Error of initialize ServerSocketChannelConnection", e);
            return;
        }
        executor.submit(this);
    }

    public synchronized void stop() throws IOException {
        if (stopped)
            return;
        if (!clientPool.removeAll()) {
            log.warn(String.format("Failed to remove all clients from the pool. %s not stopped", this));
            return;
        }
        stopped = true;
        connection.close();
        log.info(String.format("%s stopped", this));
    }

    private void connectClient(SocketChannel clientSocket) {
        Client connectedClient = null;
        try {
            clientSocket.configureBlocking(false);
            connectedClient = new ExtendedClient(clientSocket);
            if (clientPool.addNew(connectedClient)) {
                connectedClient.connect();
                log.debug(String.format("%s added to pool, and connected", connectedClient));
            } else
                log.warn(String.format("%s not added to pool", connectedClient));
        } catch (IOException e) {
            log.warn(String.format("Error switching %s to non-blocking mode", clientSocket));
        }
    }

    public int getLocalPort() {
        return connection.getPort();
    }

    public int receiveMessage(@NonNull Client client, @NonNull ByteBuffer buffer) throws IOException {
        int bytes;
        Client clientFromPool = clientPool.get(client.getId());
        if (clientFromPool == null)
            throw new NoSuchElementException(String.format("Client with specified ID %s is missed", client.getId()));
        try {
            bytes = client.receiveMessage(buffer);
            log.trace(String.format("Reading successful from %s", client));
        } catch (IOException e) {
            if (!checkClients) {
                if (clientPool.remove(client))
                    log.debug(String.format("%s removed from pool", client));
                else log.debug(String.format("%s fail finalize", client));
            }
            log.debug("Exception in reading time. May be client disconnected", e);
            throw new IOException(e);
        }
        return bytes;
    }

    public void sendMessage(@NonNull Client client, @NonNull ByteBuffer message) throws IOException {
        Client clientFromPool = clientPool.get(client.getId());
        if (clientFromPool == null)
            throw new NoSuchElementException(String.format("Client with specified ID %s is missed", client.getId()));
        try {
            client.sendMessage(message);
            log.trace(String.format("Writing successful to %s", client));
        } catch (IOException e) {
            if (!checkClients) {
                if (clientPool.remove(client))
                    log.debug(String.format("%s removed from pool", client));
                else log.debug(String.format("%s fail finalize", client));
            }
            log.debug("Exception in writing time. May be client disconnected", e);
            throw new IOException(e);
        }
    }
}

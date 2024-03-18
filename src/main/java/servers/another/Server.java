package servers.another;

import clients.another.Client;
import clients.another.ExtendedClient;
import connect.clientConnections.ClientConnection;
import connect.serverConnections.ServerConnection;
import connect.serverConnections.ServerSocketChannelConnection;
import entity.Net;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.DefaultClientManager;
import service.ReadProperties;
import service.containers.AbstractNetEntityPool;
import service.containers.ByteBufferHandlerContainer;
import service.containers.ClientPool;
import service.containers.ExtendedClientPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ToString(exclude = {"stopped", "clientPool", "executor", "propertiesReader"})
@EqualsAndHashCode(exclude = {"stopped", "clientPool", "executor", "propertiesReader"})
public class Server implements Runnable, Net {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Object id;
    private static int serverCount = 0;
    @Getter
    private boolean stopped;
    private final ServerConnection connection;
    @Getter
    private final AbstractNetEntityPool<Object, Client> clientPool;
    private final ExecutorService executor;
    private final ReadProperties propertiesReader = ReadProperties.getInstance();

    private static final Logger log = LogManager.getLogger(Server.class.getSimpleName());

    public Server(Integer port) throws IOException {
        this.connection = new ServerSocketChannelConnection(port, true);
        this.clientPool = new ExtendedClientPool();
        this.executor = Executors.newCachedThreadPool();
        this.name = getClass().getSimpleName();
        this.id = ++serverCount;
    }

    @Override
    public void run() {
        log.info(String.format("%s is running", this));
        executor.submit(this::checkAliveClient);
        while (!stopped) {
            SocketChannel clientSocket;
            try {
                clientSocket = connection.accept();
                if (clientSocket != null) {
                    connectClient(clientSocket);
                }
            } catch (IOException e) {
                log.error("Error of 'accept' method", e);
            }
        }
    }

    private void checkAliveClient() {
        log.debug("Check connections for connected clients started");
        long checkPeriod = Long.parseLong(propertiesReader.getValue("server.checkingClientTime"));
        ByteBuffer buffer = ByteBuffer.wrap("\r\n".getBytes());
        buffer.position(2);
        while (!stopped) {
            try {
                List<Client> clientList = clientPool.getAll();
                clientList.forEach(client -> {
                    try {
                        client.sendMessage(buffer);
                    } catch (IOException e) {
                        if (!client.isConnected()) {
                            clientPool.remove(client);
                            log.debug(String.format("%s removed from pool", client));
                        } else
                            log.warn(String.format("Failed checking for %s. %s is still connected", client, client));
                    }
                });
                Thread.sleep(checkPeriod);
            } catch (InterruptedException e) {
                log.debug("Thread is interrupted", e);
            }
        }
    }


    public void start() {
        executor.submit(this);
    }

    public void stop() throws IOException {
        if (!clientPool.removeAll()) {
            log.warn(String.format("Failed to remove all clients from the pool. %s not stopped", this));
            return;
        }
        stopped = true;
        connection.close();
    }

    private Client connectClient(SocketChannel clientSocket) {
        Client connectedClient = null;
        try {
            clientSocket.configureBlocking(false);
            connectedClient = new ExtendedClient(clientSocket);
            if (clientPool.addNew(connectedClient))
                connectedClient.connect();
            else
                log.warn(String.format("%s not added to pool", connectedClient));
        } catch (IOException e) {
            log.warn(String.format("Error switching %s to non-blocking mode", clientSocket));
        }
        return connectedClient;
    }

    public int getLocalPort() {
        return connection.getPort();
    }

    public int receiveMessage(@NonNull Client client, @NonNull ByteBuffer buffer) throws IOException {
        return client.receiveMessage(buffer);
    }

    public void sendMessage(@NonNull Client client, @NonNull ByteBuffer message) throws IOException {
        client.sendMessage(message);
    }
}

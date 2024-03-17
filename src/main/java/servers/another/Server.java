package servers.another;

import clients.another.Client;
import clients.another.ExtendedClient;
import connect.serverConnections.ServerConnection;
import connect.serverConnections.ServerSocketChannelConnection;
import entity.Net;
import lombok.*;
import service.DefaultClientManager;
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

@ToString(exclude = {"stopped", "clientPool", "executor"})
@EqualsAndHashCode(exclude = {"stopped", "clientPool", "executor"})
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

    public Server(Integer port) throws IOException {
        this.connection = new ServerSocketChannelConnection(port, true);
        this.clientPool = new ExtendedClientPool();
        this.executor = Executors.newCachedThreadPool();
        this.name = getClass().getSimpleName();
        this.id = ++serverCount;
    }

    @Override
    public void run() {
        executor.submit(this::checkAliveClient);
        while (!stopped) {
            SocketChannel clientSocket = null;
            try {
                clientSocket = connection.accept();
                if (clientSocket != null) {
                    connectClient(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAliveClient() { //todo мб в отдельный класс?
        ByteBuffer buffer = ByteBuffer.wrap("\r\n".getBytes());
        buffer.position(2);
        while (!stopped) {
            try {
                List<Client> clientList = clientPool.getAll();
                clientList.forEach(client -> {
                    try {
                        client.sendMessage(buffer);
                    } catch (IOException e) {
                        if (!client.isConnected())
                            clientPool.remove(client);
                    }
                });
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void start() {
        executor.submit(this);
    }

    public void stop() throws IOException {
        stopped = true;
        clientPool.removeAll();
        connection.close();
    }

    private Client connectClient(SocketChannel clientSocket) {
        Client connectedClient = null;
        try {
            clientSocket.configureBlocking(false);
            connectedClient = new ExtendedClient(clientSocket);
            if (clientPool.addNew(connectedClient))
                connectedClient.connect();
        } catch (IOException e) {
            e.printStackTrace();
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

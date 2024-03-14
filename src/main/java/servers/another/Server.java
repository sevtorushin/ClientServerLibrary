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
        this.connection = new ServerSocketChannelConnection(port);
        this.clientPool = new ClientPool();
        this.executor = Executors.newCachedThreadPool();
        this.name = getClass().getSimpleName();
        this.id = ++serverCount;
    }

    @Override
    public void run() {
        executor.submit(this::checkAliveClient);
        while (!isStopped()) {
            try {
                SocketChannel clientSocket = connection.accept();
                if (clientSocket != null) {
                    connectClient(clientSocket);
                }
                Thread.sleep(500);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAliveClient() { //todo мб в отдельный класс?
        while (!stopped) {
            try {
                List<Client> clientList = clientPool.getAll();
                clientList.forEach(client -> {
                    client.sendMessage(ByteBuffer.wrap("\r\n".getBytes()));
                    if (!client.isConnected())
                        clientPool.remove(client);
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
        Client connectedClient = new ExtendedClient(clientSocket);
        if (clientPool.addNew(connectedClient));
            connectedClient.connect();
        return connectedClient;
    }

    public int getLocalPort(){
        return connection.getPort();
    }

    public ByteBuffer receiveMessage(@NonNull Client client) {
        return client.receiveMessage();
    }

    public void sendMessage(Client client, ByteBuffer message){
        client.sendMessage(message);
    }
}

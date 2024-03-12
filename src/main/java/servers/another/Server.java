package servers.another;

import clients.another.Client;
import connect.serverConnections.ServerConnection;
import connect.serverConnections.ServerSocketChannelConnection;
import entity.Net;
import lombok.*;
import service.DefaultClientManager;
import service.containers.ByteBufferHandlerContainer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ToString(exclude = {"stopped", "clientManager", "executor"})
@EqualsAndHashCode(exclude = {"stopped", "clientManager", "executor"})
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
    private final DefaultClientManager clientManager;
    ExecutorService executor;

    public Server(Integer port) throws IOException {
        this.connection = new ServerSocketChannelConnection(port);
        this.clientManager = new DefaultClientManager();
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
                List<Client> clientList = clientManager.getAllNetEntities();
                clientList.forEach(client -> {
                    client.sendMessage(ByteBuffer.wrap("\r\n".getBytes()));
                    if (!client.isConnected())
                        clientManager.removeNetEntity(client);
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
        clientManager.removeAllNetEntities();
        connection.close();
    }

    private Client connectClient(SocketChannel clientSocket) {
        Client connectedClient = clientManager.createClient(clientSocket, Client.class);
        if (clientManager.addNetEntity(connectedClient, new ByteBufferHandlerContainer<>()))
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

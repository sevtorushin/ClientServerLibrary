package servers.another;

import clients.another.Client;
import connect.serverConnections.ServerConnection;
import connect.serverConnections.ServerSocketChannelConnection;
import entity.Net;
import lombok.Getter;
import service.DefaultClientManager;
import service.containers.ByteBufferHandlerContainer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable, Net {
    private final ServerConnection connection;
    @Getter
    private final DefaultClientManager clientManager;
    @Getter
    private boolean stopped;
    ExecutorService executor;

    public Server(int port) throws IOException {
        this.connection = new ServerSocketChannelConnection(port);
        this.clientManager = new DefaultClientManager();
        this.executor = Executors.newCachedThreadPool();
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
}

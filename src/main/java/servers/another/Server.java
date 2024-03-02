package servers.another;

import clients.another.Client;
import entity.Net;
import lombok.Getter;
import service.DefaultClientManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

public class Server implements Runnable, Net {

    private final ServerSocketChannel serverSocketChannel;
    @Getter
    private final DefaultClientManager clientManager;
    private boolean stopped;

    public Server(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.clientManager = new DefaultClientManager();
    }

    @Override
    public void run() {
        while (!isStopped()) {
            try {
                SocketChannel clientSocket = serverSocketChannel.accept();
                if (clientSocket != null) {
                    Client client = connectClient(clientSocket);
                    System.out.println(client);
                }
                Thread.sleep(500);
                checkAliveSocket();
                clientManager.getAllNetEntities().forEach(client -> System.out.println(client.isConnected()));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAliveSocket() {
        getAllClients().forEach(client -> {
            if(!client.isConnected())
                clientManager.removeNetEntity(client);
            else {
                client.sendMessage(ByteBuffer.wrap(new byte[]{0}));
            }
        });
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() throws IOException {
        stopped = true;
        disconnectAllClients();
        serverSocketChannel.close();
    }

    private Client connectClient(SocketChannel clientSocket) {
        Client clientTest = clientManager.createClient(clientSocket, Client.class);
//        if (clientManager.addNewClient(clientTest))
//            clientTest.connect();
        return clientTest;
    }

    public boolean disconnectClient(Client client) {
        if (client == null)
            return false;
        return clientManager.removeNetEntity(client);
    }

    public boolean disconnectAllClients() {
        return clientManager.removeAllNetEntities();
    }

    public List<Client> getAllClients() {
        return clientManager.getAllNetEntities();
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getLocalPort(){
        return serverSocketChannel.socket().getLocalPort();
    }
}

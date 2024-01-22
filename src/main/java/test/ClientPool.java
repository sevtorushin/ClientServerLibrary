package test;

import clients.another.ClientTest;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool {
    private final LinkedBlockingQueue<ClientTest> clientPool;
    private int DEFAULT_SOCKET_POOL_SIZE;

    public ClientPool() {
        this.clientPool = new LinkedBlockingQueue<>();
        this.DEFAULT_SOCKET_POOL_SIZE = 10;
    }

    public boolean addNewClient(ClientTest client){
        return clientPool.offer(client);
    }

    public boolean removeClient(ClientTest client) throws IOException {
        client.disconnect();
        return clientPool.remove(client);
    }

    public boolean removeAllClients() throws IOException {
        for (ClientTest client : clientPool) {
            client.disconnect();
            clientPool.remove(client);
        }
        return clientPool.isEmpty();
    }

    public List<ClientTest> getAllClients(){
        return new ArrayList<>(clientPool);
    }






    public ClientTest createClient(SocketChannel clientSocket) {
        return new ClientTest(clientSocket);
    }
}

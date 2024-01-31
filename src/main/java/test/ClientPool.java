package test;

import clients.another.Client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool {
    private final LinkedBlockingQueue<Client> clientPool;
    private int DEFAULT_SOCKET_POOL_SIZE;

    public ClientPool() {
        this.clientPool = new LinkedBlockingQueue<>();
        this.DEFAULT_SOCKET_POOL_SIZE = 10;
    }

    public boolean addNewClient(Client client){
        return clientPool.offer(client);
    }

    public boolean removeClient(Client client) throws IOException {
        client.disconnect();
        return clientPool.remove(client);
    }

    public boolean removeAllClients() throws IOException {
        for (Client client : clientPool) {
            client.disconnect();
            clientPool.remove(client);
        }
        return clientPool.isEmpty();
    }

    public List<Client> getAllClients(){
        return new ArrayList<>(clientPool);
    }






    public Client createClient(SocketChannel clientSocket) {
        return new Client(clientSocket);
    }
}

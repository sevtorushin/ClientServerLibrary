package service;

import clients.another.Client;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool {
    private final LinkedBlockingQueue<Client> clientPool;
    private int DEFAULT_SOCKET_POOL_SIZE;

    public ClientPool() {
        this.DEFAULT_SOCKET_POOL_SIZE = 1;
        this.clientPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
    }

    public ClientPool(int DEFAULT_SOCKET_POOL_SIZE) {
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
        this.clientPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
    }

    public boolean addNewClient(Client client) {
        return clientPool.offer(client);
    }

    public boolean removeClient(Client client) {
        if (client.disconnect())
            return clientPool.remove(client);
        else {
            System.err.println(String.format("Client %s disconnect error", client));
            return false;
        }
    }

    public boolean removeAllClients() {
        for (Client client : clientPool) {
            if (client.disconnect())
                clientPool.remove(client);
            else{
                System.err.println(String.format("Client %s disconnect error", client));
            }
        }
        return clientPool.isEmpty();
    }

    public List<Client> getAllClients() {
        return new ArrayList<>(clientPool);
    }


    public Client createClient(SocketChannel clientSocket) {
        return new Client(clientSocket);
    }
}

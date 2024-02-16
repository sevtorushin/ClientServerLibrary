package service;

import clients.another.Client;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientManager { //todo добавить конструкторы
    private final ClientPool clientPool;
    private final Map<Client, HandlerManager<String, ByteBuffer>> clientsTasks;

    public ClientManager() {
        this.clientPool = new ClientPool();
        this.clientsTasks = new HashMap<>();
    }

    public boolean addNewClient(Client client) {
        if (clientPool.addNewClient(client)) {
            clientsTasks.put(client, new ByteBufferHandlerManager<>());
            return true;
        } else return false;
    }

    public boolean removeClient(Client client) {
        if (clientPool.removeClient(client)) {
            clientsTasks.remove(client);
            return true;
        } else return false;
    }

    public boolean removeAllClients() {
        if (clientPool.removeAllClients()) {
            clientsTasks.clear();
            return true;
        } else return false;
    }

    public List<Client> getAllClients() {
        return clientPool.getAllClients();
    }

    public Client createClient(SocketChannel clientSocket, Class<? extends Client> clientClass) {
        Client client = null;
        try {
            client = clientClass.getConstructor(SocketChannel.class).newInstance(clientSocket);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return client;
    }

    //--------------------------------------------------------------------

    public Client getClient(int port) {
        return null;
    }

    public void addHandlerForClient(Client client, MessageHandler<ByteBuffer> handler) {
    }


}

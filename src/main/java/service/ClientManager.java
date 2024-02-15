package service;

import clients.another.Client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

public class ClientManager { //todo добавить конструкторы
    private ClientPool clientPool;
    private Map<Client, HandlerManager<String, ByteBuffer>> clientsTasks;

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


    public Client createClient(SocketChannel clientSocket) {
        return clientPool.createClient(clientSocket);
    }

    //--------------------------------------------------------------------

    public Client getClient(int port){
        return null;
    }

    //todo добавить equals&hashCode для Client
}

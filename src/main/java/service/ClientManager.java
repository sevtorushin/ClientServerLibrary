package service;

import clients.another.Client;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ClientManager {
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

    public Client createClient(String host, int port, Class<? extends Client> clientClass) {
        Client client = null;
        try {
            client = clientClass.getConstructor(String.class, int.class).newInstance(host, port);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return client;
    }

    public Client getClient(int localPort) {
        return clientPool.getAllClients().stream()
                .filter(client -> client.getClientConnection().getLocalPort() == localPort)
                .findFirst()
                .orElse(null);
    }

    //---------------------------------------------------------------

    public HandlerManager<String, ByteBuffer> getHandlerManager(Client client) {
        return clientsTasks.get(client);
    }

    public boolean addHandler(Client client, String taskIdentifier, MessageHandler<ByteBuffer> handler) {
        HandlerManager<String, ByteBuffer> manager = clientsTasks.get(client);
        if (manager != null) {
            boolean isSuccess = manager.addHandler(taskIdentifier, handler);
            if (isSuccess)
                return true;
            else {
                System.err.println(String.format("Handler with specified identifier already contains for %s", client));
                return false;
            }
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public boolean removeHandler(Client client, String taskIdentifier) {
        HandlerManager<String, ByteBuffer> manager = getHandlerManager(client);
        if (manager != null) {
            boolean isSuccess = manager.removeHandler(taskIdentifier);
            if (isSuccess)
                return true;
            else {
                System.err.println(String.format("Handler with specified identifier is missed for %s", client));
                return false;
            }
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public void removeAllHandlers(Client client) {
        HandlerManager<String, ByteBuffer> manager = getHandlerManager(client);
        if (manager != null) {
            manager.removeAllHandlers();
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public List<String> getAllHandlers(Client client) {
        HandlerManager<String, ByteBuffer> manager = getHandlerManager(client);
        if (manager != null) {
            return manager.getALLHandlers();
        } else throw new NoSuchElementException("Specified client is missed");
    }


}

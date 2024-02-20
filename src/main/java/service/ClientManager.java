package service;

import clients.another.Client;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ClientManager {
    private final ClientPool clientPool;
    private final Map<Client, AbstractHandlerContainer<String, ByteBuffer>> clientsTasks;

    public ClientManager() {
        this.clientPool = new ClientPool();
        this.clientsTasks = new HashMap<>();
    }

    public boolean addNewClient(Client client) {
        if (clientPool.addNew(client)) {
            clientsTasks.put(client, new ByteBufferHandlerContainer<>());
            return true;
        } else return false;
    }

    public boolean removeClient(Client client) {
        if (clientPool.remove(client)) {
            clientsTasks.remove(client);
            return true;
        } else return false;
    }

    public boolean removeAllClients() {
        if (clientPool.removeAll()) {
            clientsTasks.clear();
            return true;
        } else return false;
    }

    public List<Client> getAllClients() {
        return clientPool.getAll();
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
        return clientPool.get(localPort);
    }

    //---------------------------------------------------------------

    public AbstractHandlerContainer<String, ByteBuffer> getHandlerContainer(Client client) {
        return clientsTasks.get(client);
    }

    public boolean addHandler(Client client, IdentifiableMessageHandler<String, ByteBuffer> handler) {
        AbstractHandlerContainer<String, ByteBuffer> container = clientsTasks.get(client);
        if (container != null) {
            boolean isSuccess = container.addNew(handler);
            if (isSuccess)
                return true;
            else {
                System.err.println(String.format("Handler with specified identifier already contains for %s", client));
                return false;
            }
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public boolean removeHandler(Client client, String taskIdentifier) {
        AbstractHandlerContainer<String, ByteBuffer> container = getHandlerContainer(client);
        if (container != null) {
            boolean isSuccess = container.removeFromId(taskIdentifier);
            if (isSuccess)
                return true;
            else {
                System.err.println(String.format("Handler with specified identifier is missed for %s", client));
                return false;
            }
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public void removeAllHandlers(Client client) {
        AbstractHandlerContainer<String, ByteBuffer> container = getHandlerContainer(client);
        if (container != null) {
            container.removeAll();
        } else throw new NoSuchElementException("Specified client is missed");
    }

    public List<String> getAllHandlers(Client client) {
        AbstractHandlerContainer<String, ByteBuffer> container = getHandlerContainer(client);
        if (container != null) {
            return container.getAll().stream().map(IdentifiableMessageHandler::getIdentifier).collect(Collectors.toList());
        } else throw new NoSuchElementException("Specified client is missed");
    }


}

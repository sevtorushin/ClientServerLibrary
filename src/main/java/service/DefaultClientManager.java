package service;

import clients.another.Client;
import service.containers.ClientPool;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DefaultClientManager extends NetEntityManager<Client, ByteBuffer> {

    public DefaultClientManager() {
        super(new ClientPool());
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

    public Client getClient(Integer localPort) {
        return ((ClientPool) entityPool).getOnLocalPort(localPort);
    }
}

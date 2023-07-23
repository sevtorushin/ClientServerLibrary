package consoleControl;

import clients.SimpleClient;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClientController {
    private static SimpleClientController controller;
    private final LinkedBlockingQueue<SimpleClient> clients = new LinkedBlockingQueue<>();


    private SimpleClientController() {
    }

    public static SimpleClientController getInstance() {
        if (controller == null) {
            controller = new SimpleClientController();
            return controller;
        } else return controller;
    }

    private SimpleClient getClient(int port) throws NoSuchObjectException {
        SimpleClient client = clients.stream()
                .filter(cl -> cl.getEndpoint().getPort() == port)
                .findFirst()
                .orElse(null);
        if (client == null) {
            throw new NoSuchObjectException("No such client");
        }
        return client;
    }

    public LinkedBlockingQueue<SimpleClient> getClients() {
        return clients;
    }

    public SimpleClient create(String host, int port) {
        SimpleClient client;
        client = new SimpleClient(host, port);
        clients.add(client);
        return client;
    }

    public SimpleClient stop(int port) throws IOException {
        SimpleClient client = getClient(port);
        client.disconnect();
        clients.remove(client);
        return client;
    }

    public List<SimpleClient> stopAllClients() throws IOException {
        for (SimpleClient client : clients) {
            client.disconnect();
        }
        List<SimpleClient> clientList = new ArrayList<>(clients);
        clients.clear();
        return clientList;
    }

    public List<SimpleClient> getAllClients() {
        return new ArrayList<>(clients);
    }

    public void read(String host, int port) throws IOException {
        SimpleClient client;
        client = getClient(port);
        String clientHost = client.getEndpoint().getHostString();
        if (!clientHost.equals(host)) {
            throw new UnknownHostException("Unknown host " + clientHost);
        }
        try {
            client.startCaching();
        } catch (IOException e) {
            client.disconnect();
            clients.remove(client);
        }
    }
}

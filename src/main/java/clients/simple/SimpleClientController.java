package clients.simple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.NoSuchObjectException;
import java.rmi.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

public class SimpleClientController {
    private static SimpleClientController controller;
    private final LinkedBlockingQueue<SimpleClient> clients = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<SimpleClient> newClients = new LinkedBlockingQueue<>();


    private SimpleClientController() {
    }

    public static SimpleClientController getInstance() {
        if (controller == null) {
            controller = new SimpleClientController();
            return controller;
        } else return controller;
    }


    private SimpleClient getClient(String host, int port, int id) throws NoSuchObjectException {
        Predicate<SimpleClient> pred;
        if (id == 0) {
            pred = cl -> cl.getEndpoint().getPort() == port && cl.getEndpoint().getHostString().equals(host);
        } else {
            pred = cl -> cl.getEndpoint().getPort() == port && cl.getEndpoint().getHostString().equals(host) &&
                    cl.getClientId() == id;
        }
        SimpleClient client = clients.stream()
                .filter(pred)
                .findFirst()
                .orElse(null);
        if (client == null) {
            throw new NoSuchObjectException("No such client");
        }
        return client;
    }

    private void addToPool(SimpleClient client) {
        clients.add(client);
        newClients.add(client);
    }

    private void removeFromPool(SimpleClient client) {
        clients.remove(client);
        newClients.remove(client);
    }

    public SimpleClient getNewClient() {
        return newClients.poll();
    }

    private void startRead(String host, int port, int id) throws IOException {
        SimpleClient client = getClient(host, port, id);
        try {
            client.processDataFromClient();
        } catch (IOException e) {
            client.disconnect();
            removeFromPool(client);
        }
    }

    public SimpleClient create(String host, int port) throws IOException {
        SimpleClient client;
        client = new SimpleClient(host, port);
        client.connect();
        SimpleClient.clientCount++;
        client.setClientId(SimpleClient.clientCount);
        addToPool(client);
        new Thread(() -> {
            try {
                startRead(host, port, SimpleClient.clientCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return client;
    }

    public SimpleClient stop(String host, int port, int id) throws IOException {
        SimpleClient client = getClient(host, port, id);
        client.disconnect();
        removeFromPool(client);
        return client;
    }

    public List<SimpleClient> stopAllClients() throws IOException {
        for (SimpleClient client : clients) {
            client.disconnect();
        }
        List<SimpleClient> clientList = new ArrayList<>(clients);
        clients.clear();
        newClients.clear();
        return clientList;
    }

    public List<SimpleClient> getAllClients() {
        return new ArrayList<>(clients);
    }

    public void startCaching(String host, int port, int id) throws NoSuchObjectException {
        SimpleClient client = getClient(host, port, id);
        client.addTask("CACHE for client " + host + " " + port, client::saveToCache);
    }

    public void stopCaching(String host, int port, int id) throws NoSuchObjectException {
        SimpleClient client = getClient(host, port, id);
        client.removeTask("CACHE for client " + host + " " + port);
    }

    public void printRawReceiveData(String host, int port, int id) throws NoSuchObjectException {
        SimpleClient client = getClient(host, port, id);
        client.addTask("PRINT for client " + host + " " + port,
                bytes -> System.out.println(Arrays.toString(bytes)));
    }

    public void stopPrinting(String host, int port, int id) throws NoSuchObjectException {
        SimpleClient client = getClient(host, port, id);
        client.removeTask("PRINT for client " + host + " " + port);
    }

    public List<String> getRunnableTasks(String host, int port, int id) throws NoSuchObjectException {
        SimpleClient client = getClient(host, port, id);
        return new ArrayList<>(client.getHandlers().keySet());
    }

    public void startTransferToServer(String fromClientHost, int fromClientPort, int fromClientId,
                                      String hostAnotherServer, int portAnotherServer) throws IOException {
        SimpleClient fromClient = getClient(fromClientHost, fromClientPort, fromClientId);
        SimpleClient transferToAnotherServerClient = create(hostAnotherServer, portAnotherServer);
        fromClient.addTask("TRANSFER from client " + fromClientHost + " " + fromClientPort + " " + fromClientId +
                " to server " + hostAnotherServer + " " + portAnotherServer, bytes -> {
            try {
                if (!fromClient.isStopped()) {
                    transferToAnotherServerClient.write(ByteBuffer.wrap(bytes));
                } else {
                    stop(hostAnotherServer, portAnotherServer, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stopTransferToServer(String fromClientHost, int fromClientPort, int fromClientId,
                                     String hostAnotherServer, int portAnotherServer) throws IOException {
        SimpleClient client = getClient(fromClientHost, fromClientPort, fromClientId);
        client.removeTask("TRANSFER from client " + fromClientHost + " " + fromClientPort + " " + fromClientId +
                " to server " + hostAnotherServer + " " + portAnotherServer);
        stop(hostAnotherServer, portAnotherServer, 0);
    }
}

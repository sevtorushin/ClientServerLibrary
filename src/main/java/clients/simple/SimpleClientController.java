package clients.simple;

import consoleControl.HandlersCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public LinkedBlockingQueue<SimpleClient> getClientPool() {
        return clients;
    }

    private void startRead(int port) throws IOException {
        SimpleClient client = getClient(port);
        try {
            client.processDataFromClient();
        } catch (IOException e) {
            client.disconnect();
            clients.remove(client);
        }
    }

    public SimpleClient create(String host, int port) throws IOException {
        SimpleClient client;
        client = new SimpleClient(host, port);
        clients.add(client);
        new Thread(() -> {
            try {
                startRead(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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

    public void startCaching(String host, int port) throws IOException {
        SimpleClient client = getClient(port);
        String clientHost = client.getEndpoint().getHostString();
        if (!clientHost.equals(host)) {
            throw new UnknownHostException("Unknown host " + clientHost);
        }
        client.addTask(HandlersCommand.CACHE, client::saveToCache);
    }

    public void stopCaching(int port) throws NoSuchObjectException {
        SimpleClient client = getClient(port);
        client.removeTask(HandlersCommand.CACHE);
    }

    public void printRawReceiveData(int port) throws NoSuchObjectException {
        SimpleClient client = getClient(port);
        client.addTask(HandlersCommand.PRINT, bytes -> System.out.println(Arrays.toString(bytes)));
    }

    public void stopPrinting(int port) throws NoSuchObjectException {
        SimpleClient client = getClient(port);
        client.removeTask(HandlersCommand.PRINT);
    }

    public List<HandlersCommand> getRunnableTasks(int port) throws NoSuchObjectException {
        SimpleClient client = getClient(port);
        return new ArrayList<>(client.getHandlers().keySet());
    }

    public void startTransferToServer(int fromClientPort, String hostAnotherServer, int portAnotherServer) throws IOException {
        SimpleClient fromClient = getClient(fromClientPort);
        SimpleClient transferToAnotherServerClient = create(hostAnotherServer, portAnotherServer);
        new Thread(transferToAnotherServerClient).start();
        fromClient.addTask(HandlersCommand.TRANSFER, bytes -> {
            try {
                transferToAnotherServerClient.write(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stopTransferToServer(int fromClientPort, int toAnotherServerPort) throws IOException {
        SimpleClient client = getClient(fromClientPort);
        client.removeTask(HandlersCommand.TRANSFER);
        stop(toAnotherServerPort);
    }
}

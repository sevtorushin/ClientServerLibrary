package consoleControl;

import clients.SimpleClient;
import utils.ArrayUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.NoSuchObjectException;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClientControl extends Control {
    private LinkedBlockingQueue<SimpleClient> clients;
    private SimpleClient client;

    @Override
    public Object start() {
        String host = (String) getMapExpression().get("host");
        int port = (int) getMapExpression().get("port");
        client = new SimpleClient(host, port);
        clients.add(client);
        client.connect();
        return client;
    }

    @Override
    public Object stop() throws NoSuchObjectException {
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase())) {
            clients.forEach(SimpleClient::disconnect);
            clients.clear();
            return clients;
        } else {
            int port = (int) getMapExpression().get("port");
            client = getClient(port);
            client.disconnect();
            clients.remove(client);
            return client;
        }
    }

    @Override
    public Object remove() {
        return null;
    }

    @Override
    public Object read() {
        Runnable task = () -> {
            if (getMapExpression().get("option") == null &&
                    getMapExpression().get("host") != null &&
                    getMapExpression().get("port") != null) {
                String host = (String) getMapExpression().get("host");
                int port = (int) getMapExpression().get("port");
                try {
                    client = getClient(port);
                } catch (NoSuchObjectException e) {
                    System.err.println(e.getMessage());
                    return;
                }
                if (!client.getEndpoint().getHostString().equals(host)) {
                    System.err.println("Unknown host");
                    return;
                }
                ByteBuffer buffer = ByteBuffer.allocate(512);
                while (!client.isStopped()) {
                    try {
                        client.read(buffer);
                        byte[] bytes = ArrayUtils.arrayTrim(buffer);
//                        System.out.println(Arrays.toString(bytes));
                        client.saveToCache(bytes);
//                        System.out.println("Cache size = " + client.getCache().size());
                        buffer.clear();
                    } catch (IOException e) {
                        client.disconnect();
                        clients.remove(client);
                        break;
                    }
                }
                System.out.println("Reading completed");
            } else System.err.println("Wrong input expression");
        };
        return task;
    }

    @Override
    public Object get() throws NoSuchObjectException {
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()))
            return clients;
        else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            client = getClient(port);
        }
        else throw new NoSuchObjectException("Data is missing");
        return client;
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

    @Override
    @SuppressWarnings("unchecked")
    public void setEntity(LinkedBlockingQueue<?> clients) {
        this.clients = (LinkedBlockingQueue<SimpleClient>) clients;
    }
}

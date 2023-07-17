package consoleControl;

import clients.SimpleClient;
import entity.Cached;
import utils.ArrayUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClientController extends Controller {
    private static SimpleClientController controller;
    private LinkedBlockingQueue<SimpleClient> clients = new LinkedBlockingQueue<>();
    private final Map<String, Object> mapExpression = new HashMap<>();
//    private SimpleClient client;


    private SimpleClientController() {
    }

    public static SimpleClientController getInstance() {
        if (controller == null) {
            controller = new SimpleClientController();
            return controller;
        } else return controller;
    }

    @Override
    public Cached create() {
        SimpleClient client;
        String host = (String) getMapExpression().get("host");
        int port = (int) getMapExpression().get("port");
        client = new SimpleClient(host, port);
        clients.add(client);
        return client;
    }

    @Override
    public List<Cached> stop() throws NoSuchObjectException {
        SimpleClient client;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase())) {
            clients.forEach(SimpleClient::disconnect);
            clients.clear();
            return new ArrayList<>(clients);
        } else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            client = getClient(port);
            client.disconnect();
            clients.remove(client);
            return Collections.singletonList(client);
        } else throw new IllegalArgumentException("Wrong arguments expression");
    }

    @Override
    public List<SocketChannel> remove() {
        return null;
    }

    @Override
    public void read() {
        SimpleClient client;
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
            try {
                client.startCaching();
            } catch (IOException e) {
                client.disconnect();
                clients.remove(client);
            }
        } else System.err.println("Wrong input expression");
    }

    @Override
    public List<SimpleClient> get() throws NoSuchObjectException {
        SimpleClient client;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()))
            return new ArrayList<>(clients);
        else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            client = getClient(port);
            return Collections.singletonList(client);
        } else throw new NoSuchObjectException("Data is missing");
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

    @Override
    @SuppressWarnings("unchecked")
    public void setEntity(LinkedBlockingQueue<?> clients) {
        this.clients = (LinkedBlockingQueue<SimpleClient>) clients;
    }
}

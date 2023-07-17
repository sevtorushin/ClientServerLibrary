package consoleControl;

import entity.Cached;
import servers.SimpleServer;

import java.io.IOException;
import java.net.BindException;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleServerController extends Controller {
    private static SimpleServerController controller;
    private LinkedBlockingQueue<SimpleServer> servers = new LinkedBlockingQueue<>();
    private final Map<String, Object> mapExpression = new HashMap<>();


    private SimpleServerController() {
    }

    public static SimpleServerController getInstance() {
        if (controller == null) {
            controller = new SimpleServerController();
            return controller;
        } else return controller;
    }

    @Override
    public Cached create() throws IOException {
        SimpleServer server = null;
        int maxClients = 1;
        if (getMapExpression().get("option") != null &&
                !getMapExpression().get("option").equals("all".toLowerCase())) {
            maxClients = Integer.parseInt((String) getMapExpression().get("option"));
        }
        int port = (int) getMapExpression().get("port");
        try {
            server = new SimpleServer(port, maxClients);
            servers.add(server);
        } catch (BindException e) {
            System.err.println("Port is not available. Please use another port");
        }
        return server;
    }

    @Override
    public List<Cached> stop() throws NoSuchObjectException {
        SimpleServer server;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase())) {
            servers.forEach(SimpleServer::stopServer);
            servers.clear();
            return new ArrayList<>(servers);
        } else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
            server.stopServer();
            servers.remove(server);
            return Collections.singletonList(server);
        } else throw new IllegalArgumentException("Wrong arguments expression");
    }

    @Override
    public List<SocketChannel> remove() throws NoSuchObjectException {
        SimpleServer server;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int serverPort = (int) getMapExpression().get("port");
            server = getServer(serverPort);
            List<SocketChannel> clients = new ArrayList<>(server.getSocketPool());
            server.rejectAllSockets();
            return clients;
        } else if (getMapExpression().get("option") != null &&
                !getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int serverPort = (int) getMapExpression().get("port");
            int clientPort = Integer.parseInt((String) getMapExpression().get("option"));
            server = getServer(serverPort);
            removeClient(server, clientPort);
            return new ArrayList<>(server.getSocketPool());
        } else throw new NoSuchObjectException("Data is missing");
    }

    @Override
    public void read() {
        SimpleServer server;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            try {
                server = getServer(port);
            } catch (NoSuchObjectException e) {
                System.err.println(e.getMessage());
                return;
            }
            server.startCaching();
        } else System.err.println("Wrong input expression");
    }

    @Override
    public List<?> get() throws NoSuchObjectException {
        SimpleServer server;
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") == null)
            return new ArrayList<>(servers);
        else if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
            return new ArrayList<>(server.getSocketPool());
        } else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
            return Collections.singletonList(server);
        } else throw new NoSuchObjectException("Data is missing");
    }

    private SimpleServer getServer(int port) throws NoSuchObjectException {
        SimpleServer server = servers.stream()
                .filter(srv -> srv.getEndpoint().getPort() == port)
                .findFirst()
                .orElse(null);
        if (server == null) {
            throw new NoSuchObjectException("No such server");
        }
        return server;
    }

    private SocketChannel getClient(SimpleServer server, int port) throws NoSuchObjectException {
        SocketChannel client = server.getSocketPool().stream()
                .filter(cl -> cl.socket().getPort() == port)
                .findFirst()
                .orElse(null);
        if (client == null)
            throw new NoSuchObjectException("No such client");
        else return client;
    }

    private void removeClient(SimpleServer server, int port) {
        try {
            SocketChannel channel = getClient(server, port);
            channel.close();
            server.getSocketPool().remove(channel);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public LinkedBlockingQueue<SimpleServer> getServers() {
        return servers;
    }
}

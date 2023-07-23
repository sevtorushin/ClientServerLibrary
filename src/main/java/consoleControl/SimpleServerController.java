package consoleControl;

import servers.SimpleServer;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleServerController {
    private static SimpleServerController controller;
    private final LinkedBlockingQueue<SimpleServer> servers = new LinkedBlockingQueue<>();

    public static SimpleServerController getInstance() {
        if (controller == null) {
            controller = new SimpleServerController();
            return controller;
        } else return controller;
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

    private SocketChannel removeClient(SimpleServer server, int port) throws IOException {
        SocketChannel channel = getClient(server, port);
        channel.close();
        server.getSocketPool().remove(channel);
        return channel;
    }

    public LinkedBlockingQueue<SimpleServer> getServers() {
        return servers;
    }

    /**
     * This method creates a SimpleServer. When the specified port is busy, then the
     * method throws {@code BindException}
     *
     * @param serverPort
     * @param maxClients
     * @return
     * @throws IOException
     */
    public SimpleServer create(int serverPort, int maxClients) throws IOException {
        SimpleServer server = new SimpleServer(serverPort, maxClients);
        servers.add(server);
//            todo when the method throws BindException, then you need to handle:
//             "Port is not available. Please use another port"
        return server;
    }

    public SimpleServer stop(int serverPort) throws IOException {
        SimpleServer server = getServer(serverPort);
        server.stopServer();
        servers.remove(server);
        return server;
    }

    public List<SimpleServer> stopAllServers() throws IOException {
        List<SimpleServer> serverList = new ArrayList<>(servers);
        for ( SimpleServer server : servers) {
            server.stopServer();
        }
        servers.clear();
        return serverList;
    }

    public SocketChannel removeClient(int serverPort, int clientPort) throws IOException {
        SimpleServer server = getServer(serverPort);
        return removeClient(server, clientPort);
    }

    public List<SocketChannel> removeAllClients(int serverPort) throws IOException {
        SimpleServer server = getServer(serverPort);
        List<SocketChannel> clients = new ArrayList<>(server.getSocketPool());
        server.rejectAllSockets();
        return clients;
    }

    public List<SimpleServer> getAllServers() {
        return new ArrayList<>(servers);
    }

    public SimpleServer get(int port) throws NoSuchObjectException {
        return getServer(port);
    }

    public List<SocketChannel> getAllClients(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        return new ArrayList<>(server.getSocketPool());
    }

    public void read(int serverPort) throws IOException {
        SimpleServer server;
        try {
            server = getServer(serverPort);
        } catch (NoSuchObjectException e) {
            System.err.println(e.getMessage());
            return;
        }
        server.startCaching();
    }
}

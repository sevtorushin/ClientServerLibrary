package servers.simple;

import consoleControl.HandlersCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public LinkedBlockingQueue<SimpleServer> getServerPool() {
        return servers;
    } //todo что-то сделать, чтобы не давать ссылку на пул

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

    /**
     * This method creates a SimpleServer. When the specified port is busy, then the
     * method throws {@code IOException}
     *
     * @param serverPort
     * @param maxClients
     * @return
     * @throws IOException
     */
    public SimpleServer create(int serverPort, int maxClients) throws IOException {
        SimpleServer server = new SimpleServer(serverPort, maxClients);
        servers.add(server);
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
        for (SimpleServer server : servers) {
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

    public List<SocketChannel> getAllClients(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        return new ArrayList<>(server.getSocketPool());
    }

    public void startCaching(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        server.addTask(String.format("CACHE for server %d", serverPort), server::saveToCache); //todo Сделать форматированный вывод
    }

    public void stopCaching(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        server.removeTask("CACHE for server " + serverPort);
    }

    public void printRawReceiveData(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        server.addTask("PRINT for server " + serverPort, bytes -> System.out.println(Arrays.toString(bytes)));
    }

    public void stopPrinting(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        server.removeTask("PRINT for server " + serverPort);
    }

    public List<String> getRunnableTasks(int serverPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        return new ArrayList<>(server.getHandlers().keySet());
    }

    public void startTransferToClient(int serverPort, int clientPort) throws IOException {
        SimpleServer server = getServer(serverPort);
        SocketChannel channel = getClient(server, clientPort);
        server.addTask("TRANSFER from server " + serverPort + " to client " + clientPort,
                bytes -> {
                    try {
                        channel.write(ByteBuffer.wrap(bytes));
                    } catch (IOException e) {
                        server.removeTask("TRANSFER from server " + serverPort + " to client " + clientPort);
                    }
                });
    }

    public void stopTransferToClient(int serverPort, int clientPort) throws NoSuchObjectException {
        SimpleServer server = getServer(serverPort);
        server.removeTask("TRANSFER from server " + serverPort + " to client " + clientPort);
    }
}

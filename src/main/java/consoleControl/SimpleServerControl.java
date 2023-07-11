package consoleControl;

import servers.SimpleServer;
import utils.ArrayUtils;

import java.io.IOException;
import java.net.BindException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.Iterator;
import java.util.List;

public class SimpleServerControl extends Control {
    private List<SimpleServer> servers;
    private SimpleServer server;

    @Override
    public Object start() {
        int maxClients = 1;
        if (getMapExpression().get("option") != null &&
                !getMapExpression().get("option").equals("all".toLowerCase())){
            maxClients = Integer.parseInt((String) getMapExpression().get("option"));
        }
        int port = (int) getMapExpression().get("port");
        try {
            server = new SimpleServer(port, maxClients);
            servers.add(server);
        } catch (BindException e) {
            System.err.println("Port is not available. Please use another port");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return server;
    }

    @Override
    public Object stop() throws NoSuchObjectException {
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase())) {
            servers.forEach(srv -> srv.stopServer());
            servers.clear();
            return servers;
        } else {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
            server.stopServer();
            servers.remove(server);
            return server;
        }
    }

    @Override
    public Object remove() throws NoSuchObjectException {
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int serverPort = (int) getMapExpression().get("port");
            server = getServer(serverPort);
            server.rejectAllSockets();
        } else if (getMapExpression().get("option") != null &&
                !getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int serverPort = (int) getMapExpression().get("port");
            int clientPort = Integer.parseInt((String) getMapExpression().get("option"));
            server = getServer(serverPort);
            removeClient(clientPort);
        } else throw new NoSuchObjectException("Data is missing");
        return server.getSocketPool();
    }

    @Override
    public Object read() {
        Runnable task = () -> {
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
                ByteBuffer buffer = ByteBuffer.allocate(512);
                try {
                    while (!server.isStopped() /*&& server.getSocketAmount() != 0*/) {
                        int readyChannels = server.getSelector().selectNow();
                        if (readyChannels == 0) {
                            Thread.sleep(100);
                            continue;
                        }
                        Iterator<SelectionKey> iterator = server.getSelector().selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (server.isStopped())
                                return;
                            if (key.isReadable()) { //todo при остановке сервера поток зависает в этом месте
                                SocketChannel sc = (SocketChannel) key.channel();
                                try {
                                    if (sc.read(buffer) == -1) {
                                        server.rejectSocket(sc);
                                        break;
                                    }
                                } catch (IOException e) {
                                    server.rejectSocket(sc);
                                    break;
                                }
                                byte[] bytes = ArrayUtils.arrayTrim(buffer);
                                server.saveToCache(bytes);
//                                System.out.println(Arrays.toString(bytes));
                                System.out.println("Cache size = " + server.getCache().size());
                                buffer.clear();
                            }
                        }
                    }
                    System.out.println("Reading completed");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else System.err.println("Wrong input expression");
        };
        return task;
    }

    @Override
    public Object get() throws NoSuchObjectException {
        if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") == null)
            return servers;
        else if (getMapExpression().get("option") != null &&
                getMapExpression().get("option").equals("all".toLowerCase()) &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
            return server.getSocketPool();
        } else if (getMapExpression().get("option") == null &&
                getMapExpression().get("port") != null) {
            int port = (int) getMapExpression().get("port");
            server = getServer(port);
        } else throw new NoSuchObjectException("Data is missing");
        return server;
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

    private SocketChannel getClient(int port) throws NoSuchObjectException {
        SocketChannel client = server.getSocketPool().stream()
                .filter(cl -> cl.socket().getPort() == port)
                .findFirst()
                .orElse(null);
        if (client == null)
            throw new NoSuchObjectException("No such client");
        else return client;
    }

    private void removeClient(int port) {
        try {
            SocketChannel channel = getClient(port);
            channel.close();
            server.getSocketPool().remove(channel);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setEntity(List<?> servers) {
        this.servers = (List<SimpleServer>) servers;
    }
}

package servers;

import check.AbstractValidator;
import check.Validator;
import clients.AbstractClient;
import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Connection;
import utils.ConnectionUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServer implements Runnable {
    protected final ServerSocket serverSocket;
    private final int port;
    private final int maxNumberOfClient;
    private int cacheSize = 1_000_000;
    protected volatile BlockingQueue<AbstractClient> clientPool;
    protected volatile Map<AbstractClient, LinkedBlockingQueue<byte[][]>> cachePool = new ConcurrentHashMap<>();
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Condition condition = lock.newCondition();
    private final Validator validator;
    private volatile boolean isServerConnected = true;
    private volatile boolean isNewClientConnected = false;

    private static final Logger log = LogManager.getLogger(AbstractServer.class.getSimpleName());

    public AbstractServer(int port, AbstractValidator validator) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = 1;
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.validator = validator;
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + clientPool.size() + ", number of unique clients: " +
                cachePool.size() + ", is connect server: " + isServerConnected +
                ", is new client connected: " + isNewClientConnected);
    }

    public AbstractServer(int port, int maxNumberOfClient, AbstractValidator validator) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.validator = validator;
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + clientPool.size() + ", number of unique clients: " +
                cachePool.size() + ", is connect server: " + isServerConnected +
                ", is new client connected: " + isNewClientConnected);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Adding_New_Clients_Thread_" + Thread.currentThread().getId());
        log.info("Server started on port " + port);
        byte[] tempBuffer = new byte[512];
        Connection connection;
        while (isServerConnected) {
            System.out.println(Thread.activeCount());
            try {
                Socket clientSocket = serverSocket.accept();

                connection = new Connection();
                connection.bind(clientSocket);

                log.debug("Client " + connection.getHost() + " accepted");

                ConnectionUtils.readFromInputStreamToBuffer(connection.getInputStream(), tempBuffer);
                log.debug("Client start package read");

                if (!validate(tempBuffer)) {
                    connection.getOutputStream().write("Validation failed. Your reconnected".getBytes());
                    log.debug("Client " + connection.getHost() + " invalid. Connection will be dropped");
                    connection.close();
                    log.info("Client connection dropped successful");
                    continue;
                }
                log.debug("Client " + connection.getHost() + " is valid");

                AbstractClient client = getClient(tempBuffer);
                client.setConnection(connection);
                Arrays.fill(tempBuffer, (byte) 0);

                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    if (clientPool.offer(client)) {
                        log.debug("Client " + client.getHost() +
                                " has been added to the queue. Number of active clients: " + clientPool.size());
                        if (!addToMap(client))
                            client.getConnection().close();
                        isNewClientConnected = true;
                        log.info("Client " + client.getHost() + " connected: " + isNewClientConnected);
                        client.getOutStrm().write("Your are connected successful".getBytes());
                    } else {
                        log.info("Client connection limit exceeded");
                        client.getOutStrm().write(("Client connection limit exceeded. " +
                                "Your reconnected").getBytes());
                        client.getConnection().close();
                        log.debug("Client socket is closed: " + client.getConnection().isClosed());
                        continue;
                    }
                    condition.signal();
                } finally {
                    lock.unlock();
                    log.debug("Thread release lock");
                }
            } catch (SocketTimeoutException e) {
                log.trace("No new clients. Waiting for new clients");
            } catch (IOException e) {
                log.error(e);
            } catch (ConnectClientException e) {
                log.error(e);
            }
        }
    }

    private ServerSocket getServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.setSoTimeout(500);
            log.debug("Server socket created with parameters: port: " + serverSocket.getLocalPort() + ", timeout: " +
                    serverSocket.getSoTimeout());
        } catch (BindException e) {
            log.debug("Port is not available. Please use another port", e);
            System.exit(-1);
        } catch (IOException e) {
            log.error(e);
        }
        return serverSocket;
    }

    public void stopServer() {
        isServerConnected = false;
        log.info("Server stopped");
        System.exit(1);
    }

    protected Validator getValidator() {
        return validator;
    }

    public int cachePoolSize() {
        return cachePool.size();
    }

    public boolean isServerStopped() {
        return !isServerConnected;
    }

    public boolean isNewClientConnected() {
        return isNewClientConnected;
    }

    public void setNewClientConnected(boolean newClientConnected) {
        isNewClientConnected = newClientConnected;
    }

    public Set<AbstractClient> getCachedClients() {
        return cachePool.keySet();
    }

    public BlockingQueue<AbstractClient> getActiveClients() {
        return clientPool;
    }

    protected abstract boolean validate(byte[] data);

    protected abstract AbstractClient getClient(byte[] data);

    protected boolean addToMap(AbstractClient client) {
        if (!cachePool.containsKey(client)) {
            cachePool.put(client, new LinkedBlockingQueue<>(cacheSize));
            log.debug("Added unique client " + client.getConnection().getHost() + " to cachePool");
        } else {
            cachePool.keySet()
                    .forEach(cachedClient -> {
                        if (cachedClient.getId().equals(client.getId())) {
                            try {
                                cachedClient.getConnection().close();
                                cachedClient.setConnection(client.getConnection());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            log.debug("Client socket " + client.getConnection().getHost() + " updated");
        }
        return true;
    }
}

package servers;

import check.AbstractValidator;
import check.Validator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class AbstractServer implements Runnable {
    protected final ServerSocket serverSocket;
    private final int port;
    private final int maxNumberOfClient;
    protected volatile BlockingQueue<Socket> socketPool;
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
        this.socketPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.validator = validator;
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + socketPool.size() + ", number of unique clients: " +
                cachePool.size() + ", is connect server: " + isServerConnected +
                ", is new client connected: " + isNewClientConnected);
    }

    public AbstractServer(int port, int maxNumberOfClient, AbstractValidator validator) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.socketPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.validator = validator;
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + socketPool.size() + ", number of unique clients: " +
                cachePool.size() + ", is connect server: " + isServerConnected +
                ", is new client connected: " + isNewClientConnected);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Adding_New_Clients_Thread_" + Thread.currentThread().getId());
        log.info("Server started on port " + port);
        while (isServerConnected) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.debug("Client " + clientSocket.getInetAddress() + " accepted");

                InputStream is = clientSocket.getInputStream();
                byte[] data = new byte[512];
                is.read(data);

                if (!validate(data)) {
                    clientSocket.getOutputStream().write("Validation failed. Your reconnected".getBytes());
                    log.debug("Client " + clientSocket.getInetAddress() + " invalid. Connection will be dropped");
                    clientSocket.close();
                    log.info("Client connection dropped successful");
                    continue;
                }

                AbstractClient client = getClient(data);
                client.setPort(clientSocket.getPort());
                client.setHost(clientSocket.getInetAddress().toString());
                client.setSocket(clientSocket);

                log.debug("Client " + clientSocket.getInetAddress() + " is valid");
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    if (clientPool.offer(client)) {
                        log.debug("Client " + clientSocket.getInetAddress() +
                                " has been added to the queue. Number of active clients: " + socketPool.size());
                        if (!addToMap(client))
                            clientSocket.close();
                        isNewClientConnected = true;
                        log.info("Client " + clientSocket.getInetAddress() + " connected: " + isNewClientConnected);
                        clientSocket.getOutputStream().write("Your are connected successful".getBytes());
                    } else {
                        log.info("Client connection limit exceeded");
                        clientSocket.getOutputStream().write(("Client connection limit exceeded. " +
                                "Your reconnected").getBytes());
                        clientSocket.close();
                        log.debug("Client socket is closed: " + clientSocket.isClosed());
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
//        isInterrupt = true;   //todo не удалять. Решить проблему с read() и раскомментировать строку
        isServerConnected = false;
        log.info("Server stopped");
        System.exit(1); //todo удалить после решения проблемы с read()
    }

    protected Validator getValidator() {
        return validator;
    }

    public int cachePoolSize() {
        return cachePool.size();
    }

    public boolean isServerConnected() {
        return isServerConnected;
    }

    public boolean isNewClientConnected() {
        return isNewClientConnected;
    }

    public void setNewClientConnected(boolean newClientConnected) {
        isNewClientConnected = newClientConnected;
    }

    protected AbstractClient getSameMapSocket(Socket socket) {
        return cachePool.keySet().stream()
                .filter(client -> client.getHost().equals(socket.getInetAddress().toString().replace("/", "")))
                .findFirst().orElse(null);
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
            cachePool.put(client, new LinkedBlockingQueue<>(1_000_000)); //todo Размер кэша данных вынести в поле этого класса
            log.debug("Added unique client " + client.getHost() + " to cachePool");
        } else {
            cachePool.keySet()
                    .forEach(cl -> {
                        if (cl.getId().equals(client.getId()))
                            cl.setSocket(client.getSocket());
                    });
        }
        return true;
    }
}

package servers;

import check.AbstractValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServer implements Runnable{
    protected final ServerSocket serverSocket;
    private final int port;
    private final int maxNumberOfClient;
    protected volatile BlockingQueue<Socket> clientPool;
    protected volatile Map<String, LinkedBlockingQueue<byte[][]>> cachePool = new ConcurrentHashMap<>();
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Condition condition = lock.newCondition();
    private AbstractValidator validator;
    private volatile boolean isServerConnected = true;
    private volatile boolean isNewClientConnected = false;

    private static final Logger log = LogManager.getLogger(AbstractServer.class.getSimpleName());

    public AbstractServer(int port) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = 1;
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + clientPool.size() + ", number of unique clients: " +
                cachePool.size() + ", is connect server: " + isServerConnected +
                ", is new client connected: " + isNewClientConnected);
    }

    public AbstractServer(int port, int maxNumberOfClient) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.clientPool = new ArrayBlockingQueue<>(maxNumberOfClient);
        log.debug("Initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient +
                ", number of active clients: " + clientPool.size() + ", number of unique clients: " +
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
                if (!(validator.authenticate(clientSocket) && validator.authorize(clientSocket) &&
                        validator.verify(clientSocket))) {
                    log.debug("Client " + clientSocket.getInetAddress() +" invalid. Connection will be dropped");
                    clientSocket.close();
                    log.info("Client connection dropped successful");
                    continue;
                }
                log.debug("Client " + clientSocket.getInetAddress() + " is valid");
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    if (clientPool.offer(clientSocket)) {
                        isNewClientConnected = true;
                        log.debug("Client " + clientSocket.getInetAddress() +
                                " has been added to the queue. Number of active clients: " + clientPool.size());
                        log.info("Client " + clientSocket.getInetAddress() + " connected: " + isNewClientConnected);
                        addToMap(clientSocket);
                    } else {
                        log.info("Client connection limit exceeded");
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

    public ArrayList<Socket> getClientPool() {
        return new ArrayList<Socket>(clientPool);
    }

    protected void setValidator(AbstractValidator validator) {
        this.validator = validator;
    }

    protected AbstractValidator getValidator() {
        return validator;
    }

    public int cacheSize() {
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

    protected String getSameMapSocket(Socket socket) {
        return cachePool.keySet().stream()
                .filter(ip -> ip.equals(socket.getInetAddress().toString()))
                .findFirst().orElse(null);
    }

    public Set<String> getActiveClients() {
        return cachePool.keySet();
    }

    protected abstract void addToMap(Socket clientSocket);
}

package test;

import exceptions.DisconnectedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractReceiveSrv implements Receivable, Runnable, Cloneable {
    protected final ServerSocket serverSocket;
    private int port;
    private int maxNumberOfClient;
    private byte[] buffer;
    protected BlockingQueue<Socket> clientSockets;
    protected Map<Socket, LinkedBlockingQueue<byte[][]>> socketsCaches = new ConcurrentHashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private boolean serverConnect = true;
    private boolean isNewClientConnected = false;
    private boolean isInterrupt = false;

    private static final Logger log = LogManager.getLogger(AbstractReceiveSrv.class.getSimpleName());

    public AbstractReceiveSrv(int port, int DEFAULT_BUFFER_SIZE) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = 1;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.buffer = new byte[DEFAULT_BUFFER_SIZE];
        log.debug("initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient + ", buffer size: " + buffer.length +
                ", number of active clients: " + clientSockets.size() + ", history of unique clients: " + socketsCaches.size() +
                ", is connect server: " + serverConnect + ", is new client connected: " + isNewClientConnected);
    }

    public AbstractReceiveSrv(int port, int maxNumberOfClient, int DEFAULT_BUFFER_SIZE) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
        this.buffer = new byte[DEFAULT_BUFFER_SIZE];
        log.debug("initialize: port: " + port + ", maxNumberOfClient: " + maxNumberOfClient + ", buffer size: " + buffer.length +
                ", number of active clients: " + clientSockets.size() + ", history of unique clients: " + socketsCaches.size() +
                ", is connect server: " + serverConnect + ", is new client connected: " + isNewClientConnected);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Adding_New_Clients_Thread_" + Thread.activeCount());
        log.info("Server started on port " + port);
        while (serverConnect) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.debug("Client " + clientSocket.getInetAddress() + " accepted. Number of active clients: " + clientSockets.size());
                if (!isValidClient(clientSocket)) {
                    log.info("Unknown client " + clientSocket.getInetAddress() + " connection attempt...");
                    clientSocket.close();
                    log.info("Unknown client connection dropped successful");
                    continue;
                }
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    if (clientSockets.offer(clientSocket)) {
                        log.debug("Client " + clientSocket.getInetAddress() + " has been added to queue");
                        addToMap(clientSocket);
                        isNewClientConnected = true;
                        log.info("Client " + clientSocket.getInetAddress() + " connected " + isNewClientConnected);
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
                if (isInterrupt) {
                    log.debug("Thread is interrupted");
                    return;
                }
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
        log.info("Server stopped");
        System.exit(1); //todo удалить после решения проблемы с read()
    }

    @Override
    public byte[] receiveBytes(Socket clientSocket) {
        byte[][] bytes = null;
        try {
            bytes = socketsCaches.get(getSameMapSocket(clientSocket)).take();
            log.debug("Data retrieves from cache. Buffer size: " + buffer.length);
        } catch (InterruptedException e) {
            log.debug("Thread interrupted while waiting for data from cache", e);
        }
        return bytes[1];

    }

    public void startCaching() {
        new Thread(() -> {
            Thread.currentThread().setName("Start_Caching_Thread_" + Thread.activeCount());
            while (true) {
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    while (!isNewClientConnected) {
                        try {
                            log.trace("No new clients. Waiting for new clients");
                            condition.await(500, TimeUnit.MILLISECONDS);
                            if (isInterrupt) {
                                log.debug("Thread is interrupted");
                                return;
                            }
                        } catch (InterruptedException e) {
                            log.debug("Thread interrupted while waiting for new clients", e);
                        }
                    }
                    Set<Socket> uniqueSocketsContainer = new HashSet<>();
                    for (Socket clientSocket : clientSockets) {
                        if (uniqueSocketsContainer.add(clientSocket) && clientSocket != null) {
                            log.debug("New unique client " + clientSocket.getInetAddress() + " is connected");
                            writeToQueueFromSocket(clientSocket);
                            isNewClientConnected = false;
                        }
                    }
                    if (isInterrupt) {
                        log.debug("Thread is interrupted");
                        return;
                    }
                } finally {
                    lock.unlock();
                    log.debug("Thread release lock");
                }
            }
        }).start();
    }

    private void writeToQueueFromSocket(Socket socket) {
        new Thread(() -> {
            Thread.currentThread().setName("Write_Data_Thread_" + Thread.activeCount());
            Socket mapSocket = getSameMapSocket(socket);
            LinkedBlockingQueue<byte[][]> queue = socketsCaches.get(mapSocket);
            try (InputStream is = socket.getInputStream()) {
                while (!isClosedInputStream(is)) { //todo метод блокирует поток и сервер не может быть остановлен при вызове мотода stopServer(), который прерывает поток методом interrupt()
                    LocalDateTime dateTime = LocalDateTime.now();
                    String s = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    queue.offer(new byte[][]{s.getBytes(), buffer});    //todo обработать условие когда offer отдает false (если очередь переполнена)
                    log.debug("Data added to socket " + mapSocket.getInetAddress() + " cache");
                    if (isInterrupt) {
                        log.debug("Thread is interrupted");
                        return;
                    }
                }
                throw new DisconnectedException("Client " + socket.getInetAddress() + " disconnected");
            } catch (IOException e) {
                log.debug(e);
            } catch (DisconnectedException e) {
                log.info(e.getMessage());
            } finally {
                try {
                    socket.close();//todo закрывать снаружи?
                    log.debug("Client socket " + socket.getInetAddress() + " is closed: " + socket.isClosed());
                    clientSockets.remove(socket);
                    log.debug("Client " + socket.getInetAddress() + " removed from queue");
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    protected Socket getSameMapSocket(Socket socket) {
        return socketsCaches.keySet().stream()
                .filter(mapSocket -> mapSocket.getInetAddress().equals(socket.getInetAddress()))
                .findFirst().orElse(null);
    }

    public Set<Socket> getActiveClients() {
        return socketsCaches.keySet();
    }

    protected boolean isClosedInputStream(InputStream is) {
        try {
            if (is.read(buffer) == -1)
                return true;
        } catch (IOException e) {
            log.debug(e);
        }
        return false;
    }

    protected byte[] getBuffer() {
        return buffer;
    }

    protected abstract boolean isValidClient(Socket clientSocket);

    protected abstract void addToMap(Socket socket);

}

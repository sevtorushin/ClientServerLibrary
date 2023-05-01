package test;

import exceptions.DisconnectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SIBMonitorSrv extends AbstractReceiveSrv {
    private final ServerSocket serverSocket;
    private int port;
    private int maxNumberOfClient;
    private BlockingQueue<Socket> clientSockets;
    private Map<Socket, LinkedBlockingQueue<byte[][]>> socketsCaches = new ConcurrentHashMap<>();
    private int DEFAULT_BUFFER_SIZE = 22;
    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private boolean isNewClientConnected = false;
    private boolean serverConnect = true;
    private boolean isInterrupt = false;

    public SIBMonitorSrv(int port) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = 1;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
    }

    public SIBMonitorSrv(int port, int maxNumberOfClient) {
        this.port = port;
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
    }

    @Override
    public void run() {
        System.out.println("Server started on port " + port);
        while (serverConnect) {
            try {
                Socket clientSocket = serverSocket.accept();
                if (!isValidClient(clientSocket)) {
                    System.out.println("Unknown client " + clientSocket.getInetAddress() + " connection attempt...");
                    clientSocket.close();
                    System.out.println("Unknown client connection dropped successful");
                    continue;
                }
                try {
                    lock.lock();
                    if (clientSockets.offer(clientSocket)) {
                        addToMap(clientSocket);
                        isNewClientConnected = true;
                        System.out.println("SIB Monitor client " + clientSocket.getInetAddress() + " connected");
                    } else {
                        System.err.println("Client connection limit exceeded");
                        clientSocket.close();
                        continue;
                    }
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            } catch (SocketTimeoutException e) {
                if (isInterrupt)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer(){
//        isInterrupt = true;   //todo не удалять. Решить проблему с read() и раскомментировать строку
        System.out.println("Server stopped");
        System.exit(1); //todo удалить после решения проблемы с read()
    }

    private boolean isValidClient(Socket clientSocket) throws IOException {
        if (clientSockets.isEmpty())
            if ((byte) clientSocket.getInputStream().read() == -56)
                return true;
        for (Socket socket : clientSockets) {
            if (socket != null && !socket.getInetAddress().equals(clientSocket.getInetAddress()) &&
                    (byte) clientSocket.getInputStream().read() == -56)
                return true;
            System.err.println("Starting a second Sib Monitor client from the " +
                    clientSocket.getInetAddress() + " ip address was rejected");
        }
        return false;
    }

    @Override
    public byte[] receiveBytes(Socket clientSocket) {
        byte[][] bytes = null;
        try {
            bytes = socketsCaches.get(getSameMapSocket(clientSocket)).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bytes[1];

    }

    public void startCaching() {
        new Thread(() -> {
            Set<Socket> uniqueSocketsContainer = new HashSet<>();
            while (true) {
                try {
                    lock.lock();
                    while (!isNewClientConnected) {
                        try {
                            condition.await(500, TimeUnit.MILLISECONDS);
                            if (isInterrupt)
                                return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    for (Socket clientSocket : clientSockets) {
                        if (uniqueSocketsContainer.add(clientSocket) && clientSocket != null) {
                            writeToQueueFromSocket(clientSocket);
                            isNewClientConnected = false;
                        }
                    }
                } finally {
                    lock.unlock();
                    if (isInterrupt)
                        return;
                }
            }
        }).start();
    }

    public Set<Socket> getActiveClients() {
        return socketsCaches.keySet();
    }

    private void writeToQueueFromSocket(Socket socket) {
        new Thread(() -> {
            LinkedBlockingQueue<byte[][]> queue = socketsCaches.get(getSameMapSocket(socket));
            try (InputStream is = socket.getInputStream()) {
                while (is.read(buffer) != -1) { //todo метод блокирует поток и сервер не может быть остановлен
                    if (buffer[0] == 4)
                        break;
                    LocalDateTime dateTime = LocalDateTime.now();
                    String s = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    queue.offer(new byte[][]{s.getBytes(), buffer});    //todo обработать условие когда offer отдает false (если очередь переполнена)
                    if (isInterrupt)
                        return;
                }
                throw new DisconnectedException("Client " + socket.getInetAddress() + " disconnected");
            } catch (IOException | DisconnectedException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    socket.close();//todo закрывать снаружи?
                    clientSockets.remove(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private Socket getNextSocket() {
//        Socket socket = null;
//        try {
//            lock.lock();
//            while (clientSockets.isEmpty())
//                condition.await();
//            socket = clientSockets.take();
//            clientSockets.offer(socket);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
//        return socket;
//    }

    private Socket getSameMapSocket(Socket socket) {
        return socketsCaches.keySet().stream()
                .filter(mapSocket -> mapSocket.getInetAddress().equals(socket.getInetAddress()))
                .findFirst().orElse(null);
    }

    private void addToMap(Socket socket) {
        if (getSameMapSocket(socket) == null)
            socketsCaches.put(socket, new LinkedBlockingQueue<>());
    }

    private ServerSocket getServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.setSoTimeout(500);
        } catch (BindException e) {
            System.err.println("Port is not available. Please use another port" + e);
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }
}

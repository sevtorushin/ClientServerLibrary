package test;

import exceptions.DisconnectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SIBMonitorSrv extends AbstractReceiveSrv {
    private final ServerSocket serverSocket;
    private int maxNumberOfClient;
    private BlockingQueue<Socket> clientSockets;
    private Queue<SibNode> data = new LinkedBlockingDeque<>();
    private int DEFAULT_BUFFER_SIZE = 44;
    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    public SIBMonitorSrv(int port) {
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = 1;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
    }

    public SIBMonitorSrv(int port, int maxNumberOfClient) {
        this.serverSocket = getServerSocket(port);
        this.maxNumberOfClient = maxNumberOfClient;
        this.clientSockets = new ArrayBlockingQueue<>(maxNumberOfClient);
    }

    private static class SibNode implements Comparable<SibNode> {
        private LocalDateTime localDateTime;
        private byte[] bytes;

        public SibNode(LocalDateTime localDateTime, byte[] bytes) {
            this.localDateTime = localDateTime;
            this.bytes = bytes;
        }

        @Override
        public int compareTo(SibNode node) {
            return this.localDateTime.compareTo(node.localDateTime);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket sibMonitorSocket = serverSocket.accept();
                sibMonitorSocket.getInputStream().read(buffer);
                if (buffer[0] != -56) {
                    System.out.println("Unknown client connection attempt...");
                    sibMonitorSocket.close();
                    System.out.println("Unknown client connection dropped successful");
                    continue;
                }
                if (clientSockets.size() == maxNumberOfClient) {
                    System.err.println("Client connection limit exceeded");
                    continue;
                }
                clientSockets.add(sibMonitorSocket);
                System.out.println("SIB Monitor client connected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] receiveBytes(Socket clientSocket) throws DisconnectedException {
        try (InputStream is = clientSocket.getInputStream()) {
            if (is.read(buffer) == -1) {
                throw new DisconnectedException("Client disconnected");
            }
        } catch (IOException e) {
            throw new DisconnectedException("Client disconnected");
        }
        return buffer;
    }

    public void receiveBytes() {
        new Thread(() -> {
            while (true) {
                try (Socket clientSocket = clientSockets.take();
                     InputStream is = clientSocket.getInputStream()) {
                    while (is.read(buffer) != -1) {
                        if (buffer[0] == 4)
                            break;
                        data.offer(new SibNode(LocalDateTime.now(), buffer));
                    }
                    System.out.println("Client disconnected");
                } catch (IOException | InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }

    public Socket getSocket() {
        Socket socket = null;
        try {
            socket = clientSockets.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public byte[] getValue() throws IOException {
        if (!data.isEmpty())
            return data.poll().bytes;
        else throw new IOException("No data");
    }

    public LocalDateTime getLocalDateTime() throws IOException {
        if (!data.isEmpty())
            return data.poll().localDateTime;
        else throw new IOException("No data");
    }

    private ServerSocket getServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
        } catch (BindException e) {
            System.err.println("Port is not available. Please use another port" + e);
            System.exit(-1);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }
}

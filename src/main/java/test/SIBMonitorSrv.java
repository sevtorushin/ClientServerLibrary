package test;

import exceptions.DisconnectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SIBMonitorSrv extends AbstractReceiveSrv {
    private final ServerSocket serverSocket;
    private BlockingQueue<Socket> clientSockets = new ArrayBlockingQueue<>(2);
    private Queue<SibNode> data = new PriorityQueue<>();
    private ReentrantLock lock = new ReentrantLock(true);
    Condition condition = lock.newCondition();
    private byte[] buffer = new byte[44];
    private boolean isNewConnectionCreated = false;

    public SIBMonitorSrv(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private class SibNode implements Comparable<SibNode>{
        private LocalDateTime localDateTime;
        private byte[] bytes;

        public SibNode(LocalDateTime localDateTime, byte[] bytes) {
            this.localDateTime = localDateTime;
            this.bytes = bytes;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public int compareTo(SibNode node) {
            return this.localDateTime.compareTo(node.localDateTime);
        }
    }

    public void setNewConnectionCreated(boolean newConnectionCreated) {
        lock.lock();
        try {
            while (!isNewConnectionCreated) {
                condition.await();
            }
            isNewConnectionCreated = newConnectionCreated;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public boolean isNewConnectionCreated() {
        return isNewConnectionCreated;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            Socket sibMonitorSocket = serverSocket.accept();
            sibMonitorSocket.getInputStream().read(buffer);
            if (buffer[0] == -56) {
                System.out.println("SIB Monitor client connected");
            } else {
                System.out.println("Unknown client connection attempt...");
                sibMonitorSocket.close();
                System.out.println("Unknown client connection dropped successful");
                isNewConnectionCreated = true;
                condition.signal();
                return;
            }
            clientSockets.add(sibMonitorSocket);
            isNewConnectionCreated = true;
            condition.signal();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket sibMonitorSocket = serverSocket.accept();
                    sibMonitorSocket.getInputStream().read(buffer);
                    if (buffer[0] == -56) {
                        System.out.println("SIB Monitor client connected");
                    } else {
                        System.out.println("Unknown client connection attempt...");
                        sibMonitorSocket.close();
                        System.out.println("Unknown client connection dropped successful");
                        continue;
                    }
                    clientSockets.add(sibMonitorSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public byte[] receiveBytes(Socket clientSocket) throws DisconnectedException {
        try (InputStream is = clientSocket.getInputStream()){
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
                                throw new DisconnectedException("Client disconnected");
                            data.offer(new SibNode(LocalDateTime.now(), buffer));
                        }
                        System.err.println("Client disconnected");
                    } catch (IOException | InterruptedException | DisconnectedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }).start();
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public BlockingQueue<Socket> getClientSockets() {
        return clientSockets;
    }

    public Queue<SibNode> getData() {
        return data;
    }
}

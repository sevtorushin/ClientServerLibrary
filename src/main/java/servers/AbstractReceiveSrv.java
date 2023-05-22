package servers;

import check.AbstractValidator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public abstract class AbstractReceiveSrv extends AbstractServer implements Receivable {
    private final byte[] buffer;
    private static final Logger log = LogManager.getLogger(AbstractReceiveSrv.class.getSimpleName());

    public AbstractReceiveSrv(int port, int DEFAULT_BUFFER_SIZE, AbstractValidator validator) {
        super(port, validator);
        this.buffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    public AbstractReceiveSrv(int port, int maxNumberOfClient, int DEFAULT_BUFFER_SIZE, AbstractValidator validator) {
        super(port, maxNumberOfClient, validator);
        this.buffer = new byte[DEFAULT_BUFFER_SIZE];
    }


    @Override
    public byte[] receiveBytes(String source) {
        lock.lock();
        try {
            while (cachePool.isEmpty()){
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    log.debug("Thread was interrupted while waiting for new clients", e);
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
        Set<AbstractClient> clients = cachePool.keySet();
        Object[] idClients = clients.stream()
                .filter(cl -> cl.getId().equals(source)).toArray();
        Object[] ipClients = clients.stream()
                .filter(cl -> cl.getHost().equals(source)).toArray();
        if (idClients.length == 1) {
            return receiveBytes((AbstractClient) idClients[0]);
        } else if (ipClients.length == 1)
            return receiveBytes((AbstractClient) ipClients[0]);
        else {
            log.info("Specified client is missing");
            throw new IllegalArgumentException();
        }
    }

    public byte[] receiveBytes(AbstractClient source) {
        byte[][] bytes = null;
        try {
            bytes = cachePool.get(source).take();
            log.debug("Data retrieves from socket cache " + source);
        } catch (InterruptedException e) {
            log.debug("Thread was interrupted while waiting for data from cache", e);
        }
        return bytes[1];
    }

    public void startCaching() {
        new Thread(() -> {
            Thread.currentThread().setName("Start_Caching_Thread_" + Thread.currentThread().getId());
            Set<Socket> uniqueClientContainer = new HashSet<>();
            while (true) {
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    while (!isNewClientConnected()) {
                        try {
                            condition.await(500, TimeUnit.MILLISECONDS);
                            if (!isServerConnected()) {
                                log.debug("Thread is interrupted");
                                return;
                            }
                        } catch (InterruptedException e) {
                            log.debug("Thread was interrupted while waiting for new clients", e);
                            return;
                        }
                    }
                    for (Socket clientSocket : clientPool) {
                        if (uniqueClientContainer.add(clientSocket) && clientSocket != null) {
                            log.debug("New unique client " + clientSocket.getInetAddress() + " is connected");
                            writeToQueueFromSocket(clientSocket);
                            setNewClientConnected(false);
                        }
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
            Thread.currentThread().setName("Write_Data_Thread_" + Thread.currentThread().getId());
            AbstractClient client = getSameMapSocket(socket);
            LinkedBlockingQueue<byte[][]> cache = cachePool.get(client);
            try (InputStream is = socket.getInputStream()) {
                while (!isClosedInputStream(is)) { //todo метод блокирует поток и сервер не может быть остановлен при вызове мотода stopServer(), который прерывает поток методом interrupt()
                    LocalDateTime dateTime = LocalDateTime.now();
                    String s = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    if (!cache.offer(new byte[][]{s.getBytes(), buffer})) {    //todo обработать условие когда offer отдает false (если очередь переполнена)
                        log.error("Local cache full");
                        continue;
                    }
                    log.debug("Data added to socket cache " + client.getId());
                    if (!isServerConnected()) {
                        log.debug("Thread is interrupted");
                        return;
                    }
                }
                log.info("Client " + socket.getInetAddress() + " disconnected");
            } catch (IOException e) {
                log.debug("Exception in writeToQueueFromSocket method", e);
                log.info("May be SibReceiver app client " + socket.getInetAddress() + " was closed");
            } finally {
                try {
                    socket.close();
                    log.debug("Client socket " + socket.getInetAddress() + " is closed: " + socket.isClosed());
                    clientPool.remove(socket);
                    log.debug("Client " + socket.getInetAddress() + " removed from pool");
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    protected boolean isClosedInputStream(InputStream is) throws IOException {
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
}

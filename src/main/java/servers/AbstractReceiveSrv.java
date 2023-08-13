package servers;

import check.AbstractValidator;
import clients.AbstractClient;
import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ArrayUtils;
import utils.ConnectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

public abstract class AbstractReceiveSrv extends AbstractServer implements Receivable {
    private byte[] buffer;
    private static final int TIMEOUT_RESET_CLIENT = 30_000;
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
            while (cachePool.isEmpty()) {
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
                .filter(cl -> cl.getConnection().getHost().equals(source)).toArray();
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
            while (true) {
                try {
                    lock.lock();
                    log.debug("Thread takes lock");
                    while (!isNewClientConnected()) {
                        try {
                            condition.await(500, TimeUnit.MILLISECONDS);
                            if (isServerStopped()) {
                                log.debug("Thread is interrupted");
                                return;
                            }
                        } catch (InterruptedException e) {
                            log.debug("Thread was interrupted while waiting for new clients", e);
                            return;
                        }
                    }
                    for (AbstractClient client : getCachedClients()) {
                        if (!client.isWriteToCache()) {
                            log.debug("New unique client " + client.getConnection().getHost() + " is connected");
                            writeToQueueFromSocket(client);
                            log.debug("Client " + client.getConnection().getHost() + " sent for recording");
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

    private void writeToQueueFromSocket(AbstractClient client) {
        new Thread(() -> {
            Thread.currentThread().setName("Write_Data_Thread_" + Thread.currentThread().getId());
            LinkedBlockingQueue<byte[][]> cache = cachePool.get(client);
            byte[] buffer = new byte[512];
            client.setWriteToCache(true);
            try {
                readFromClientToBuffer(client, buffer);
                while (getValidator().verify(buffer)) {
                    log.debug("Client package " + client.getConnection().getHost() + " received");
                    LocalDateTime dateTime = LocalDateTime.now();
                    String s = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    byte[] b = ArrayUtils.arrayTrim(buffer);
                    if (!cache.offer(new byte[][]{s.getBytes(), b})) {
                        log.error("Local cache full");
                        continue;
                    }
                    log.debug("Data added to client cache " + client.getId());
                    Arrays.fill(buffer, (byte) 0);
                    readFromClientToBuffer(client, buffer);
                    if (isServerStopped()) {
                        log.debug("Thread is interrupted");
                        return;
                    }
                }
            } catch (ConnectClientException e) {
                if (e.getMessage().contains("unreachable"))
                    log.info("Connection with  " + client.getConnection().getHost() + " was lost");
            } finally {
                try {
                    log.info("Client " + client.getConnection().getHost() + " disconnected");
                    if (client.getInpStrm() != null)
                        client.getInpStrm().close();
                    if (client.getConnection().getSocket() != null && !client.getConnection().getSocket().isClosed())
                        client.getConnection().getSocket().close();
                    log.debug("Client socket " + client.getConnection().getHost() +
                            " is closed: " + client.getConnection().getSocket().isClosed());
                    client.setWriteToCache(false);
                    clientPool.remove(client);
                    log.debug("Client " + client.getConnection().getHost() + " removed from pool");
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }).start();
    }

    protected static void readFromClientToBuffer(AbstractClient client, byte[] buffer) throws ConnectClientException {
        try {
            boolean isReached = ConnectionUtils.isReachedHost(client.getHost());
            if (isReached) {
                ConnectionUtils.readFromInputStreamToBuffer(client.getInpStrm(), buffer, TIMEOUT_RESET_CLIENT);
            } else
                throw new ConnectClientException("Client host " + client.getConnection().getHost() + " is unreachable");
        } catch (Exception e) {
            if (e.getMessage().contains("Connection reset"))
                throw new ConnectClientException("Client " + client.getConnection().getHost() + " disconnected");
        }
    }

    protected byte[] getBuffer() {
        return buffer;
    }
}

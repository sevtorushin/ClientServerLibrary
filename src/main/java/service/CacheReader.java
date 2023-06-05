package service;

import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.AbstractReceiveSrv;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class CacheReader {
    private final AbstractReceiveSrv server;
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Condition condition = lock.newCondition();
    private static final Logger log = LogManager.getLogger(CacheReader.class.getSimpleName());

    public CacheReader(AbstractReceiveSrv server) {
        this.server = server;
    }

    public void read(Consumer<? super byte[]> processor) {
        new Thread(() -> {
            Set<AbstractClient> clients;
            int clientCount = 0;
            while (true) {
                lock.lock();
                while (true) {
                    if (server.getActiveClients().size() > clientCount) {
                        if (server.getCachedClients().containsAll(server.getActiveClients())) {
                            clientCount = server.getActiveClients().size();
                            break;
                        }
                    } else clientCount = server.getActiveClients().size();
                    try {
                        condition.await(500, TimeUnit.MILLISECONDS);
                        if (server.isServerStopped())
                            return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                lock.unlock();
                clients = server.getCachedClients();
                for (AbstractClient client : clients) {
                    if (!client.isReadFromCache()) {
                        new Thread(() -> {
                            client.setReadFromCache(true);
                            byte[] bytes;
                            while (client.isWriteToCache()) {
                                bytes = server.receiveBytes(client);
                                processor.accept(bytes);
                                Arrays.fill(bytes, (byte) 0);
                            }
                            client.setReadFromCache(false);
                        }).start();
                    }
                }
            }
        }).start();
    }
}

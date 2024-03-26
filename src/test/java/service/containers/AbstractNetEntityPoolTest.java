package service.containers;

import clients.another.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import servers.another.Server;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AbstractNetEntityPoolTest {
    private static ClientPool clientPool;
    private static Client client;

    @BeforeAll
    static void init() {
        clientPool = new ClientPool(2);
        client = new Client("127.0.0.1", 7000);
    }

    @AfterEach
    void clearPool() {
        clientPool.removeAll();
    }

    @Test
    void addNewOverCapacity() {
        boolean isSuccessful;
        clientPool.addNew(client);
        clientPool.addNew(new Client("127.0.0.1", 5500));
        isSuccessful = clientPool.addNew(new Client("127.0.0.1", 1100));
        assertFalse(isSuccessful);
    }

    @Test
    void removeConnectedEntity() {
        runServer(7000);
        client.connect();
        clientPool.addNew(client);
        boolean isSuccessful = clientPool.remove(client);
        assertTrue(clientPool.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeNotConnectedEntity() {
        clientPool.addNew(client);
        boolean isSuccessful = clientPool.remove(client);
        assertTrue(clientPool.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeAnotherEntity() {
        clientPool.addNew(client);
        Client secondClient = new Client("127.0.0.1", 5500);
        boolean isSuccessful = clientPool.remove(secondClient);
        assertFalse(clientPool.entityStorage.isEmpty());
        assertFalse(isSuccessful);
    }

    @Test
    void removeAll() {
        clientPool.addNew(client);
        clientPool.addNew(new Client("127.0.0.1", 5500));
        assertTrue(clientPool.removeAll());
    }

    private void runServer(int port){
        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
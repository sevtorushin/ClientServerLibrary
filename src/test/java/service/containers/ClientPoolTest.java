package service.containers;

import clients.another.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import servers.another.Server;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ClientPoolTest {
    private static ClientPool clientPool;
    private static Client client;
    private static Server server;

    @BeforeAll
    static void beforeAll() {
        clientPool = new ClientPool();
        client = new Client("127.0.0.1", 7000);
    }



    @BeforeEach
    void setUp() {
        try {
            server = new Server(7000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        clientPool.removeAll();
        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Stream<Arguments> methodClientProvider() {
        return Stream.of(
                Arguments.arguments(client),
                Arguments.arguments(new Client("127.0.0.1", 5500)),
                Arguments.arguments((Object) null)
        );
    }

    @Test
    void getLocalPortForConnectedClientTest() {
        client.connect();
        clientPool.addNew(client);
        Integer expectedLocalPort = client.getClientConnection().getLocalPort();
        Integer actualLocalPort = clientPool.getLocalPort(client);
        assertEquals(expectedLocalPort, actualLocalPort);
    }

    @Test
    void getOnLocalPortForConnectedClientTest() {
        client.connect();
        clientPool.addNew(client);
        Integer localPort = clientPool.getLocalPort(client);
        assertEquals(client, clientPool.getOnLocalPort(localPort));
    }

    @Test
    void getOnLocalPortForDisconnectedClientTest() {
        clientPool.addNew(client);
        Integer localPort = clientPool.getLocalPort(client);
        assertEquals(client, clientPool.getOnLocalPort(localPort));
    }

    @Test
    void finalizeTest() {
        client.connect();
        assertTrue(client.isConnected());
        clientPool.finalizeEntity(client);
        assertFalse(client.isConnected());
    }
}
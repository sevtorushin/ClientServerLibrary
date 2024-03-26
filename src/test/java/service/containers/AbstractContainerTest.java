package service.containers;

import clients.another.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AbstractContainerTest {

    private static ClientPool clientPool;
    private static Client client;

    @BeforeAll
    static void init() {
        clientPool = new ClientPool();
        client = new Client("127.0.0.1", 7000);
    }

    @AfterEach
    void clearPool() {
        clientPool.removeAll();
    }

    static Stream<Arguments> methodClientProvider() {
        return Stream.of(
                Arguments.arguments(client),
                Arguments.arguments(new Client("127.0.0.1", 5500)),
                Arguments.arguments((Object) null)
        );
    }

    @ParameterizedTest
    @MethodSource("methodClientProvider")
    void addNew(Client client) {
        if (client != null) {
            boolean isSuccessful = clientPool.addNew(client);
            assertFalse(clientPool.entityStorage.isEmpty());
            assertTrue(isSuccessful);
        } else {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> clientPool.addNew(client));
            assertEquals("netEntity is marked non-null but is null", exception.getMessage());
        }
    }

    @Test
    void addNewEqualEntity() {
        boolean isSuccessful;
        Client secondClient = new Client("127.0.0.1", 7000);
        secondClient.setId(1);
        clientPool.addNew(client);
        isSuccessful = clientPool.addNew(secondClient);
        assertFalse(isSuccessful);
    }

    @ParameterizedTest
    @MethodSource("methodClientProvider")
    void remove(Client client) {
        if (client != null) {
            clientPool.addNew(client);
            boolean isSuccessful = clientPool.remove(client);
            assertTrue(clientPool.entityStorage.isEmpty());
            assertTrue(isSuccessful);
        } else {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> clientPool.remove(client));
            assertEquals("netEntity is marked non-null but is null", exception.getMessage());
        }
    }

    @Test
    void removeFromEmptyPool() {
        boolean isSuccessful = clientPool.remove(client);
        assertFalse(isSuccessful);
    }

    @ParameterizedTest
    @MethodSource("methodClientProvider")
    void removeAnother() {
        clientPool.addNew(client);
        boolean isSuccessful = clientPool.remove(new Client("1.1.1.1", 11));
        assertFalse(clientPool.entityStorage.isEmpty());
        assertFalse(isSuccessful);
    }

    @ParameterizedTest
    @MethodSource("methodClientProvider")
    void removeForId(Client client) {
        if (client != null) {
            Object id = client.getId();
            clientPool.addNew(client);
            boolean isSuccessful = clientPool.removeForID(id);
            assertTrue(clientPool.entityStorage.isEmpty());
            assertTrue(isSuccessful);
        } else {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> clientPool.removeForID(null));
            assertEquals("id is marked non-null but is null", exception.getMessage());
        }
    }

    @Test
    void removeForAnotherId() {
        Object id = client.getId();
        Object anotherId = new Client("1.1.1.1", 11).getId();
        clientPool.addNew(client);
        boolean isSuccessful = clientPool.removeForID(anotherId);
        assertFalse(clientPool.entityStorage.isEmpty());
        assertFalse(isSuccessful);
    }

    @Test
    void removeAll() {
        clientPool.addNew(client);
        clientPool.addNew(new Client("127.0.0.1", 5500));
        assertTrue(clientPool.removeAll());
    }

    @Test
    void getAll() {
        Client secondClient = new Client("127.0.0.1", 5500);
        ArrayList<Client> expected = new ArrayList<>(List.of(client, secondClient));
        clientPool.addNew(client);
        clientPool.addNew(secondClient);
        List<Client> actual = clientPool.getAll();
        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    void getAllID() {
        Client secondClient = new Client("127.0.0.1", 5500);
        List<Object> expected = new ArrayList<>(List.of(client.getId(), secondClient.getId()));
        clientPool.addNew(client);
        clientPool.addNew(secondClient);
        List<Object> actual = clientPool.getAllID();
        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }

    @ParameterizedTest
    @MethodSource("methodClientProvider")
    void get() {
        if (client != null) {
            clientPool.addNew(client);
            Object id = client.getId();
            assertEquals(client, clientPool.get(id));
        } else {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> clientPool.get(null));
            assertEquals("id is marked non-null but is null", exception.getMessage());
        }
    }
}
package service;

import clients.another.Client;
import exceptions.HandleException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.containers.AbstractHandlerContainer;
import service.containers.ByteBufferHandlerContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DefaultClientManagerTest {
    static DefaultClientManager clientManager;
    static Client client;
    static AbstractHandlerContainer<Object, ByteBuffer> handlerContainer;
    static IdentifiableMessageHandler<Object, ByteBuffer> handler;

    @BeforeAll
    static void beforeAll() {
        clientManager = new DefaultClientManager();
        client = new Client("127.0.0.1", 7000);
        handlerContainer = new ByteBufferHandlerContainer<>();
        handler = new IdentifiableMessageHandler<>("print") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println(message);
            }
        };
    }

    @BeforeEach
    void setUp() {
        clientManager.removeAllNetEntities();
        handlerContainer.removeAll();
    }

//    static Stream<Arguments> methodClientProvider() {
//        return Stream.of(
//                Arguments.arguments(client),
//                Arguments.arguments(new Client("127.0.0.1", 5500)),
//                Arguments.arguments((Object) null)
//        );
//    }
//
//    static Stream<Arguments> methodHandlerContainerProvider() {
//        return Stream.of(
//                Arguments.arguments(handlerContainer),
//                Arguments.arguments(new ByteBufferHandlerContainer<>()),
//                Arguments.arguments((Object) null)
//        );
//    }
//
//    static Stream<Arguments> methodHandlerProvider() {
//        return Stream.of(
//                Arguments.arguments(handler),
//                Arguments.arguments(new IdentifiableMessageHandler<String, ByteBuffer>("toFile") {
//                    @Override
//                    public void handleMessage(ByteBuffer message) throws HandleException {
//                        try (FileOutputStream file = new FileOutputStream("E:\\log.log", true)) {
//                            file.getChannel().write(message);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }),
//                Arguments.arguments((Object) null)
//        );
//    }

    @Test
    void getHandlerContainerTest() {
        clientManager.addNetEntity(client, handlerContainer);
        assertEquals(handlerContainer, clientManager.getHandlerContainer(client));
    }

    @Test
    void getHandlerContainerTestForAnotherClient() {
        clientManager.addNetEntity(client, handlerContainer);
        Client secondClient = new Client("127.0.0.1", 5500);
        AbstractHandlerContainer<Object, ByteBuffer> actualContainer = clientManager.getHandlerContainer(secondClient);
        assertNull(actualContainer);
    }

    @Test
    void getHandlerContainerTestForNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.getHandlerContainer(null));
        assertEquals("netEntity is marked non-null but is null", exception.getMessage());
    }

    @Test
    void addNetEntityTest() {
        boolean isSuccess = clientManager.addNetEntity(client, handlerContainer);
        Client actualFromPool = clientManager.entityPool.get(client.getId());
        Client actualFromMap = clientManager.map.keySet().stream().findFirst().orElse(null);
        AbstractHandlerContainer<Object, ByteBuffer> actualContainer = clientManager.getHandlerContainer(client);
        assertEquals(client, actualFromPool);
        assertEquals(client, actualFromMap);
        assertEquals(handlerContainer, actualContainer);
        assertTrue(isSuccess);
    }

    @Test
    void addNetEntityTestFromNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.addNetEntity(null, handlerContainer));
        assertEquals("netEntity is marked non-null but is null", exception.getMessage());
    }

    @Test
    void addNetEntityTestFromNullableContainer() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.addNetEntity(client, null));
        assertEquals("handlerContainer is marked non-null but is null", exception.getMessage());
    }

    @Test
    void addSameNewEntity() {
        clientManager.addNetEntity(client, handlerContainer);
        Client sameClient = new Client("127.0.0.1", 7000);
        sameClient.setId(client.getId());
        boolean isSuccess = clientManager.addNetEntity(sameClient, handlerContainer);
        assertFalse(isSuccess);
    }

    @Test
    void removeNetEntityTest() {
        clientManager.addNetEntity(client, handlerContainer);
        boolean isSuccess = clientManager.removeNetEntity(client);
        assertTrue(isSuccess);
        assertTrue(clientManager.entityPool.getAll().isEmpty());
        assertTrue(clientManager.map.isEmpty());
    }

    @Test
    void removeNetEntityTestFromEmptyPool() {
        boolean isSuccess = clientManager.removeNetEntity(client);
        assertFalse(isSuccess);
        assertTrue(clientManager.entityPool.getAll().isEmpty());
        assertTrue(clientManager.map.isEmpty());
    }

    @Test
    void removeAnotherNetEntityTest() {
        clientManager.addNetEntity(client, handlerContainer);
        boolean isSuccess = clientManager.removeNetEntity(new Client("127.0.0.1", 5500));
        assertFalse(isSuccess);
        assertFalse(clientManager.entityPool.getAll().isEmpty());
        assertFalse(clientManager.map.isEmpty());
    }

    @Test
    void removeNullableNetEntityTest() {
        clientManager.addNetEntity(client, handlerContainer);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.removeNetEntity(null));
        assertEquals("netEntity is marked non-null but is null", exception.getMessage());
    }

//    @Test
//    void removeNetEntityTestWithId() {
//        clientManager.addNetEntity(client, handlerContainer);
//        Object id = client.getId();
//        boolean isSuccess = clientManager.removeNetEntity(id);
//        assertTrue(isSuccess);
//        assertTrue(clientManager.entityPool.getAll().isEmpty());
//        assertTrue(clientManager.map.isEmpty());
//    }
//
//    @Test
//    void removeNetEntityTestWithAnotherId() {
//        clientManager.addNetEntity(client, handlerContainer);
//        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.removeNetEntity(10));
//        assertEquals("Specified entity with ID '10' missed in pool", exception.getMessage());
//    }
//
//    @Test
//    void removeNetEntityTestWithNullableId() {
//        clientManager.addNetEntity(client, handlerContainer);
//        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.removeNetEntity((Object) null));
//        assertEquals("idNetEntity is marked non-null but is null", exception.getMessage());
//    }

    @Test
    void addHandlerTest() {
        clientManager.addNetEntity(client, handlerContainer);
        boolean isSuccess = clientManager.addHandler(client, handler);
        IdentifiableMessageHandler<Object, ByteBuffer> actualHandler = handlerContainer.getAll().get(0);
        assertEquals(handler, actualHandler);
        assertTrue(isSuccess);
    }

    @Test
    void addHandlerTestToMissedClient() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.addHandler(client, handler));
        assertEquals(String.format("Specified %s missed in pool", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void addSameHandlerTest() {
        clientManager.addNetEntity(client, handlerContainer);
        IdentifiableMessageHandler<Object, ByteBuffer> sameHandler = new IdentifiableMessageHandler<Object, ByteBuffer>("print") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {

            }
        };
        clientManager.addHandler(client, sameHandler);
        boolean isSuccess = clientManager.addHandler(client, handler);
        assertFalse(isSuccess);
    }

    @Test
    void addHandlerTestToNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.addHandler(null, handler));
        assertEquals(String.format("netEntity is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void addNullableHandlerTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.addHandler(client, null));
        assertEquals(String.format("handler is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void removeHandler() {
        clientManager.addNetEntity(client, handlerContainer);
        clientManager.addHandler(client, handler);
        Object handlerId = handler.getIdentifier();
        boolean isSuccess = clientManager.removeHandler(client, handlerId);
        List<IdentifiableMessageHandler<Object, ByteBuffer>> all = clientManager.getHandlerContainer(client).getAll();
        assertTrue(all.isEmpty());
        assertTrue(isSuccess);
    }

    @Test
    void removeHandlerTestFromMissedClient() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.removeHandler(client, ""));
        assertEquals(String.format("Specified %s missed in pool", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void removeMissedHandlerTest() {
        clientManager.addNetEntity(client, handlerContainer);
        boolean isSuccess = clientManager.removeHandler(client, "");
        assertFalse(isSuccess);
    }

    @Test
    void removeHandlerTestFromNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.removeHandler(null, ""));
        assertEquals(String.format("netEntity is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void removeNullableHandlerTestFromClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.removeHandler(client, null));
        assertEquals(String.format("handlerId is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void getAllNetEntitiesTest() {
        clientManager.addNetEntity(client, handlerContainer);
        Client secondClient = new Client("127.0.0.1", 5500);
        clientManager.addNetEntity(secondClient, new ByteBufferHandlerContainer<>());
        List<Client> actualNetEntityList = clientManager.getAllNetEntities();
        List<Client> expectedNetEntityList = List.of(client, secondClient);
        assertTrue(expectedNetEntityList.size() == actualNetEntityList.size() && expectedNetEntityList.containsAll(actualNetEntityList));
    }

    @Test
    void getAllIdNetEntityTest() {
        clientManager.addNetEntity(client, handlerContainer);
        Client secondClient = new Client("127.0.0.1", 5500);
        clientManager.addNetEntity(secondClient, new ByteBufferHandlerContainer<>());
        List<Object> actualIdList = clientManager.getAllIdNetEntity();
        List<Object> expectedIdList = List.of(client.getId(), secondClient.getId());
        assertTrue(expectedIdList.size() == actualIdList.size() && expectedIdList.containsAll(actualIdList));
    }

    @Test
    void getAllHandlersTest() {
        clientManager.addNetEntity(client, handlerContainer);
        clientManager.addHandler(client, handler);
        IdentifiableMessageHandler<Object, ByteBuffer> secondHandler = new IdentifiableMessageHandler<>("toFile") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
            }
        };
        clientManager.addHandler(client, secondHandler);
        List<MessageHandler<ByteBuffer>> actualHandlerList = clientManager.getAllHandlers(client);
        List<MessageHandler<ByteBuffer>> expectedHandlerList = List.of(handler, secondHandler);
        assertTrue(expectedHandlerList.size() == actualHandlerList.size() && expectedHandlerList.containsAll(actualHandlerList));
    }

    @Test
    void getAllHandlersTestFromMissedClient() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.getAllHandlers(client));
        assertEquals(String.format("Specified %s missed in pool", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void getAllHandlersTestFromNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.getAllHandlers(null));
        assertEquals(String.format("netEntity is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void getAllIdHandlersTest() {
        clientManager.addNetEntity(client, handlerContainer);
        IdentifiableMessageHandler<Object, ByteBuffer> secondHandler = new IdentifiableMessageHandler<>("toFile") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
            }
        };
        clientManager.addHandler(client, handler);
        clientManager.addHandler(client, secondHandler);
        List<Object> actualIdList = clientManager.getAllIdHandlers(client);
        List<Object> expectedIdList = List.of(handler.getIdentifier(), secondHandler.getIdentifier());
        assertTrue(expectedIdList.size() == actualIdList.size() && expectedIdList.containsAll(actualIdList));
    }

    @Test
    void getAllIdHandlersTestFromMissedClient() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.getAllIdHandlers(client));
        assertEquals(String.format("Specified %s missed in pool", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void getAllIdHandlersTestFromNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.getAllIdHandlers(null));
        assertEquals(String.format("netEntity is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void removeAllNetEntitiesTest() {
        clientManager.addNetEntity(client, handlerContainer);
        Client secondClient = new Client("127.0.0.1", 5500);
        clientManager.addNetEntity(secondClient, new ByteBufferHandlerContainer<>());
        boolean isSuccess = clientManager.removeAllNetEntities();
        assertTrue(isSuccess);
        assertTrue(clientManager.getAllNetEntities().isEmpty());
    }

    @Test
    void removeAllHandlersTest() {
        clientManager.addNetEntity(client, handlerContainer);
        IdentifiableMessageHandler<Object, ByteBuffer> secondHandler = new IdentifiableMessageHandler<>("toFile") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
            }
        };
        clientManager.addHandler(client, handler);
        clientManager.addHandler(client, secondHandler);
        boolean isSuccess = clientManager.removeAllHandlers(client);
        assertTrue(isSuccess);
        assertTrue(clientManager.getAllHandlers(client).isEmpty());
    }

    @Test
    void removeAllHandlersTestFromMissedClient() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> clientManager.removeAllHandlers(client));
        assertEquals(String.format("Specified %s missed in pool", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void removeAllHandlersTestFromNullableClient() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.removeAllHandlers(null));
        assertEquals(String.format("netEntity is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void getNetEntityTest() {
        clientManager.addNetEntity(client, handlerContainer);
        Object clientId = client.getId();
        Client expectedClient = clientManager.getNetEntity(clientId);
        assertEquals(client, expectedClient);
    }

    @Test
    void getMissedNetEntityTest() {
        Object clientId = client.getId();
        Client expectedClient = clientManager.getNetEntity(clientId);
        assertNull(expectedClient);
    }

    @Test
    void getNullableNetEntityTest() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> clientManager.getNetEntity(null));
        assertEquals(String.format("id is marked non-null but is null", client.getClass().getSimpleName()), exception.getMessage());
    }

    @Test
    void createClientTestWithHostAndPort() {
        Client actualClient = clientManager.createClient("127.0.0.1", 7000, Client.class);
        actualClient.setId(1);
        assertEquals(client, actualClient);
    }

    @Test
    void createClientTestWithSocketChannel() {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.bind(new InetSocketAddress("127.0.0.1", 7000));
            Client expectedClient = new Client(channel);
            expectedClient.setId(1);
            Client actualClient = clientManager.createClient(channel, Client.class);
            actualClient.setId(1);
            assertEquals(expectedClient, actualClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createClientTestWithInetSocketAddress() {
        InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", 7000);
        Client expectedClient = new Client(endpoint);
        expectedClient.setId(1);
        Client actualClient = clientManager.createClient(endpoint, Client.class);
        actualClient.setId(1);
        assertEquals(expectedClient, actualClient);
    }

    @Test
    void createClientTestWithSocket() {
        try {
            Socket socket = new Socket();
            socket.bind(new InetSocketAddress("127.0.0.1", 7000));
            Client expectedClient = new Client(socket);
            expectedClient.setId(1);
            Client actualClient = clientManager.createClient(socket, Client.class);
            actualClient.setId(1);
            assertEquals(expectedClient, actualClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getClientTest() {
        clientManager.addNetEntity(client, handlerContainer);
        Integer localPort = client.getClientConnection().getLocalPort();
        Client actualClient = clientManager.getClient(localPort);
        assertEquals(client, actualClient);
    }
}
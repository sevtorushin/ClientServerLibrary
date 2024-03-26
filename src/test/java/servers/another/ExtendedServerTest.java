package servers.another;

import clients.another.ExtendedClient;
import exceptions.HandleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.IdentifiableMessageHandler;
import service.RunnableTask;
import service.containers.ExtendedClientPool;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedServerTest {
    private static ExtendedServer server;
    private static ExtendedClient firstClient;
    private static final String message = "Hello";
    private static final ByteBuffer byteMessage = ByteBuffer.allocate(512);


    @BeforeAll
    static void beforeAll() {
        try {
            server = new ExtendedServer(9000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firstClient = new ExtendedClient("127.0.0.1", 9000);
        byteMessage.put(message.getBytes());
    }

    @AfterEach
    void tearDown() {
        try {
            server.stop();
            firstClient.close();
            byteMessage.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Server will be started")
    void startServerTest() {
        server.start();
        assertFalse(server.isStopped());
    }

    @Test
    void stopServerTest() {
        try {
            server.start();
            server.stop();
            assertTrue(server.isStopped());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void repeatCallStartMethodTest() {
        server.start();
        server.start();
    }

    @Test
    void connectClientTest() {
        server.start();
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        assertFalse(server.getClientPool().getAll().isEmpty());
    }

    @Test
    void disconnectClientTest() {
        server.setCheckClients(true);
        server.start();
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        assertFalse(server.getClientPool().getAll().isEmpty());
        firstClient.disconnect();
        pause(0.1); // Здесь сервер успевают удалить клиента из пула
        assertTrue(server.getClientPool().getAll().isEmpty());
        server.setCheckClients(false);
    }

    @Test
    void reconnectClientTest() {
        server.setCheckClients(true);
        server.start();
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        firstClient.disconnect();
        pause(0.1); // Здесь сервер успевают удалить клиента из пула
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        assertFalse(server.getClientPool().getAll().isEmpty());
        server.setCheckClients(false);
    }

    //
    @Test
    void connectAfterCloseClientTest() {
        server.setCheckClients(true);
        server.start();
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        firstClient.close();
        pause(0.1); // Здесь сервер успевают удалить клиента из пула
        firstClient.connect();
        pause(0.1); // Здесь сервер успевают добавить клиента в пул
        assertFalse(server.getClientPool().getAll().isEmpty());
    }

    // После отключения клиента, клиент со стороны сервера завершает задачи и удаляет из контейнера все задачи и обработчики
    @Test
    void shouldCancelTaskAndDeleteHandlerAfterDisconnectClient() {
        server.start();
        firstClient.connect();
        ExtendedClient onServerSideClient = null;
        while (onServerSideClient == null){
            onServerSideClient = (ExtendedClient) ((ExtendedClientPool)server.getClientPool()).getNewClient();
            pause(0.01);
        }
        IdentifiableMessageHandler<Object, ByteBuffer> handler = new IdentifiableMessageHandler<>("print") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println(new String(message.array()));
            }
        };
        ExtendedClient finalOnServerSideClient = onServerSideClient;
        RunnableTask clientTask = new RunnableTask("send") {
            @Override
            public void run() {
                while (!isCancelled()) {
                    try {
                        server.sendMessage(finalOnServerSideClient, byteMessage);
                        finalOnServerSideClient.getHandlerContainer().invokeAll(byteMessage);
                        pause(0.5);
                    } catch (IOException e) {
                        pause(0.1);
                    } catch (HandleException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        onServerSideClient.getHandlerContainer().addNew(handler);
        onServerSideClient.getTaskContainer().addNew(clientTask);
        clientTask.execute();
        pause(1);
        firstClient.disconnect();
        pause(1);
        assertTrue(onServerSideClient.getHandlerContainer().getAll().isEmpty());
        assertTrue(onServerSideClient.getTaskContainer().getAll().isEmpty());
        assertTrue(clientTask.isCancelled());
        assertTrue(server.getClientPool().getAll().isEmpty());
    }

    private static void pause(double sec) {
        try {
            Thread.sleep((long) (sec * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Запуск передачи сообщения сервером для всех клиентов
    private void runTransferFromServer() {
        server.start();
        new Thread(() -> {
            while (!server.isStopped()) {
                server.getClientPool().getAll().forEach(client -> {
                    try {
                        server.sendMessage(client, byteMessage);
                    } catch (IOException e) {
                        System.err.println("!!!");
                    }
                });
                pause(0.5);
            }
        }).start();
    }
}
package clients.another;

import exceptions.HandleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import servers.another.ExtendedServer;
import service.IdentifiableMessageHandler;
import service.IdentifiableTask;
import service.RunnableTask;
import service.Task;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedClientTest {

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
    void connectionClientTest() {
        server.start();
        firstClient.connect();
        assertTrue(firstClient.isConnected());
        assertFalse(firstClient.isClosed());
    }

    @Test
    void disconnectClientTest() {
        server.start();
        firstClient.connect();
        firstClient.disconnect();
        assertFalse(firstClient.isConnected());
        assertFalse(firstClient.isClosed());
    }

    @Test
    void closeClientTest() {
        server.start();
        firstClient.connect();
        firstClient.close();
        assertFalse(firstClient.isConnected());
        assertTrue(firstClient.isClosed());
    }

    // Все обработчики удаляются из контенера после вызова метода close клиента
    @Test
    void shouldBeEmptyHandlerContainerAfterCloseClientTest() {
        IdentifiableMessageHandler<Object, ByteBuffer> handler = new IdentifiableMessageHandler<>("handler") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println();
            }
        };
        firstClient.getHandlerContainer().addNew(handler);
        firstClient.close();
        assertTrue(firstClient.getHandlerContainer().getAll().isEmpty());
    }

    // Все задачи удаляются из контенера после вызова метода close клиента
    @Test
    void shouldBeEmptyTaskContainerAfterCloseClientTest() {
        Task<Void> task = new RunnableTask("task") {
            @Override
            public void run() {
                System.out.println();
            }
        };
        firstClient.getTaskContainer().addNew((IdentifiableTask<Object, ?>) task);
        firstClient.close();
        assertTrue(firstClient.getTaskContainer().getAll().isEmpty());
    }

    // Проверка приема и передачи сообщения от сервера к клиенту
    @Test
    void sendAndReceiveMessage() {
        server.start();
        ByteBuffer buffer = ByteBuffer.allocate(5);
        Client client;
        try {
            firstClient.connect();
            pause(0.1); // Здесь сервер успевают добавить клиента в пул
            client = server.getClientPool().getOnLocalPort(9000);
            client.sendMessage(byteMessage);
            firstClient.receiveMessage(buffer);
            assertEquals(byteMessage, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Все задачи клиента завершаются после вызова метода close клиента
    @Test
    void shouldBeCancelledTaskAfterCloseClientTest() {
        Task<Void> task = new RunnableTask("task") {
            @Override
            public void run() {
                while (!isCancelled()) {
                    System.out.println("run task");
                    pause(0.5);
                }
            }
        };
        firstClient.getTaskContainer().addNew((IdentifiableTask<Object, ?>) task);
        task.execute();
        pause(2);
        firstClient.close();
        assertTrue(task.isCancelled());
        pause(1);
    }

    // Выполнение метода disconnect и последующий вызов connect приводит к успешному переподключению к серверу и успешной передачи сообщения
    @Test
    void reconnectAndSendClientTest() {
        server.setCheckClients(true);
        server.start();
        ByteBuffer buffer = ByteBuffer.allocate(5);
        Client client;
        try {
            firstClient.connect();
            pause(0.1); // тут сервер успевает добавить в пул клиента
            firstClient.disconnect();
            firstClient.connect();
            pause(0.1); // тут сервер успевает удалить из пула неактивного клиента
            client = server.getClientPool().getOnLocalPort(9000);
            client.sendMessage(byteMessage);
            firstClient.receiveMessage(buffer);
            assertEquals(byteMessage, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Тест автоматического переподключения клиента после перезапуска сервера
    @Test
    void reconnectClientTest() {
        try {
            firstClient.getClientConnection().setReconnectionMode(true);
            server.start();
            firstClient.connect();
            server.stop();
            firstClient.sendMessage(byteMessage); // После отключения сервера этот метод отработает успешно
            firstClient.sendMessage(byteMessage); // Этот метод уже выбросит исключение
        } catch (IOException e) {
            pause(5);
            assertFalse(firstClient.getClientConnection().isConnected());
            server.start();
            pause(5);
            assertTrue(firstClient.getClientConnection().isConnected());
            firstClient.getClientConnection().setReconnectionMode(false);
            // В конце клиент успешно переподключится
        }
    }

    @Test
    void repeatConnectionTest() {
        server.start();
        firstClient.connect();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> firstClient.connect()); //Повторный вызов метода connect на подключенном клиенте приведет к исключению
        assertEquals("Client already connected", exception.getMessage());
    }

    // Тест повторного вызова метода receiveMessage для клиента
    @Test
    void repeatReceiveMessageTest() {
        ByteBuffer temp1 = ByteBuffer.allocate(512);
        server.start();
        firstClient.connect();
        pause(0.1); // тут сервер успевает добавить в пул клиента
        server.getClientPool().getAll().forEach(client -> {
            try {
                server.sendMessage(client, byteMessage);
            } catch (IOException e) {
                System.err.println("!!!");
            }
        });
        try {
            int bytes = firstClient.receiveMessage(temp1);
            assertNotEquals(0, bytes);
            assertNotEquals(0, temp1.position());
            bytes = firstClient.receiveMessage(temp1);// Повторные вызовы не приведут к перезаписи буфера, так как сервером не отправлено повторных сообщений
            assertNotEquals(0, temp1.position());
            assertEquals(0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // После вызова метода disconnect и повторного подключения клиента обработчики и задачи продолжают выполняться
    @Test
    void successfullyReconnectAfterReconnectClientTest() {
        IdentifiableMessageHandler<Object, ByteBuffer> handler = new IdentifiableMessageHandler<>("handler") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println(new String(message.array()));
            }
        };
        RunnableTask task = new RunnableTask("task") {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(512);
                while (!isCancelled()) {
                    try {
                        int bytes = firstClient.receiveMessage(buffer);
                        if (bytes == 0){
                            pause(0.1);
                            continue;
                        }
                        firstClient.getHandlerContainer().invokeAll(buffer);
                        pause(0.1);
                    } catch (IOException ignore) {
                        pause(0.1);
                    } catch (HandleException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        firstClient.getHandlerContainer().addNew(handler);
        firstClient.getTaskContainer().addNew(task);
        runTransferFromServer();
        firstClient.connect();
        task.execute();
        pause(2); // Здесь ничинается передача сообщений
        firstClient.disconnect();
        pause(2); // Здесь передача прерывается
        firstClient.connect();
        pause(2); // Передача возобновляется
    }

    // После рестарта сервера клиент автоматически переподключается и задача клиента продолжает выполняться
    @Test
    void autoReconnectClientAfterRestartServer() {
        IdentifiableMessageHandler<Object, ByteBuffer> handler = new IdentifiableMessageHandler<>("handler") {
            @Override
            public void handleMessage(ByteBuffer message) throws HandleException {
                System.out.println(new String(message.array()));
            }
        };
        RunnableTask task = new RunnableTask("task") {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(512);
                while (!isCancelled()) {
                    try {
                        int bytes = firstClient.receiveMessage(buffer);
                        if (bytes == 0){
                            pause(0.1);
                            continue;
                        }
                        firstClient.getHandlerContainer().invokeAll(buffer);
                        pause(0.1);
                    } catch (IOException e) {
                        pause(0.1);
                    } catch (HandleException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        firstClient.getClientConnection().setReconnectionMode(true);
        firstClient.getHandlerContainer().addNew(handler);
        firstClient.getTaskContainer().addNew(task);
        runTransferFromServer();
        pause(1);
        firstClient.connect();
        task.execute();
        pause(3); // Начинается прием сообщений от сервера
        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pause(5); // Клиент пытается переподключится к серверу
        runTransferFromServer();
        pause(5); // Клиент переподключился и прием сообщений от сервера возобновляется
        firstClient.getClientConnection().setReconnectionMode(false);
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
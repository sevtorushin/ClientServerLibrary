package clients.another;

import exceptions.HandleException;
import test.MessageHandler;
import test.MessageStorage;
import test.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class CachingClient extends Client {
    private TaskManager taskManager;
    private MessageStorage messageStorage;

    public CachingClient(SocketChannel socketChannel) {
        super(socketChannel);
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    public CachingClient(InetSocketAddress endpoint) {
        super(endpoint);
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    public CachingClient(String host, int port) {
        super(host, port);
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    @Override
    public ByteBuffer receiveMessage() { //todo дублирование кода
        if (!isConnected()) {
            return messageStorage.getEmptyBuffer();
        }
        ByteBuffer buffer = messageStorage.getTempBuffer();
        try {
            int n = clientConnection.read(buffer);
            if (n < 1) {
                return messageStorage.getEmptyBuffer();
            }
        } catch (IOException e) {
            clientConnection.disconnect();
            clientConnection.reconnect();
            return messageStorage.getEmptyBuffer();
        }
        return buffer.duplicate();
    }

    @Override
    public void sendMessage(ByteBuffer message) { //todo подумать над реализацией. Нужен ли тут обработчик или отправлять сразу в сокет
        if (!isConnected())
            return;
        try {
            taskManager.handleAllOutgoingMessage(message);
        } catch (HandleException e) {
            if (e.getCause() instanceof IOException) {
                clientConnection.disconnect();
                clientConnection.reconnect();
            } else e.printStackTrace();
        }
    }

    public void handleMessage(ByteBuffer message) throws HandleException {
        taskManager.handleAllIncomingMessage(message);
    }

    public void addTask(String name, MessageHandler handler) {
        this.taskManager.addTask(name, handler);
    }

    public void removeTask(String name) {
        this.taskManager.removeTask(name);
    }

    public List<String> getALLTask() {
        return taskManager.getALLTask();
    }

    public void removeAllTask() {
        taskManager.removeAllTask();
    }

    public void saveToCache(ByteBuffer message) {
        messageStorage.saveToCache(message);
    }

    public ByteBuffer readAllCache() {
        return messageStorage.readAllCache();
    }

    public ByteBuffer readElementCache() {
        return messageStorage.readElementCache();
    }
}

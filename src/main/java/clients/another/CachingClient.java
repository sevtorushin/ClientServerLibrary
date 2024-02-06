package clients.another;

import exceptions.HandleException;
import lombok.ToString;
import test.MessageHandler;
import test.MessageStorage;
import test.TaskManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@ToString(callSuper = true)
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

    public void handleIncomingMessage(ByteBuffer message) throws HandleException {
        taskManager.handleAllIncomingMessage(message);
    }

    public void handleOutgoingMessage(ByteBuffer message) throws HandleException {
        taskManager.handleAllOutgoingMessage(message);
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

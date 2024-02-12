package clients.another;

import exceptions.HandleException;
import lombok.ToString;
import service.CachedMessageStorage;
import service.MessageHandler;
import service.TaskManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@ToString(callSuper = true,
exclude = {"taskManager","messageStorage"})
public class CachingClient extends Client {
    private TaskManager taskManager;
    private CachedMessageStorage messageStorage;

    public CachingClient(SocketChannel socketChannel) {
        super(socketChannel);
        this.taskManager = new TaskManager();
        this.messageStorage = new CachedMessageStorage();
    }

    public CachingClient(InetSocketAddress endpoint) {
        super(endpoint);
        this.taskManager = new TaskManager();
        this.messageStorage = new CachedMessageStorage();
    }

    public CachingClient(String host, int port) {
        super(host, port);
        this.taskManager = new TaskManager();
        this.messageStorage = new CachedMessageStorage();
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

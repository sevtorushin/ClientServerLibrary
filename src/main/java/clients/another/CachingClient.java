package clients.another;

import exceptions.HandleException;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.CachedMessageStorage;
import service.HandlerManager;
import service.MessageHandler;
import service.ByteBufferHandlerManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@ToString(callSuper = true,
exclude = {"handlerManager","messageStorage"})
public class CachingClient extends Client {
    private final HandlerManager<String, ByteBuffer> handlerManager;
    private final CachedMessageStorage messageStorage;

    private static final Logger log = LogManager.getLogger(CachingClient.class.getSimpleName());

    public CachingClient(SocketChannel socketChannel) {
        super(socketChannel);
        this.handlerManager = new ByteBufferHandlerManager<>();
        this.messageStorage = new CachedMessageStorage();
    }

    public CachingClient(InetSocketAddress endpoint) {
        super(endpoint);
        this.handlerManager = new ByteBufferHandlerManager<>();
        this.messageStorage = new CachedMessageStorage();
    }

    public CachingClient(String host, int port) {
        super(host, port);
        this.handlerManager = new ByteBufferHandlerManager<>();
        this.messageStorage = new CachedMessageStorage();
    }

    public void handleMessage(ByteBuffer message) throws HandleException {
        handlerManager.handle(message);
    }

    public void addHandler(String name, MessageHandler<ByteBuffer> handler) {
        this.handlerManager.addHandler(name, handler);
        log.debug(String.format("Task \"%s\" added to %s", name, this));
    }

    public void removeHandler(String name) {
        this.handlerManager.removeHandler(name);
        log.debug(String.format("Task \"%s\" removed from %s", name, this));
    }

    public List<String> getALLHandlers() {
        return handlerManager.getALLHandlers();
    }

    public void removeAllHandlers() {
        handlerManager.removeAllHandlers();
        log.debug(String.format("All tasks removed from %s", this));
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

package clients.another;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.containers.TaskContainer;
import service.containers.AbstractHandlerContainer;
import service.containers.ByteBufferHandlerContainer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(callSuper = true,
        exclude = {"handlerContainer", "taskContainer"})
@EqualsAndHashCode(callSuper = true,
        exclude = {"handlerContainer", "taskContainer"})
public class ExtendedClient extends Client {
    @Getter
    private final AbstractHandlerContainer<Object, ByteBuffer> handlerContainer;
    @Getter
    private final TaskContainer taskContainer;

    private static final Logger log = LogManager.getLogger(ExtendedClient.class.getSimpleName());

    public ExtendedClient(SocketChannel socketChannel) {
        super(socketChannel);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    public ExtendedClient(Socket socket) {
        super(socket);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    public ExtendedClient(InetSocketAddress endpoint) {
        super(endpoint);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    public ExtendedClient(String host, int port) {
        super(host, port);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    @Override
    public void close() {
        if (!taskContainer.forceRemoveAll()) {
            log.warn(String.format("Don't remove all tasks for %s. Client not closed", this));
            return;
        }
        this.handlerContainer.removeAll();
        super.close();
    }
}

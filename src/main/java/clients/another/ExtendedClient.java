package clients.another;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import service.containers.TaskContainer;
import service.containers.AbstractHandlerContainer;
import service.containers.ByteBufferHandlerContainer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExtendedClient extends Client {
    @Getter
    private final AbstractHandlerContainer<Object, ByteBuffer> handlerContainer;
    @Getter
    private final TaskContainer taskContainer;

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


}

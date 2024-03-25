package servers.another;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.containers.AbstractHandlerContainer;
import service.containers.ByteBufferHandlerContainer;
import service.containers.TaskContainer;

import java.io.IOException;
import java.nio.ByteBuffer;

@ToString(callSuper = true,
        exclude = {"handlerContainer", "taskContainer"})
@EqualsAndHashCode(callSuper = true,
        exclude = {"handlerContainer", "taskContainer"})
public class ExtendedServer extends Server{
    @Getter
    private final AbstractHandlerContainer<Object, ByteBuffer> handlerContainer;
    @Getter
    private final TaskContainer taskContainer;

    private static final Logger log = LogManager.getLogger(ExtendedServer.class.getSimpleName());

    public ExtendedServer(Integer port) throws IOException {
        super(port);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    public ExtendedServer(Integer port, boolean checkClients) throws IOException {
        super(port, checkClients);
        this.handlerContainer = new ByteBufferHandlerContainer<>();
        this.taskContainer = new TaskContainer();
    }

    @Override
    public void stop() throws IOException {
        if (!taskContainer.forceRemoveAll()) {
            log.warn(String.format("Don't remove all tasks for %s. Client not closed", this));
            return;
        }
        this.handlerContainer.removeAll();
        super.stop();
    }
}

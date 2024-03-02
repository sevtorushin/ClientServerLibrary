package service.copy;

import exceptions.HandleException;
import service.MessageHandler;

import java.nio.ByteBuffer;

public class ByteBufferHandlerContainerCopy<I> extends AbstractHandlerContainerCopy<I, ByteBuffer> {

    @Override
    public void invokeAll(ByteBuffer message) throws HandleException {
        if (message.position() == 0)
            return;
        message.flip();
        for (MessageHandler<ByteBuffer> handler : handlers.values()) {
            handler.handleMessage(message);
            message.rewind();
        }
    }
}
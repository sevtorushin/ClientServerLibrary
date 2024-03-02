package service.containers;

import exceptions.HandleException;
import service.MessageHandler;

import java.nio.ByteBuffer;

public class ByteBufferHandlerContainer<I> extends AbstractHandlerContainer<I, ByteBuffer> {

    @Override
    public void invokeAll(ByteBuffer message) throws HandleException {
        if (message.position() == 0)
            return;
        message.flip();
        for (MessageHandler<ByteBuffer> handler : handlers) {
            handler.handleMessage(message);
            message.rewind();
        }
    }
}
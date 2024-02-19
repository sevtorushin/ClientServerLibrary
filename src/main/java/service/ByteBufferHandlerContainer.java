package service;

import exceptions.HandleException;

import java.nio.ByteBuffer;
import java.util.Collection;

public class ByteBufferHandlerContainer<N> extends AbstractHandlerContainer<N, ByteBuffer> {

    @Override
    public void handle(ByteBuffer message) throws HandleException {
        if (message.position() == 0)
            return;
        message.flip();
        Collection<MessageHandler<ByteBuffer>> values = handlers.values();
        for (MessageHandler<ByteBuffer> handler : values) {
            handler.handleMessage(message);
            message.rewind();
        }
    }
}
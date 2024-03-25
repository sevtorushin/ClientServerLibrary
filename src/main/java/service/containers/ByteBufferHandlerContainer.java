package service.containers;

import exceptions.HandleException;
import lombok.NonNull;
import service.IdentifiableMessageHandler;
import service.MessageHandler;

import java.nio.ByteBuffer;

public class ByteBufferHandlerContainer<I> extends AbstractHandlerContainer<I, ByteBuffer> {

    @Override
    public void invokeAll(@NonNull ByteBuffer message) throws HandleException {
        if (message.position() == 0)
            return;
        for (MessageHandler<ByteBuffer> handler : entityStorage) {
            handler.handleMessage(message);
        }
    }

    @Override
    protected I getId(@NonNull IdentifiableMessageHandler<I, ByteBuffer> entity) {
        return entity.getIdentifier();
    }
}
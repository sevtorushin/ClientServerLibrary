package service;

import exceptions.HandleException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ByteBufferHandlerManager<N> implements HandlerManager<N, ByteBuffer> {
    private final Map<N, MessageHandler<ByteBuffer>> handlers;

    public ByteBufferHandlerManager() {
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public void addHandler(N identifier, MessageHandler<ByteBuffer> handler) {
        this.handlers.put(identifier, handler);
    }

    @Override
    public void removeHandler(N identifier) {
        handlers.remove(identifier);
    }

    @Override
    public List<N> getALLHandlers() {
        return new ArrayList<>(handlers.keySet());
    }

    @Override
    public void removeAllHandlers() {
        handlers.clear();
    }

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
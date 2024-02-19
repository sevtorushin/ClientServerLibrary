package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHandlerContainer<N, T> implements HandlerManager<N, T>{
    protected final Map<N, MessageHandler<T>> handlers;

    protected AbstractHandlerContainer() {
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean addHandler(N identifier, MessageHandler<T> handler) {
        if (handlers.containsKey(identifier))
            return false;
        else {
            this.handlers.put(identifier, handler);
            return true;
        }
    }

    @Override
    public boolean removeHandler(N identifier) {
        MessageHandler<T> handler = handlers.remove(identifier);
        return handler != null;
    }

    @Override
    public List<N> getALLHandlers() {
        return new ArrayList<>(handlers.keySet());
    }

    @Override
    public boolean removeAllHandlers() {
        handlers.clear();
        return true;
    }

    @Override
    public MessageHandler<T> getHandler(N identifier) {
        return handlers.get(identifier);
    }
}

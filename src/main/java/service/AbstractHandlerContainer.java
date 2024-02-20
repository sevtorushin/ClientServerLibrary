package service;

import exceptions.HandleException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHandlerContainer<I, T> implements Container<I, IdentifiableMessageHandler<I, T>> {
    protected final Set<IdentifiableMessageHandler<I, T>> handlers;

    protected AbstractHandlerContainer() {
        this.handlers = new HashSet<>();
    }

    @Override
    public boolean addNew(IdentifiableMessageHandler<I, T> handler) {
        if (handlers.contains(handler))
            return false;
        else {
            this.handlers.add(handler);
            return true;
        }
    }

    @Override
    public boolean remove(IdentifiableMessageHandler<I, T> handler) {
        return handlers.remove(handler);
    }

    @Override
    public boolean removeAll() {
        handlers.clear();
        return true;
    }

    @Override
    public List<IdentifiableMessageHandler<I, T>> getAll() {
        return new ArrayList<>(handlers);
    }

    @Override
    public IdentifiableMessageHandler<I, T> get(I id) {
        return handlers.stream().filter(handler -> handler.getIdentifier().equals(id)).findFirst().orElse(null);
    }

    public boolean removeFromId(I identifier) {
        MessageHandler<T> handler = get(identifier);
        if (handler != null)
            return handlers.remove(handler);
        else return false;
    }

    public abstract void invokeAll(T message) throws HandleException;
}

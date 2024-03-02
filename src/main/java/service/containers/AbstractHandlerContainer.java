package service.containers;

import exceptions.HandleException;
import service.IdentifiableMessageHandler;

import java.util.*;
import java.util.stream.Collectors;

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
    public boolean removeForID(I ID) {
        return handlers.removeIf(imh -> imh.getIdentifier().equals(ID));
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
    public List<I> getAllID() {
        return handlers.stream().map(IdentifiableMessageHandler::getIdentifier).collect(Collectors.toList());
    }

    @Override
    public IdentifiableMessageHandler<I, T> get(I id) {
        return handlers.stream().filter(handler -> handler.getIdentifier().equals(id)).findFirst().orElse(null);
    }

    public abstract void invokeAll(T message) throws HandleException;
}

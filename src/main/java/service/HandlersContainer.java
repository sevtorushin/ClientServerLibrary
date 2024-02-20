package service;

import exceptions.HandleException;

import java.util.List;

public interface HandlersContainer<I, T> extends Container<IdentifiableMessageHandler<I, T>>{
    boolean addHandler(IdentifiableMessageHandler<I, T> handler);
    boolean removeHandler(I identifier);
    MessageHandler<T> getHandler(I identifier);
    boolean removeAllHandlers();
    List<I> getALLHandlers();
    void invokeAll(T message) throws HandleException;
}

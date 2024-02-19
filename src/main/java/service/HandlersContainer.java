package service;

import exceptions.HandleException;

import java.util.List;

public interface HandlersContainer<N, T> {
    boolean addHandler(N identifier, MessageHandler<T> handler);
    boolean removeHandler(N identifier);
    MessageHandler<T> getHandler(N identifier);
    boolean removeAllHandlers();
    List<N> getALLHandlers();
    void handle(T message) throws HandleException;
}

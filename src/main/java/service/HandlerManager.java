package service;

import exceptions.HandleException;

import java.util.List;

public interface HandlerManager<N, T> {
    boolean addHandler(N identifier, MessageHandler<T> handler);
    boolean removeHandler(N identifier);
    boolean removeAllHandlers();
    List<N> getALLHandlers();
    void handle(T message) throws HandleException;
}

package service;

import exceptions.HandleException;

import java.util.List;

public interface HandlerManager<N, T> {
    void addHandler(N identifier, MessageHandler<T> handler);
    void removeHandler(N identifier);
    void removeAllHandlers();
    List<N> getALLHandlers();
    void handle(T message) throws HandleException;
}

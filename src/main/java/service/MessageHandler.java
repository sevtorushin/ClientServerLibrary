package service;

import exceptions.HandleException;

public interface MessageHandler<T> {
    void handleMessage(T message)  throws HandleException;
}

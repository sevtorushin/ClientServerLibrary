package test;

import exceptions.HandleException;

import java.nio.ByteBuffer;

public interface MessageHandler {

    void incomingMessageHandle(ByteBuffer message)  throws HandleException;

    void outgoingMessageHandle(ByteBuffer message) throws HandleException;
}

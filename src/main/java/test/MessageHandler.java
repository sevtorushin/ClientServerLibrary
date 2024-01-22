package test;

import java.nio.ByteBuffer;

public interface MessageHandler {

    void incomingMessageHandle(ByteBuffer message)  throws Exception;

    void outgoingMessageHandle(ByteBuffer message) throws Exception;
}

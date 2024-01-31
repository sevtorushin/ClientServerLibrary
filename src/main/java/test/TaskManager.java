package test;

import exceptions.HandleException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager implements TaskHandler {
    private Map<String, MessageHandler> handlers;

    public TaskManager() {
        this.handlers = new ConcurrentHashMap<>();
    }

    @Override
    public void addTask(String name, MessageHandler handler) {
        this.handlers.put(name, handler);
    }

    @Override
    public void removeTask(String name) {
        handlers.remove(name);
    }

    public void handleAllIncomingMessage(ByteBuffer message) throws HandleException {
        if (message.position() == 0)
            return;
        message.flip();
        Collection<MessageHandler> values = handlers.values();
        for (MessageHandler handler : values) {
            handler.incomingMessageHandle(message);
            message.rewind();
        }
    }

    public void handleAllOutgoingMessage(ByteBuffer message) throws HandleException {
//        if (message.position() == 0) //todo подумой
//            return;
//        message.flip();
        Collection<MessageHandler> values = handlers.values();
        for (MessageHandler handler : values) {
            handler.outgoingMessageHandle(message);
            message.rewind();
        }
    }

    @Override
    public List<String> getALLTask() {
        return new ArrayList<>(handlers.keySet());
    }

    @Override
    public void removeAllTask() {
        handlers.clear();
    }
}

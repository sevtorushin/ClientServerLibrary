package service.containers;

import lombok.Getter;
import lombok.Setter;
import service.ReadProperties;
import service.Stored;

import java.nio.ByteBuffer;

public class MessageStorage implements Stored<ByteBuffer> {
    @Getter
    private final ByteBuffer tempBuffer;
    @Getter
    private final ByteBuffer emptyBuffer;
    @Getter
    @Setter
    private int bufferSize;
    private final ReadProperties properties;

    {
        properties = ReadProperties.getInstance();
    }

    public MessageStorage(int bufferSize) {
        this.tempBuffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
        this.bufferSize = bufferSize;
    }

    public MessageStorage() {
        this.bufferSize = Integer.parseInt(properties.getValue("messageStorage.bufferSize"));
        this.tempBuffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
    }

    @Override
    public void putToStorage(ByteBuffer message) {
        tempBuffer.clear();
        tempBuffer.put(message);
        tempBuffer.flip();
    }

    @Override
    public ByteBuffer retrieveFromStorage() {
        int limit = tempBuffer.limit();
        ByteBuffer copy = ByteBuffer.allocate(limit);
        copy.put(tempBuffer);
        return copy;
    }

    @Override
    public void clearStorage() {
        tempBuffer.clear();
    }
}

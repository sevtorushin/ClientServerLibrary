package service.containers;

import lombok.Getter;
import lombok.Setter;
import service.Cached;
import service.ReadProperties;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class CachedMessageStorage extends MessageStorage implements Cached<ByteBuffer> {
    @Getter
    private final ByteBuffer tempBuffer;
    @Getter
    private final ByteBuffer emptyBuffer;
    private final LinkedBlockingQueue<ByteBuffer> cache;
    @Getter
    @Setter
    private int bufferSize;
    private final ReadProperties properties;

    {
        this.properties = ReadProperties.getInstance();
    }

    public CachedMessageStorage() {
        this.bufferSize = Integer.parseInt(properties.getValue("messageStorage.bufferSize"));
        this.tempBuffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
        this.cache = new LinkedBlockingQueue<>();
    }

    public CachedMessageStorage(int bufferSize) {
        this.tempBuffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
        this.cache = new LinkedBlockingQueue<>();
        this.bufferSize = bufferSize;
    }

    @Override
    public void saveToCache(ByteBuffer message) {
        ByteBuffer b = ByteBuffer.allocate(message.limit());
        b.put(message);
        b.flip();
        cache.add(b);
        message.rewind();
    }

    @Override
    public ByteBuffer readAllCache() {
        ByteBuffer buffer;
        if (cache.size() == 0) {
            buffer = emptyBuffer;
        } else {
            buffer = ByteBuffer.allocate(bufferSize * cache.size());
            cache.forEach(buffer::put);
            buffer.flip();
            cache.clear();
        }
        return buffer;
    }

    @Override
    public ByteBuffer readElementCache() {
        ByteBuffer buffer;
        if (cache.size() == 0)
            buffer = emptyBuffer;
        else
            try {
                buffer = cache.take();
            } catch (InterruptedException e) {
                buffer = emptyBuffer;
            }
        return buffer;
    }

    @Override
    public boolean removeAll() {
        cache.clear();
        tempBuffer.clear();
        return true;
    }
}

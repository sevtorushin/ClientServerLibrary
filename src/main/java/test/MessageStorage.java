package test;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageStorage {
    @Getter
    private final ByteBuffer tempBuffer;
    @Getter
    private final ByteBuffer emptyBuffer;
    private final LinkedBlockingQueue<byte[]> cache;
    @Getter @Setter
    private int bufferSize = 8192;

    public MessageStorage() {
       this.tempBuffer = ByteBuffer.allocate(bufferSize);
        this.emptyBuffer = ByteBuffer.allocate(0);
        this.cache = new LinkedBlockingQueue<>();
    }

    public void saveToCache(ByteBuffer message) {
        byte[] data = new byte[message.limit()];
        message.get(data);
        cache.add(data);
    }

    public ByteBuffer readAllCache() {
        byte[] data;
        if (cache.size() == 0)
            data = new byte[0];
        else if (cache.size() == 1) {
            try {
                data = cache.take();
            } catch (InterruptedException e) {
                data = new byte[0];
            }
        } else
            data = cache.stream().reduce((bytes, bytes2) -> {
                byte[] b = Arrays.copyOf(bytes, bytes.length + bytes2.length);
                System.arraycopy(bytes2, 0, b, bytes.length, bytes2.length);
                return b;
            }).orElse(new byte[0]);
        cache.clear();
        return ByteBuffer.wrap(data);
    }

    public ByteBuffer readElementCache() {
        byte[] data;
        if (cache.size() == 0)
            data = new byte[0];
        else
            try {
                data = cache.take();
            } catch (InterruptedException e) {
                data = new byte[0];
            }
        return ByteBuffer.wrap(data);
    }

    public void putTempMessage(ByteBuffer message) {
        tempBuffer.clear();
        tempBuffer.put(message);
    }

    public void putTempMessage(byte[] message) {
        tempBuffer.clear();
        tempBuffer.put(message);
    }

    public ByteBuffer retrieveTempMessage(){
        tempBuffer.flip();
        return tempBuffer;
    }

    public void clear(){
        cache.clear();
        tempBuffer.clear();
    }
}

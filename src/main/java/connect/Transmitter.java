package connect;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Transmitter {
    int read(ByteBuffer buffer) throws IOException;
    void write(ByteBuffer buffer) throws IOException;
}

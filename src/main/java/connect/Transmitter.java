package connect;

import java.io.IOException;

public interface Transmitter {
    int read(byte[] buffer) throws IOException;
    void write(byte[] buffer) throws IOException;
}

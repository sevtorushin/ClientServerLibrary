package connect;

import java.io.IOException;

public interface Transmitter<T> {
    int read(T buffer) throws IOException;
    void write(T buffer) throws IOException;
}

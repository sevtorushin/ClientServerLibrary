package connect;

import java.io.IOException;

public interface TCPConnection {
    boolean connect() throws IOException;
    boolean disconnect() throws IOException;
    boolean isConnected();
}

package connection;

import exceptions.DisconnectedException;

import java.io.IOException;

public interface Transmit {
    byte[] receiveBytes() throws IOException, DisconnectedException;
    void sendBytes(byte[] bytes) throws IOException;
}

package clients;

import java.net.Socket;

public interface Transmittable {
    void sendBytes(byte[] bytes);
}
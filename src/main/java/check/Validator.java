package check;

import clients.AbstractClient;

import java.net.Socket;

public interface Validator {
    boolean verify(byte[] data);
    boolean authorize(byte[] data);
    boolean authenticate(byte[] data);
}

package test;

import exceptions.DisconnectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public interface Receivable extends Cloneable {
    byte[] receiveBytes(String client);
}

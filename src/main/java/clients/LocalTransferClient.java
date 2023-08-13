package clients;

import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class LocalTransferClient extends TransferClient {
    private static final Logger log = LogManager.getLogger(LocalTransferClient.class.getSimpleName());

    public LocalTransferClient(String host, int port, String id) {
        super(host, port, id, null);
    }


    @Override
    public void sendBytes(byte[] bytes) {
        try {
            getConnection().getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

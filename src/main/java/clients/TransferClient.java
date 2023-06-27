package clients;

import check.KeyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.Receivable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TransferClient extends AbstractClient implements Transmittable, Receivable {
    private transient final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static final Logger log = LogManager.getLogger(TransferClient.class.getSimpleName());

    public TransferClient(String host, int port, String id, String keyFilePath) {
        super(host, port, id,
                new KeyManager(keyFilePath));
    }

    @Override
    public void sendBytes(byte[] bytes) {
        buffer.put((getId() + "\r\n").getBytes()).put((getSessionKey() + "\r\n\r\n").getBytes()).put(bytes);
        int pos = buffer.position();
        buffer.rewind();
        byte[] pack = new byte[pos];
        for (int i = 0; i < pos; i++) {
            pack[i] = buffer.get();
        }
        try {
            getOutStrm().write(pack);
            buffer.clear();
        } catch (SocketException e) {
            log.info("Connection was reset. Reconnect...", e);
            connectToServer();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException interruptedException) {
                log.error("Waiting client reconnect aborted", interruptedException);
            }
        } catch (IOException e) {
            log.error("Error sending data from client", e);
        }
    }

    @Override
    public byte[] receiveBytes(String source) {
        try {
            getInpStrm().read(buffer.array());
        } catch (IOException e) {
            log.error("Client reading error", e);
        }
        return buffer.array();
    }

    public void startTransferTo(String anotherHost, int anotherPort) {
        try {
            Socket anotherSocket = setSocket(anotherHost, anotherPort);
            InputStream is = getInpStrm();
            OutputStream os = anotherSocket.getOutputStream();
            new Thread(() -> {
                try {
                    is.transferTo(os);
                } catch (IOException e) {
                    log.error("Error sending data from client to another host " + anotherHost, e);
                }
            }).start();
        } catch (IOException e) {
            log.error("IO Exception sending data from client to another host " + anotherHost, e);
        }
    }

    public void startTransferFrom(String anotherHost, int anotherPort) {
        try {
            Socket anotherSocket = setSocket(anotherHost, anotherPort);
            if (anotherSocket == null) {
                log.error("Connection to node " + anotherHost + " " + anotherPort + " not established");
                return;
            }
            InputStream is = anotherSocket.getInputStream();
            OutputStream os = getOutStrm();
            new Thread(() -> {
                try {
                    is.transferTo(os);
                } catch (IOException e) {
                    log.error("Error sending data from client to another host " + anotherHost, e);
                }
            }).start();
        } catch (IOException e) {
            log.error("IO Exception sending data from client to another host " + anotherHost, e);
        }
    }
}

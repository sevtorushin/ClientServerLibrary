package clients;

import check.KeyManager;
import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.Receivable;
import utils.ArrayUtils;
import utils.ConnectionUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

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
        byte[] pack = ArrayUtils.arrayTrim(buffer.array());
        try {
            getConnection().getOutputStream().write(pack);
            buffer.clear();
        } catch (IOException e) {
            log.error("Error sending data from client", e);
        }
    }

    @Override
    public byte[] receiveBytes(String source) {
        try {
            getConnection().getInputStream().read(buffer.array());
        } catch (IOException e) {
            log.error("Client reading error", e);
        }
        return buffer.array();
    }

    @Override
    protected void loadSessionKey() {
        KeyManager keyManager = getKeyManager();
        String sessionKey = getSessionKey();
        keyManager.removeKey(sessionKey);
        sessionKey = keyManager.getKey();
        setSessionKey(sessionKey);
        log.debug("Session key loaded");
    }

    @Override
    protected boolean authorize() {
        loadSessionKey();
        KeyManager keyManager = getKeyManager();
        sendBytes(new byte[0]);
        log.debug("Client identity sent to server");
        keyManager.removeKey(getSessionKey());
        receiveBytes("");
        log.info(new String(buffer.array()));
        return false;
    }
}

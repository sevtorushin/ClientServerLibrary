package clients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LocalTransferClient extends TransferClient {
    private static final Logger log = LogManager.getLogger(LocalTransferClient.class.getSimpleName());

    public LocalTransferClient(String host, int port, String id) {
        super(host, port, id, null);
    }

    @Override
    public void connectToServer() {
        byte[] message = new byte[512];
        Socket socket;
        try {
            socket = setSocket(getHost(), getPort());
            setSocket(socket);
            log.debug("Connect to server " + getHost() + " " + " established");
            setSessionKey("constant");
            setInpStrm(socket.getInputStream());
            setOutStrm(socket.getOutputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            baos.writeTo(getOutStrm());
            log.debug("Client identity sent to server");
            socket.getInputStream().read(message);
            log.info(new String(message));
        } catch (IOException e) {
            log.error(e);
        }
    }
}

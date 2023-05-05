package servers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.*;

public class SIBMonitorSrv extends AbstractReceiveSrv {
    private final int cacheSize = 1_000_000;
    Validator validator = new SibValidator(this);
    private static final Logger log = LogManager.getLogger(SIBMonitorSrv.class.getSimpleName());

    public SIBMonitorSrv(int port) {
        super(port, 22);
        super.setValidator(validator);
    }

    public SIBMonitorSrv(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 22);
        super.setValidator(validator);
    }

    @Override
    protected void addToMap(Socket socket) {
        if (getSameMapSocket(socket) == null) {
            cachePool.put(socket.getInetAddress().toString(), new LinkedBlockingQueue<>(cacheSize));
            log.debug("Added unique socket " + socket.getInetAddress() + " to socketsCache");
        }
    }

    @Override
    protected boolean isClosedInputStream(InputStream is) throws IOException {
        byte[] buf = getBuffer();
        if (is.read(buf) == -1 || buf[0] == 4) {
            log.debug("InputStream closed");
            return true;
        }
        return false;
    }
}

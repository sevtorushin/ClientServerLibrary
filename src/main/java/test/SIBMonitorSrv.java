package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.*;

public class SIBMonitorSrv extends AbstractReceiveSrv {

    private static final Logger log = LogManager.getLogger(SIBMonitorSrv.class.getSimpleName());

    public SIBMonitorSrv(int port) {
        super(port, 22);
    }

    public SIBMonitorSrv(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 22);
    }

    @Override
    protected boolean isValidClient(Socket clientSocket) {
        boolean result = false;
        try {
            result = (byte) clientSocket.getInputStream().read() == -56;
            if (!result) {
                log.info("Unknown client " + clientSocket.getInetAddress() + " connection attempt...");
                return false;
            }
            for (Socket socket : clientPool) {
                if (socket == null) {
                    log.debug("Client " + clientSocket.getInetAddress() + " is valid");
                    return result;
                }
                if (socket.getInetAddress().equals(clientSocket.getInetAddress())) {
                    log.info("Starting a second Sib Monitor client from the " +
                            clientSocket.getInetAddress() + " ip address was rejected");
                    return false;
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        return result;
    }

    @Override
    protected void addToMap(Socket socket) {
        if (getSameMapSocket(socket) == null) {
            cachePool.put(socket.getInetAddress().toString(), new LinkedBlockingQueue<>());
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

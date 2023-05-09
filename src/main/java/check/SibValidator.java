package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.SIBMonitorSrv;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class SibValidator extends AbstractValidator {
    private final SIBMonitorSrv server;
    private static final Logger log = LogManager.getLogger(SibValidator.class.getSimpleName());

    public SibValidator(SIBMonitorSrv server) {
        this.server = server;
    }

    @Override
    public boolean authorize(Socket clientSocket) {
        byte result = 0;
        try {
            result = (byte) clientSocket.getInputStream().read();
        } catch (IOException e) {
            log.error("Read error from InputStream", e);
        }
        if (result != -56) {
            log.info("Unknown client " + clientSocket.getInetAddress() + " connection attempt...");
            return false;
        } else {
            log.debug("Client " + clientSocket.getInetAddress() + " has been authorized");
            return true;
        }
    }

    @Override
    public boolean authenticate(Socket client) {
        return true; //todo добавить логику аутентификации входящего клиента
    }

    @Override
    public boolean verify(Socket clientSocket) {
        List<Socket> clientPool = server.getClientPool();
        if (clientPool.isEmpty()) {
            log.debug("Client " + clientSocket.getInetAddress() + " has been verified");
            return true;
        }
        for (Socket socket : clientPool) {
            if (socket.getInetAddress().equals(clientSocket.getInetAddress())) {
                log.info("Starting a second Sib Monitor client from the " +
                        clientSocket.getInetAddress() + " ip address was rejected");
                return false;
            } else {
                log.debug("Client " + clientSocket.getInetAddress() + " has been verified");
                return true;
            }
        }
        return false;
    }
}

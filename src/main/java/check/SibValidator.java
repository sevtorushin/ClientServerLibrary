package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class SibValidator extends AbstractValidator {
    private static final Logger log = LogManager.getLogger(SibValidator.class.getSimpleName());

    public SibValidator() {
        super(new KeyManager("c:\\users\\public\\server_keys.txt"));
    }

    @Override
    public boolean authorize(byte[] data) {
        return true;
    }

    @Override
    public boolean authenticate(byte[] data) {
        if (data[0] != -56) {
            log.info("Unknown client connection attempt...");
            return false;
        }
        log.debug("Client has been authorized");
        return true;
    }

    @Override
    public boolean verify(byte[] data) {
//        List<Socket> clientPool = server.getClientPool();
//        if (clientPool.isEmpty()) {
//            log.debug("Client " + clientSocket.getInetAddress() + " has been verified");
//            return true;
//        }
//        for (Socket socket : clientPool) {
//            if (socket.getInetAddress().equals(clientSocket.getInetAddress())) {
//                log.info("Starting a second Sib Monitor client from the " +
//                        clientSocket.getInetAddress() + " ip address was rejected");
//                return false;
//            } else {
//                log.debug("Client " + clientSocket.getInetAddress() + " has been verified");
        return true;
//            }
//        }
//        return false;
    }
}

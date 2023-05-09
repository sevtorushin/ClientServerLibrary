package servers;

import check.MultifunctionalServerValidator;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class MultifunctionalServer extends AbstractReceiveSrv{
    private final int cacheSize = 1_000_000;

    public MultifunctionalServer(int port) {
        super(port, 512);
        super.setValidator(new MultifunctionalServerValidator(this));
    }

    public MultifunctionalServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512);
        super.setValidator(new MultifunctionalServerValidator(this));
    }

    @Override
    protected void addToMap(Socket clientSocket) {
        String clientIdentifier = getValidator().getClientIdentifier();
        if (getSameMapSocket(clientSocket) == null) {
            cachePool.put(clientSocket.getInetAddress().toString() + "/" +
                    clientIdentifier, new LinkedBlockingQueue<>(cacheSize));
//            log.debug("Added unique socket " + clientSocket.getInetAddress() + " to socketsCache");
        }
    }
}

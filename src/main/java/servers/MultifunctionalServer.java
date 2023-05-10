package servers;

import check.KeyManager;
import check.MultifunctionalServerValidator;
import check.SibValidator;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class MultifunctionalServer extends AbstractReceiveSrv{
    private final int cacheSize = 1_000_000;

    public MultifunctionalServer(int port) {
        super(port, 512,
                new MultifunctionalServerValidator(new KeyManager("C:\\Users\\Public\\server_keys.txt")));
    }

    public MultifunctionalServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512,
                new MultifunctionalServerValidator(new KeyManager("C:\\Users\\Public\\server_keys.txt")));
    }

    @Override
    protected void addToMap(Socket clientSocket) {
        if (getSameMapSocket(clientSocket) == null) {
            cachePool.put(clientSocket.getInetAddress().toString(), new LinkedBlockingQueue<>(cacheSize));
//            log.debug("Added unique socket " + clientSocket.getInetAddress() + " to socketsCache");
        }
    }
}

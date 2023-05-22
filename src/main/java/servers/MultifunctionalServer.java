package servers;

import check.KeyManager;
import check.MultifunctionalServerValidator;
import check.Validator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class MultifunctionalServer extends AbstractReceiveSrv {
    private final int cacheSize = 1_000_000;
    private static final Logger log = LogManager.getLogger(MultifunctionalServer.class.getSimpleName());

    public MultifunctionalServer(int port) {
        super(port, 512,
                new MultifunctionalServerValidator(new KeyManager("C:\\Users\\Public\\server_keys.txt")));
    }

    public MultifunctionalServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512,
                new MultifunctionalServerValidator(new KeyManager("C:\\Users\\Public\\server_keys.txt")));
    }

//    @Override
//    protected boolean addToMap(AbstractClient client) {
//        if (!cachePool.containsKey(client)) {
//            cachePool.put(client, new LinkedBlockingQueue<>(cacheSize));
//            log.debug("Added unique socket " + client.getHost() + " to socketsCache");
//            return true;
//        } else {
//            log.info("Starting a second Sib Monitor client from the " +
//                    client.getHost() + " address was rejected");
//            return false;
//        }
//    }

    @Override
    protected boolean validate(byte[] data) {
        Validator validator = getValidator();
        return validator.authenticate(data) && validator.authorize(data) &&
                validator.verify(data);
    }

    @Override
    protected AbstractClient getClient(byte[] data) {
        AbstractClient client = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            client = (AbstractClient) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return client;
    }
}

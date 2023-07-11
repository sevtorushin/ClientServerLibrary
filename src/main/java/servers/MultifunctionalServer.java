package servers;

import check.KeyManager;
import check.MultifunctionalServerValidator;
import check.Validator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class MultifunctionalServer extends AbstractReceiveSrv {
    private final int cacheSize = 1_000_000;
    private static final Logger log = LogManager.getLogger(MultifunctionalServer.class.getSimpleName());

    public MultifunctionalServer(int port, String keyFilePath) {
        super(port, 512,
                new MultifunctionalServerValidator(new KeyManager(keyFilePath)));
    }

    public MultifunctionalServer(int port, int maxNumberOfClient, String keyFilePath) {
        super(port, maxNumberOfClient, 512,
                new MultifunctionalServerValidator(new KeyManager(keyFilePath)));
    }

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
            log.debug("Initialize data from client is correct");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Unknown client", e);
        }
        return client;
    }

//    @Override
//    protected boolean isClosedInputStream(InputStream is) throws IOException {
//        byte[] buf = getBuffer();
//        if (is.read(buf) == -1 || buf[0] == 0) {
//            log.debug("InputStream closed");
//            return true;
//        }
//        return false;
//    }
}

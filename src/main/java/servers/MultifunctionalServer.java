package servers;

import check.KeyManager;
import check.MultifunctionalServerValidator;
import check.Validator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;

public class MultifunctionalServer extends AbstractReceiveSrv {
    private final int cacheSize = 1_000_000;
    private static final Properties props = new Properties();
    private static final Logger log = LogManager.getLogger(MultifunctionalServer.class.getSimpleName());

    {
        try {
            props.load(new FileInputStream("src\\main\\resources\\props.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MultifunctionalServer(int port) {
        super(port, 512,
                new MultifunctionalServerValidator(new KeyManager(props.getProperty("server.keyPath"))));
    }

    public MultifunctionalServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512,
                new MultifunctionalServerValidator(new KeyManager(props.getProperty("server.keyPath"))));
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
}

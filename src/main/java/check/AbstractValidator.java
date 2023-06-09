package check;

import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

public abstract class AbstractValidator implements Validator {
    private final KeyManager keyManager;
    private static final Logger log = LogManager.getLogger(AbstractValidator.class.getSimpleName());

    public AbstractValidator(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    @Override
    public boolean authenticate(byte[] data) {
        AbstractClient client = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            client = (AbstractClient) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.info("Unknown client connection attempt...");
            return false;
        }
        Set<String> keys = keyManager.getPublicKeys();
        if (keys.isEmpty()){
            keyManager.createKeyFile();
        }
            String key = client.getSessionKey();
            if (keys.contains(key)) {
                keyManager.removeKey(key);
                return true;
            }
        return false;
    }
}

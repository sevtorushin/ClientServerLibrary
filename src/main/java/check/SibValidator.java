package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SibValidator extends AbstractValidator {
    private static final Logger log = LogManager.getLogger(SibValidator.class.getSimpleName());

    public SibValidator() {
        super(new KeyManager(null));
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
        return true;
    }
}

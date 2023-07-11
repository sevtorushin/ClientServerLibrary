package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WITSValidator extends AbstractValidator {
    private static final Logger log = LogManager.getLogger(WITSValidator.class.getSimpleName());

    public WITSValidator() {
        super(null);
    }

    @Override
    public boolean verify(byte[] data) {
        return data[0] == 38 && data[1] == 38;
    }

    @Override
    public boolean authorize(byte[] data) {
        return true;
    }

    @Override
    public boolean authenticate(byte[] data) {
        if (data[0] != 38 && data[1] != 38) {
            log.info("Unknown client connection attempt...");
            return false;
        }
        log.debug("Client has been authorized");
        return true;
    }
}

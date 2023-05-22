package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.SIBMonitorSrv;

public class TransferClientValidator extends AbstractValidator {
    private static final Logger log = LogManager.getLogger(TransferClientValidator.class.getSimpleName());

    public TransferClientValidator(KeyManager keyManager) {
        super(keyManager);
    }

    @Override
    public boolean verify(byte[] data) {
        return true;
    }

    @Override
    public boolean authorize(byte[] data) {
        return true;
    }

    @Override
    public boolean authenticate(byte[] data) {
        return true;
    }
}

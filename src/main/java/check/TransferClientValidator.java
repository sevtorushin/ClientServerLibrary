package check;

import java.net.Socket;

public class TransferClientValidator extends AbstractValidator {
    public TransferClientValidator(KeyManager keyManager) {
        super(keyManager);
    }

    @Override
    public boolean verify(Socket client) {
        return true;
    }

    @Override
    public boolean authorize(Socket client) {
        return true;
    }
}

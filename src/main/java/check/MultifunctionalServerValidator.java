package check;

import java.net.Socket;

public class MultifunctionalServerValidator extends AbstractValidator{
    public MultifunctionalServerValidator(KeyManager keyManager) {
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

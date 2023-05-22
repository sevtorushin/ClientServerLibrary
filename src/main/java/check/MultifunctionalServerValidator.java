package check;

public class MultifunctionalServerValidator extends AbstractValidator{
    public MultifunctionalServerValidator(KeyManager keyManager) {
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


}

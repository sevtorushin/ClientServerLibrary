package check;

public class MultifunctionalServerValidator extends AbstractValidator{
    public MultifunctionalServerValidator(KeyManager keyManager) {
        super(keyManager);
    }

    @Override
    public boolean verify(byte[] data) {
        return data[0] != 0;
    }

    @Override
    public boolean authorize(byte[] data) {
        return true;
    }


}

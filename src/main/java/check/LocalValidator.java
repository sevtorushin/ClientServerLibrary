package check;

public class LocalValidator extends AbstractValidator {
    public LocalValidator() {
        super(null);
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

package check;

public class LocalValidator extends AbstractValidator {
    private SibValidator sibValidator = new SibValidator();
    private WITSValidator witsValidator = new WITSValidator();

    public LocalValidator() {
        super(null);
    }

    @Override
    public boolean verify(byte[] data) {
        return sibValidator.verify(data) || witsValidator.verify(data);
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

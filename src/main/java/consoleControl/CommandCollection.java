package consoleControl;

public enum CommandCollection {
    START("-start"),
    STOP("-stop"),
    GET("-get"),
    REMOVE("-remove"),
    READ("-read"),
    EXIT("-exit");

    private final String mnemonic;

    CommandCollection(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic(){
        return mnemonic;
    }
}

package consoleControl;

public enum CommandCollection {
    START("-start"),
    STOP("-stop"),
    GET("-get"),
    REMOVE("-remove"),
    READ("-read"),
    EXIT("-exit");

    private String mnemonic;

    CommandCollection(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic(){
        return mnemonic;
    }

    public void setMnemonic(CommandCollection c, String mnemonic){
        c.mnemonic = mnemonic;
    }
}

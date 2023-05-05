package servers;

public interface Receivable extends Cloneable {
    byte[] receiveBytes(String source);
}

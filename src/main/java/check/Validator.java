package check;

import java.net.Socket;

public interface Validator {
    boolean verify(Socket client);
    boolean authorize(Socket client);
    boolean authenticate(Socket client);
}

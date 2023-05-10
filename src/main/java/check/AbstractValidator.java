package check;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

public abstract class AbstractValidator implements Validator {
    private final KeyManager keyManager;

    public AbstractValidator(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    @Override
    public boolean authenticate(Socket clientSocket) {
        byte[] bytes = new byte[128];
        Set<String> keys = keyManager.getPublicKeys();
        if (keys.isEmpty()){
            keyManager.createKeyFile();
            System.err.println("Keys file updated");
        }
        try {
            clientSocket.getInputStream().read(bytes);
            String request = new String(bytes).trim();
            String key = request.substring(request.indexOf("\n")+1);
            if (keys.contains(key)) {
                keyManager.removeKey(key);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

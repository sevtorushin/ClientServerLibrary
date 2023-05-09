package check;

import clients.TransferClient;

import java.io.IOException;
import java.net.Socket;

public class ClientValidator extends AbstractValidator {
    private final TransferClient client;

    public ClientValidator(String path, TransferClient client) {
        loadPublicKeys(path);
        this.client = client;
    }

    @Override
    public boolean verify(Socket client) {
        return false;
    }

    @Override
    public boolean authorize(Socket inputSocket) {
        try {
            inputSocket.getOutputStream().write(("!!" + client.getIdentifier() + "!!").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean authenticate(Socket client) {
        String key = getPublicKeys().get(0);
        try {
            client.getOutputStream().write(key.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

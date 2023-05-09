package check;

import servers.MultifunctionalServer;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class MultifunctionalServerValidator extends AbstractValidator {
    private MultifunctionalServer server;

    public MultifunctionalServerValidator(MultifunctionalServer server) {
        this.server = server;
    }

    @Override
    public boolean verify(Socket client) {
        return true; //todo добавить логику аутентификации входящего клиента
    }

    @Override
    public boolean authorize(Socket client) {
        byte[] buffer = new byte[512];
        try {
            client.getInputStream().read(buffer);
            setClientIdentifier(new String(buffer));
            //todo добавить логику аутентификации входящего клиента
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean authenticate(Socket client) {
        byte[] buffer = new byte[512];
        loadPublicKeys("C:\\Users\\Public\\server_keys.txt");
        List<String> keys = getPublicKeys();
        try {
            client.getInputStream().read(buffer);
            String clientKey = new String(buffer).trim();
            for (String key : keys) {
                if (clientKey.equals(key)) {
                    System.out.println("Client key is valid");
                    return true;
                }
            }
            System.err.println("Client key is wrong");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

package servers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.Socket;
import java.util.ArrayList;

public class AbstractValidator implements Validator {
    private ArrayList<String> publicKeys = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(AbstractValidator.class.getSimpleName());
    @Override
    public boolean verify(Socket clientSocket) {
        //подтверждение выполнения неких требований
        return false;
    }

    @Override
    public boolean authorize(Socket clientSocket) {
        //предоставление определенных прав клиенту
        return false;
    }

    @Override
    public boolean authenticate(Socket clientSocket) {
        // проверка подлинности клиента
        log.debug("Client " + clientSocket.getInetAddress() + " has been authenticated");
        return true;
    }

    public void loadPublicKeys(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            reader.lines().forEach(publicKeys::add);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

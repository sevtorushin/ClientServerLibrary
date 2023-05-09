package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public abstract class AbstractValidator implements Validator {
    private ArrayList<String> publicKeys = new ArrayList<>();
    private String clientIdentifier;
    private static final Logger log = LogManager.getLogger(AbstractValidator.class.getSimpleName());

    public void loadPublicKeys(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            reader.lines().forEach(publicKeys::add);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getPublicKeys() {
        return publicKeys;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}

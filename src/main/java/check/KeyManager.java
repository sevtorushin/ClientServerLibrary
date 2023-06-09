package check;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class KeyManager {
    private final Set<String> publicKeys = new HashSet<>();
    private File keyFile;
    private int keyLength = 50;
    private int amountKey = 100;
    private static final Logger log = LogManager.getLogger(KeyManager.class.getSimpleName());

    public KeyManager(String keyPath) {
        if (keyPath == null) {
            log.debug("The path to the key file is not set");
            return;
        }
        this.keyFile = new File(keyPath);
        try {
            if (!keyFile.createNewFile())
                log.debug("Key file already exists");
        } catch (IOException e) {
            log.error("Key file access error", e);
        }
        loadKeys(keyPath);
    }

    public void loadKeys(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            reader.lines().forEach(publicKeys::add);
            log.debug("Keys from file loaded successfully");
        } catch (IOException e) {
            log.error("Key file IO error", e);
        }
    }

    public void removeKey(String key) {
        publicKeys.remove(key);
        log.debug("Specified key deleted successful");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            publicKeys.forEach(s -> {
                try {
                    writer.write(s + "\r\n");
                    log.debug("Key file updated successful");
                } catch (IOException e) {
                    log.error("Key file write error", e);
                }
            });
        } catch (IOException e) {
            log.error("Key file IO error", e);
        }
    }

    public void createKeyFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            String s;
            for (int i = 0; i < amountKey; i++) {
                s=generateKey();
                writer.write(s + "\r\n");
            }
            log.debug("Key file created successful");
        } catch (IOException e) {
            log.error("Key file IO error", e);;
        }
    }

    private String generateKey() {
        StringBuilder builder = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < keyLength; i++) {
            builder.append((char) (r.nextInt(93)+33));
        }
        return builder.toString().replace('\\', '/');
    }

    public String getKey() {
        return publicKeys.stream().findFirst().orElseThrow(() -> new RuntimeException("The keys are over"));
    }

    public Set<String> getPublicKeys() {
        return publicKeys;
    }
}

package check;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class KeyManager {
    private final Set<String> publicKeys = new HashSet<>();
    private final String keyPath;
    private File file;
    private int keyLength = 50;
    private int amountKey = 100;

    public KeyManager(String keyPath) {
        this.keyPath = keyPath;
        this.file = new File(keyPath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadKeys(keyPath);
    }

    public void loadKeys(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            reader.lines().forEach(publicKeys::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeKey(String key) {
        publicKeys.remove(key);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            publicKeys.forEach(s -> {
                try {
                    writer.write(s + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createKeyFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String s;
            for (int i = 0; i < amountKey; i++) {
                s=generateKey();
                writer.write(s + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        return publicKeys.stream().findFirst().orElseThrow(() -> new RuntimeException("Key limit exceeded"));
    }

    public Set<String> getPublicKeys() {
        return publicKeys;
    }
}

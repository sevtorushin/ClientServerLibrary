package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadProperties {
    private static ReadProperties instance;
    private final Properties properties;

    private ReadProperties() {
        properties = new Properties();
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("props.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static synchronized ReadProperties getInstance() {
        if (instance == null) {
            instance = new ReadProperties();
        }
        return instance;
    }

    public String getValue(String key) {
        return this.properties.getProperty(key, String.format("The key %s does not exists!", key));
    }
}

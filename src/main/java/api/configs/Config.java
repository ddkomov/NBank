package api.configs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new FileNotFoundException("config.properties file not found");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        //Приоритет 1 - это системное свойство baseApiUrl = ..
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        //Приоритет 2 - это переменная окружения BASEAPIURL
        //admin.username -> ADMIN_USERNAME
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return envValue;
        }
        //Приоритет 3 - это файл config.properties
        return INSTANCE.properties.getProperty(key);
    }
}

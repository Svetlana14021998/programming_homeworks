package config;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Getter
public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;

    private final int linkTtlDays;
    private final String linkPrefix;
    private final int codeLength;
    private final String linksFile;
    private final String usersFile;
    private final long cleanupInterval;
    private final TimeUnit cleanupTimeUnit;

    private AppConfig() {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Внимание: Файл " + CONFIG_FILE + " не найден в resources. Используются значения по умолчанию.");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении конфига: " + e.getMessage());
        }

        this.linkTtlDays = Integer.parseInt(properties.getProperty("link.ttl.days", "1"));
        this.linkPrefix = properties.getProperty("link.prefix", "my.short.link/");
        this.codeLength = Integer.parseInt(properties.getProperty("link.code.length", "6"));
        this.linksFile = properties.getProperty("links.file", "links.json");
        this.usersFile = properties.getProperty("users.file", "users.json");
        this.cleanupInterval = Long.parseLong(properties.getProperty("schedule.cleanup.interval", "30"));
        this.cleanupTimeUnit = parseTimeUnit(properties.getProperty("schedule.cleanup.time-unit"), TimeUnit.MINUTES);
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private TimeUnit parseTimeUnit(String unitStr, TimeUnit defaultUnit) {
        try {
            return TimeUnit.valueOf(unitStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultUnit;
        }
    }
}

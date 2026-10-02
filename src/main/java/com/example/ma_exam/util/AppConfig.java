package com.example.ma_exam.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Application configuration (database access, admin account).
 * Values come from environment variables first (e.g. DB_PASSWORD for db.password),
 * then from the git-ignored "config.properties" file in the working directory.
 * See config.properties.example.
 */
public final class AppConfig {
    private static final String DEFAULT_FILE = "config.properties";
    private static final Properties FILE_PROPERTIES =
            loadFrom(Path.of(System.getProperty("app.config", DEFAULT_FILE)));

    private AppConfig() {
    }

    /**
     * @return the configured value, or null if it is missing or blank
     */
    public static String get(String key) {
        return resolve(key, FILE_PROPERTIES, System.getenv());
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    static Properties loadFrom(Path path) {
        Properties properties = new Properties();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Impossible de lire " + path + ": " + e.getMessage());
            }
        }
        return properties;
    }

    static String resolve(String key, Properties properties, Map<String, String> env) {
        String envValue = env.get(toEnvName(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }
}

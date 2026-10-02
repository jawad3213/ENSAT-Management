package com.example.ma_exam.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void environmentVariableOverridesFile() {
        Properties file = new Properties();
        file.setProperty("db.password", "from-file");
        assertEquals("from-env", AppConfig.resolve("db.password", file, Map.of("DB_PASSWORD", "from-env")));
    }

    @Test
    void fileValueIsUsedWithoutEnvironmentVariable() {
        Properties file = new Properties();
        file.setProperty("app.admin.user", "  admin  ");
        assertEquals("admin", AppConfig.resolve("app.admin.user", file, Map.of()));
    }

    @Test
    void blankOrMissingValuesAreNull() {
        Properties file = new Properties();
        file.setProperty("db.password", "  ");
        assertNull(AppConfig.resolve("db.password", file, Map.of("DB_PASSWORD", "")));
        assertNull(AppConfig.resolve("db.url", file, Map.of()));
    }

    @Test
    void envNameIsDerivedFromKey() {
        assertEquals("APP_ADMIN_PASSWORD", AppConfig.toEnvName("app.admin.password"));
    }

    @Test
    void loadsPropertiesFileAndToleratesMissingFile(@TempDir Path dir) throws IOException {
        Path config = dir.resolve("config.properties");
        Files.writeString(config, "db.user=ensat\n");
        assertEquals("ensat", AppConfig.loadFrom(config).getProperty("db.user"));
        assertTrue(AppConfig.loadFrom(dir.resolve("missing.properties")).isEmpty());
    }
}

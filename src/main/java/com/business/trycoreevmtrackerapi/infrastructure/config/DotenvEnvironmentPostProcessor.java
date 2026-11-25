package com.business.trycoreevmtrackerapi.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DotenvEnvironmentPostProcessor {

    private DotenvEnvironmentPostProcessor() {
    }

    public static void loadIntoSystemProperties() {
        Path rootEnv = Path.of(".env");

        if (!Files.exists(rootEnv)) {
            throw new IllegalStateException("File not found .env at path: " + rootEnv.toAbsolutePath());
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }
        });
    }
}

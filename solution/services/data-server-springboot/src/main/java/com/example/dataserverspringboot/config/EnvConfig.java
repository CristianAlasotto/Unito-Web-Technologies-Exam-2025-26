package com.example.dataserverspringboot.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class to load .env file from outside the project directory
 */
@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = null;
        String loadedFrom = null;

        try {
            // Option 1: Load from custom path if ENV_FILE is set
            String customPath = System.getenv("ENV_FILE");
            if (customPath != null && !customPath.isEmpty()) {
                File envFile = new File(customPath);
                if (envFile.exists()) {
                    dotenv = Dotenv.configure()
                            .directory(envFile.getParent())
                            .filename(envFile.getName())
                            .load();
                    loadedFrom = customPath;
                }
            }

            // Option 2: Try parent directory (../.env)
            if (dotenv == null) {
                Path parentPath = Paths.get("..").toAbsolutePath().normalize();
                File parentEnv = new File(parentPath.toFile(), ".env");
                if (parentEnv.exists()) {
                    dotenv = Dotenv.configure()
                            .directory(parentPath.toString())
                            .load();
                    loadedFrom = parentEnv.getAbsolutePath();
                }
            }

            // Option 3: Try two levels up (../../.env)
            if (dotenv == null) {
                Path grandparentPath = Paths.get("../..").toAbsolutePath().normalize();
                File grandparentEnv = new File(grandparentPath.toFile(), ".env");
                if (grandparentEnv.exists()) {
                    dotenv = Dotenv.configure()
                            .directory(grandparentPath.toString())
                            .load();
                    loadedFrom = grandparentEnv.getAbsolutePath();
                }
            }

            // Option 4: Try project root (fallback)
            if (dotenv == null) {
                dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                loadedFrom = "project root";
            }

            // Load all environment variables into system properties
            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    System.setProperty(entry.getKey(), entry.getValue());
                });

                System.out.println("✅ .env file loaded successfully from: " + loadedFrom);

                // Show the CORRECT variable names from your .env
                System.out.println("   SPRING_DATASOURCE_URL: " + dotenv.get("SPRING_DATASOURCE_URL", "not set"));
                System.out.println("   SPRING_DATASOURCE_USERNAME: " + dotenv.get("SPRING_DATASOURCE_USERNAME", "not set"));
                System.out.println("   SPRING_DATASOURCE_PASSWORD: " + (dotenv.get("SPRING_DATASOURCE_PASSWORD") != null ? "***SET***" : "not set"));
            }

        } catch (Exception e) {
            System.out.println("⚠️  Could not load .env file: " + e.getMessage());
            System.out.println("   Using default values from application.properties");
        }
    }
}
package com.campusvoice.ai;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AIConfig {
    public static final String APP_VERSION = "1.0.0";
    public static final double CONFIDENCE_THRESHOLD = 0.75; // 75%
    public static final int DEFAULT_SERVER_PORT = 8000;

    public static final Path BASE_DIR = Paths.get("").toAbsolutePath();
    public static final Path MODELS_DIR = BASE_DIR.resolve("models");
    public static final Path TRAINING_DIR = BASE_DIR.resolve("training");

    public static int getServerPort() {
        String envPort = System.getenv("AI_SERVICE_PORT");
        if (envPort == null || envPort.isBlank()) {
            envPort = System.getenv("PORT");
        }
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_SERVER_PORT;
    }

    public static double getConfidenceThreshold() {
        String envThresh = System.getenv("AI_CONFIDENCE_THRESHOLD");
        if (envThresh != null && !envThresh.isBlank()) {
            try {
                return Double.parseDouble(envThresh.trim());
            } catch (NumberFormatException ignored) {}
        }
        return CONFIDENCE_THRESHOLD;
    }
}

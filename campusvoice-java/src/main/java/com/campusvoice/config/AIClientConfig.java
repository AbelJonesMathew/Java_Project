package com.campusvoice.config;

public class AIClientConfig {
    private static String aiServiceUrl = System.getenv("AI_SERVICE_URL");

    static {
        if (aiServiceUrl == null || aiServiceUrl.isBlank()) {
            aiServiceUrl = "http://localhost:8000";
        }
    }

    public static String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public static String getAnalyzePriorityEndpoint() {
        return aiServiceUrl + "/ai/analyze-priority";
    }

    public static String getDetectLanguageEndpoint() {
        return aiServiceUrl + "/ai/detect-language";
    }

    public static String getTranslateEndpoint() {
        return aiServiceUrl + "/ai/translate";
    }

    public static String getHealthEndpoint() {
        return aiServiceUrl + "/ai/health";
    }
}

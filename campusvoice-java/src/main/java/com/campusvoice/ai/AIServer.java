package com.campusvoice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class AIServer {
    private static final Logger logger = LoggerFactory.getLogger(AIServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final int port;
    private HttpServer server;
    private final PriorityService priorityService;

    public AIServer(int port) {
        this.port = port;
        this.priorityService = new PriorityService();
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        // Register REST API endpoints matching Python FastAPI app.py
        server.createContext("/ai/health", new HealthHandler());
        server.createContext("/ai/analyze-priority", new AnalyzePriorityHandler());
        server.createContext("/ai/detect-language", new DetectLanguageHandler());
        server.createContext("/ai/translate", new TranslateHandler());

        server.start();
        logger.info("CampusVoice AI Java Server running on http://localhost:{}/ (Zero-Python Native Mode)", port);
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            logger.info("CampusVoice AI Server stopped.");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        byte[] bytes = mapper.writeValueAsBytes(data);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2048];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static byte[] readRawBytes(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2048];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        }
    }

    // 1. GET /ai/health
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, Collections.emptyMap());
                return;
            }
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "healthy");
            resp.put("service", "CampusVoice AI Java Microservice");
            resp.put("version", AIConfig.APP_VERSION);
            resp.put("model_loaded", priorityService.isModelLoaded());
            sendJsonResponse(exchange, 200, resp);
        }
    }

    // 2. POST /ai/analyze-priority
    private class AnalyzePriorityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, Collections.emptyMap());
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
                return;
            }

            try {
                String body = readBody(exchange);
                JsonNode root = mapper.readTree(body);

                String text = root.has("text") ? root.get("text").asText() : "";
                String category = root.has("category") ? root.get("category").asText() : "General";
                String language = root.has("language") ? root.get("language").asText() : "English";

                if (text.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, Map.of("detail", "Complaint text cannot be empty."));
                    return;
                }

                PriorityService.AIAnalysisResult analysis = priorityService.analyzeComplaint(text, category, language);

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("priority", analysis.getPriority());
                resp.put("confidence", analysis.getConfidence());
                resp.put("reason", analysis.getReason());
                resp.put("detected_language", analysis.getDetectedLanguage());
                resp.put("translated_text", analysis.getTranslatedText());
                resp.put("risk_factors", analysis.getRiskFactors());
                resp.put("human_review_required", analysis.isHumanReviewRequired());

                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                logger.error("Analyze priority handler error: {}", e.getMessage());
                sendJsonResponse(exchange, 500, Map.of("error", e.getMessage()));
            }
        }
    }

    // 3. POST /ai/detect-language
    private class DetectLanguageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, Collections.emptyMap());
                return;
            }

            try {
                String body = readBody(exchange);
                JsonNode root = mapper.readTree(body);
                String text = root.has("text") ? root.get("text").asText() : "";

                LanguageService.LanguageDetectionResult res = LanguageService.detectLanguage(text);

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("language_code", res.getLanguageCode());
                resp.put("language_name", res.getLanguageName());
                resp.put("confidence", res.getConfidence());

                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, Map.of("error", e.getMessage()));
            }
        }
    }

    // 4. POST /ai/translate
    private class TranslateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, Collections.emptyMap());
                return;
            }

            try {
                String body = readBody(exchange);
                JsonNode root = mapper.readTree(body);

                String text = root.has("text") ? root.get("text").asText() : "";
                String sourceLang = root.has("source_language") && !root.get("source_language").isNull()
                        ? root.get("source_language").asText() : null;
                String targetLang = root.has("target_language") ? root.get("target_language").asText() : "English";

                LanguageService.TranslationResult res = LanguageService.translateToEnglish(text, sourceLang);

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("original_text", res.getOriginalText());
                resp.put("translated_text", res.getTranslatedText());
                resp.put("source_language", res.getSourceLanguage());
                resp.put("target_language", targetLang);

                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, Map.of("error", e.getMessage()));
            }
        }
    }

    public static void main(String[] args) {
        int port = AIConfig.getServerPort();
        System.out.println("==================================================");
        System.out.println("     CampusVoice AI Microservice (Java Edition)   ");
        System.out.println("==================================================");
        try {
            AIServer server = new AIServer(port);
            server.start();
            System.out.println("Server started successfully on port " + port);
            System.out.println("Health check: http://localhost:" + port + "/ai/health");
            System.out.println("Press Ctrl+C to stop.");
        } catch (Exception e) {
            System.err.println("Failed to start AI Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.campusvoice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campusvoice.config.AIClientConfig;
import com.campusvoice.model.AIAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorityService {
    private static final Logger logger = LoggerFactory.getLogger(PriorityService.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final com.campusvoice.ai.PriorityService nativeAiService = new com.campusvoice.ai.PriorityService();

    public AIAnalysis analyzePriority(String text, String category, String language) {
        AIAnalysis result = new AIAnalysis();
        result.setDetectedLanguage(language != null ? language : "English");
        result.setModelVersion("v1.0.0-java");

        // 1. First evaluate with native Java AI engine (TF-IDF + Softmax + Safety Overrides)
        try {
            com.campusvoice.ai.PriorityService.AIAnalysisResult nativeResult =
                    nativeAiService.analyzeComplaint(text, category, language);

            result.setOriginalPriority(nativeResult.getPriority());
            result.setFinalPriority(nativeResult.getPriority());
            result.setConfidence(nativeResult.getConfidence());
            result.setReason(nativeResult.getReason());
            result.setDetectedLanguage(nativeResult.getDetectedLanguage());
            result.setRiskFactors(nativeResult.getRiskFactors());
            logger.info("Native Java AI evaluated Priority: {} ({}%)", nativeResult.getPriority(), nativeResult.getConfidence());
            return result;
        } catch (Exception e) {
            logger.warn("Native Java AI evaluation encountered an issue: {}. Using fallback rule engine.", e.getMessage());
        }

        // Local Rule-Based Fallback Engine
        return localFallbackAnalysis(text, category, language);
    }

    private AIAnalysis localFallbackAnalysis(String text, String category, String language) {
        AIAnalysis result = new AIAnalysis();
        result.setDetectedLanguage(language != null ? language : "English");

        String lowerText = (category + " " + text).toLowerCase();
        List<String> risks = new ArrayList<>();

        if (lowerText.contains("wire") || lowerText.contains("electric") || lowerText.contains("shock") ||
            lowerText.contains("fire") || lowerText.contains("ragging") || lowerText.contains("harassment") || lowerText.contains("leak")) {
            result.setOriginalPriority("HIGH");
            result.setFinalPriority("HIGH");
            result.setConfidence(95.0);
            result.setReason("Potential safety hazard detected via keyword patterns.");
            risks.add("• Critical Hazard / Safety keyword detected");
            risks.add("• Priority override applied");
        } else if (lowerText.contains("broken") || lowerText.contains("not working") || lowerText.contains("delay") || lowerText.contains("canteen")) {
            result.setOriginalPriority("MEDIUM");
            result.setFinalPriority("MEDIUM");
            result.setConfidence(85.0);
            result.setReason("Standard operational issue.");
            risks.add("• Facility equipment repair needed");
        } else {
            result.setOriginalPriority("LOW");
            result.setFinalPriority("LOW");
            result.setConfidence(90.0);
            result.setReason("General feedback request.");
            risks.add("• Low immediate impact");
        }
        result.setRiskFactors(risks);
        return result;
    }
}

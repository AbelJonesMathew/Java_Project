package com.campusvoice.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AIServiceTest {
    private static PriorityService priorityService;

    @BeforeAll
    public static void setUp() {
        priorityService = new PriorityService();
    }

    @Test
    public void testHealthCheck() {
        assertNotNull(priorityService, "PriorityService should be initialized");
        assertTrue(priorityService.isModelLoaded(), "Model should be successfully trained and loaded");
    }

    @Test
    public void testElectricalHazardOverride() {
        String complaintText = "The electrical wire in the lab is exposed and students could get an electric shock.";
        PriorityService.AIAnalysisResult result = priorityService.analyzeComplaint(
                complaintText,
                "Electricity",
                "English"
        );

        assertNotNull(result);
        assertEquals("HIGH", result.getPriority(), "Electrical hazard must trigger HIGH priority override");
        assertTrue(result.getConfidence() >= 95.0, "Confidence should be >= 95% for safety hazard");
        assertTrue(result.getReason().toLowerCase().contains("safety hazard") ||
                   result.getRiskFactors().toString().toLowerCase().contains("electrical hazard"),
                "Reason or risk factors must describe the safety hazard");
        assertFalse(result.isHumanReviewRequired(), "Safety override does not require manual review");
    }

    @Test
    public void testLanguageDetectionMalayalam() {
        String text = "ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.";
        LanguageService.LanguageDetectionResult result = LanguageService.detectLanguage(text);

        assertNotNull(result);
        assertEquals("Malayalam", result.getLanguageName(), "Should detect Malayalam script");
        assertEquals("ml", result.getLanguageCode());
        assertTrue(result.getConfidence() >= 0.95);
    }

    @Test
    public void testTranslationMalayalam() {
        String text = "ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.";
        LanguageService.TranslationResult result = LanguageService.translateToEnglish(text, "Malayalam");

        assertNotNull(result);
        assertTrue(result.getTranslatedText().toLowerCase().contains("projector"),
                "Translated text should contain 'projector'");
        assertEquals("English", result.getTargetLanguage());
    }

    @Test
    public void testHindiLanguageDetectionAndTranslation() {
        String text = "गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।";
        LanguageService.LanguageDetectionResult langRes = LanguageService.detectLanguage(text);
        assertEquals("Hindi", langRes.getLanguageName());

        LanguageService.TranslationResult transRes = LanguageService.translateToEnglish(text, "Hindi");
        assertTrue(transRes.getTranslatedText().toLowerCase().contains("hostel") ||
                   transRes.getTranslatedText().toLowerCase().contains("light"));
    }

    @Test
    public void testTamilLanguageDetection() {
        String text = "ஹாஸ்டலில் குடிநீர் விநியோகம் இல்லை.";
        LanguageService.LanguageDetectionResult langRes = LanguageService.detectLanguage(text);
        assertEquals("Tamil", langRes.getLanguageName());
        assertEquals("ta", langRes.getLanguageCode());
    }
}

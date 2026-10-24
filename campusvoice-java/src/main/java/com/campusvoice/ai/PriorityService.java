package com.campusvoice.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PriorityService {
    private static final Logger logger = LoggerFactory.getLogger(PriorityService.class);

    public static class HazardInfo {
        private final String riskTitle;
        private final String riskDescription;

        public HazardInfo(String riskTitle, String riskDescription) {
            this.riskTitle = riskTitle;
            this.riskDescription = riskDescription;
        }

        public String getRiskTitle() { return riskTitle; }
        public String getRiskDescription() { return riskDescription; }
    }

    public static class AIAnalysisResult {
        private String priority;
        private double confidence;
        private String reason;
        private String detectedLanguage;
        private String translatedText;
        private List<String> riskFactors;
        private boolean humanReviewRequired;

        public AIAnalysisResult() {
            this.riskFactors = new ArrayList<>();
        }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getDetectedLanguage() { return detectedLanguage; }
        public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }

        public String getTranslatedText() { return translatedText; }
        public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

        public boolean isHumanReviewRequired() { return humanReviewRequired; }
        public void setHumanReviewRequired(boolean humanReviewRequired) { this.humanReviewRequired = humanReviewRequired; }
    }

    // Critical Safety Keywords that trigger instant HIGH Priority Overrides
    private static final Map<String, HazardInfo> CRITICAL_SAFETY_KEYWORDS = new LinkedHashMap<>();

    static {
        CRITICAL_SAFETY_KEYWORDS.put("electrical shock", new HazardInfo("Electrical hazard", "Risk of electrocution to students"));
        CRITICAL_SAFETY_KEYWORDS.put("electric shock", new HazardInfo("Electrical hazard", "Risk of electrocution to students"));
        CRITICAL_SAFETY_KEYWORDS.put("exposed wire", new HazardInfo("Electrical hazard", "Exposed live wiring in campus facility"));
        CRITICAL_SAFETY_KEYWORDS.put("live wire", new HazardInfo("Electrical hazard", "Exposed live electric wire"));
        CRITICAL_SAFETY_KEYWORDS.put("sparking", new HazardInfo("Electrical hazard", "Electrical sparking in facility"));
        CRITICAL_SAFETY_KEYWORDS.put("fire", new HazardInfo("Fire hazard", "Potential fire outbreak risk"));
        CRITICAL_SAFETY_KEYWORDS.put("gas leak", new HazardInfo("Chemical hazard", "LPG/Gas leak detected"));
        CRITICAL_SAFETY_KEYWORDS.put("weapon", new HazardInfo("Security hazard", "Weapon reported on campus premises"));
        CRITICAL_SAFETY_KEYWORDS.put("violence", new HazardInfo("Security hazard", "Physical violence reported"));
        CRITICAL_SAFETY_KEYWORDS.put("ragging", new HazardInfo("Zero-Tolerance violation", "Ragging incident reported"));
        CRITICAL_SAFETY_KEYWORDS.put("harassment", new HazardInfo("Student safety threat", "Harassment or intimidation reported"));
        CRITICAL_SAFETY_KEYWORDS.put("threatened", new HazardInfo("Security hazard", "Student intimidation or threat"));
        CRITICAL_SAFETY_KEYWORDS.put("medical emergency", new HazardInfo("Health hazard", "Severe medical emergency"));
        CRITICAL_SAFETY_KEYWORDS.put("collapse", new HazardInfo("Structural hazard", "Building/ceiling structural damage"));
        CRITICAL_SAFETY_KEYWORDS.put("fell off", new HazardInfo("Structural hazard", "Fallen equipment or fixture"));
        CRITICAL_SAFETY_KEYWORDS.put("flooding", new HazardInfo("Infrastructure hazard", "Water pipe burst or severe flooding"));
        CRITICAL_SAFETY_KEYWORDS.put("spoiled food", new HazardInfo("Health hazard", "Food poisoning or severe mess contamination"));
        CRITICAL_SAFETY_KEYWORDS.put("darkness", new HazardInfo("Security hazard", "Severe dark spot / unlit hostel area"));
        CRITICAL_SAFETY_KEYWORDS.put("dirty water", new HazardInfo("Health hazard", "Contaminated drinking water supply"));
    }

    private TfidfVectorizer vectorizer;
    private TextClassifier classifier;
    private boolean modelLoaded = false;

    public PriorityService() {
        loadModels();
    }

    public synchronized void loadModels() {
        try {
            TrainModel.TrainedAIModel trained = TrainModel.trainPriorityModel();
            this.vectorizer = trained.getVectorizer();
            this.classifier = trained.getClassifier();
            this.modelLoaded = vectorizer != null && classifier != null && vectorizer.getVocabularySize() > 0;
            logger.info("Java PriorityService initialized with native ML model ({} vocabulary features, {}% accuracy).",
                    vectorizer != null ? vectorizer.getVocabularySize() : 0, trained.getTrainingAccuracy());
        } catch (Exception e) {
            logger.warn("Could not train in-memory ML model: {}. Relying on rule-based safety engine.", e.getMessage());
            this.modelLoaded = false;
        }
    }

    public AIAnalysisResult analyzeComplaint(String text, String category, String language) {
        AIAnalysisResult result = new AIAnalysisResult();

        if (text == null) text = "";
        if (category == null) category = "General";

        // 1. Detect language and translate if needed
        LanguageService.LanguageDetectionResult langResult = LanguageService.detectLanguage(text);
        String detectedLangName = langResult.getLanguageName();
        LanguageService.TranslationResult transResult = LanguageService.translateToEnglish(text, detectedLangName);
        String translatedText = transResult.getTranslatedText();

        result.setDetectedLanguage(detectedLangName);
        result.setTranslatedText(translatedText);

        String cleanEvalText = (category + " " + translatedText + " " + text).toLowerCase();

        // 2. Rule-Based Safety Engine Check
        List<String> matchedHazards = new ArrayList<>();
        List<String> riskFactors = new ArrayList<>();

        for (Map.Entry<String, HazardInfo> entry : CRITICAL_SAFETY_KEYWORDS.entrySet()) {
            if (cleanEvalText.contains(entry.getKey())) {
                matchedHazards.add(entry.getKey());
                riskFactors.add("• " + entry.getValue().getRiskTitle() + ": " + entry.getValue().getRiskDescription());
            }
        }

        if (!matchedHazards.isEmpty()) {
            String reason = "Potential safety hazard detected. The complaint mentions " + String.join(", ", matchedHazards)
                    + " which presents an immediate risk to campus safety and student well-being.";
            result.setPriority("HIGH");
            result.setConfidence(96.0);
            result.setReason(reason);
            result.setRiskFactors(riskFactors);
            result.setHumanReviewRequired(false);
            return result;
        }

        // 3. Machine Learning Classification Check (TF-IDF + Softmax)
        if (modelLoaded && vectorizer != null && classifier != null) {
            try {
                String fullFeature = category + " " + translatedText;
                double[] xVec = vectorizer.transform(fullFeature);
                TextClassifier.PredictionResult pred = classifier.predict(xVec);

                String predictedPriority = pred.getPriority();
                double confidenceScore = pred.getConfidence();

                String reasonDesc;
                switch (predictedPriority) {
                    case "HIGH":
                        reasonDesc = "Critical complaint evaluated in category '" + category + "' with high severity indicators.";
                        break;
                    case "MEDIUM":
                        reasonDesc = "Operational complaint evaluated in category '" + category + "' requiring timely department resolution.";
                        break;
                    case "LOW":
                    default:
                        reasonDesc = "General request or low-impact feedback in category '" + category + "'.";
                        break;
                }

                boolean humanReview = confidenceScore < (AIConfig.getConfidenceThreshold() * 100.0);

                List<String> rf = new ArrayList<>();
                rf.add("• Evaluated Priority: " + predictedPriority);
                rf.add("• Category Impact: " + category);
                rf.add(String.format("• Classification Confidence: %.1f%%", confidenceScore));

                result.setPriority(predictedPriority);
                result.setConfidence(confidenceScore);
                result.setReason(reasonDesc);
                result.setRiskFactors(rf);
                result.setHumanReviewRequired(humanReview);
                return result;
            } catch (Exception e) {
                logger.error("ML Prediction error: {}", e.getMessage());
            }
        }

        // 4. Fallback Heuristic
        result.setPriority("MEDIUM");
        result.setConfidence(75.0);
        result.setReason("Default priority assigned for category '" + category + "'. Human review recommended.");
        result.setRiskFactors(Collections.singletonList("• Standard operational issue in " + category));
        result.setHumanReviewRequired(true);
        return result;
    }

    public boolean isModelLoaded() {
        return modelLoaded;
    }
}

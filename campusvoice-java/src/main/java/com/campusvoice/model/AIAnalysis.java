package com.campusvoice.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AIAnalysis {
    private String id;
    private String complaintId;
    private String detectedLanguage;
    private String originalPriority;
    private String finalPriority;
    private double confidence;
    private String reason;
    private List<String> riskFactors = new ArrayList<>();
    private boolean humanOverridden;
    private String modelVersion;
    private Timestamp analyzedAt;

    public AIAnalysis() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }

    public String getOriginalPriority() { return originalPriority; }
    public void setOriginalPriority(String originalPriority) { this.originalPriority = originalPriority; }

    public String getFinalPriority() { return finalPriority; }
    public void setFinalPriority(String finalPriority) { this.finalPriority = finalPriority; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public boolean isHumanOverridden() { return humanOverridden; }
    public void setHumanOverridden(boolean humanOverridden) { this.humanOverridden = humanOverridden; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Timestamp getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Timestamp analyzedAt) { this.analyzedAt = analyzedAt; }
}

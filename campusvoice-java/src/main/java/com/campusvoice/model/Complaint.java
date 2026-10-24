package com.campusvoice.model;

import java.sql.Timestamp;

public class Complaint {
    private String id;
    private String complaintCode;
    private String studentId;
    private String studentName;
    private String title;
    private String description;
    private String originalText;
    private String originalLanguage;
    private String translatedText;
    private int categoryId;
    private String categoryName;
    private String priority; // LOW, MEDIUM, HIGH
    private double priorityConfidence;
    private String aiReason;
    private String status; // SUBMITTED, UNDER REVIEW, ASSIGNED, IN PROGRESS, ESCALATED, RESOLVED, REJECTED, CLOSED
    private boolean anonymous;
    private boolean groupComplaint;
    private String assignedTo;
    private String assignedToName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

    public Complaint() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComplaintCode() { return complaintCode; }
    public void setComplaintCode(String complaintCode) { this.complaintCode = complaintCode; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() {
        if (anonymous) return "Anonymous Student";
        return studentName != null ? studentName : "Student";
    }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public double getPriorityConfidence() { return priorityConfidence; }
    public void setPriorityConfidence(double priorityConfidence) { this.priorityConfidence = priorityConfidence; }

    public String getAiReason() { return aiReason; }
    public void setAiReason(String aiReason) { this.aiReason = aiReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public boolean isGroupComplaint() { return groupComplaint; }
    public void setGroupComplaint(boolean groupComplaint) { this.groupComplaint = groupComplaint; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}

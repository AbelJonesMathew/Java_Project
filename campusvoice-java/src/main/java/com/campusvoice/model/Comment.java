package com.campusvoice.model;

import java.sql.Timestamp;

public class Comment {
    private String id;
    private String complaintId;
    private String userId;
    private String userName;
    private String userRole;
    private String commentText;
    private boolean internal;
    private Timestamp createdAt;

    public Comment() {}

    public Comment(String complaintId, String userId, String commentText, boolean internal) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.commentText = commentText;
        this.internal = internal;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public boolean isInternal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

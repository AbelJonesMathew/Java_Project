package com.campusvoice.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroupComplaint {
    private String id;
    private String complaintId;
    private String groupCode;
    private String creatorStudentId;
    private String title;
    private int targetSupportersCount;
    private int currentSupportersCount;
    private List<String> memberStudentIds = new ArrayList<>();
    private Timestamp createdAt;

    public GroupComplaint() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getCreatorStudentId() { return creatorStudentId; }
    public void setCreatorStudentId(String creatorStudentId) { this.creatorStudentId = creatorStudentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getTargetSupportersCount() { return targetSupportersCount; }
    public void setTargetSupportersCount(int targetSupportersCount) { this.targetSupportersCount = targetSupportersCount; }

    public int getCurrentSupportersCount() { return currentSupportersCount; }
    public void setCurrentSupportersCount(int currentSupportersCount) { this.currentSupportersCount = currentSupportersCount; }

    public List<String> getMemberStudentIds() { return memberStudentIds; }
    public void setMemberStudentIds(List<String> memberStudentIds) { this.memberStudentIds = memberStudentIds; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

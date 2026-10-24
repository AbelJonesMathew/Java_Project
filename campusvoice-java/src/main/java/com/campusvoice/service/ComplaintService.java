package com.campusvoice.service;

import com.campusvoice.dao.AuditLogDAO;
import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.dao.impl.AuditLogDAOImpl;
import com.campusvoice.dao.impl.ComplaintDAOImpl;
import com.campusvoice.model.AuditLog;
import com.campusvoice.model.Complaint;
import com.campusvoice.security.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ComplaintService {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    private final ComplaintDAO complaintDAO;
    private final AuditLogDAO auditLogDAO;

    public ComplaintService() {
        this.complaintDAO = new ComplaintDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public boolean createComplaint(Complaint complaint) {
        if (complaint == null || complaint.getTitle() == null || complaint.getTitle().isBlank()) {
            return false;
        }

        boolean success = complaintDAO.createComplaint(complaint);
        if (success) {
            String currentUserId = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getId() : complaint.getStudentId();
            auditLogDAO.logAction(new AuditLog(currentUserId, "CREATE_COMPLAINT", "Complaint", complaint.getId(), "Created complaint " + complaint.getComplaintCode()));
        }
        return success;
    }

    public Optional<Complaint> getComplaintById(String id) {
        return complaintDAO.findById(id);
    }

    public Optional<Complaint> getComplaintByCode(String code) {
        return complaintDAO.findByCode(code);
    }

    public List<Complaint> getComplaintsForStudent(String studentId) {
        return complaintDAO.findByStudentId(studentId);
    }

    public List<Complaint> getComplaintsForDepartment(int deptId) {
        return complaintDAO.findByDepartmentId(deptId);
    }

    public List<Complaint> getComplaintsAssignedTo(String userId) {
        return complaintDAO.findByAssignedTo(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintDAO.findAll();
    }

    public List<Complaint> searchComplaints(String keyword, String priority, String status, Integer categoryId) {
        return complaintDAO.searchComplaints(keyword, priority, status, categoryId);
    }

    public boolean updateStatus(String complaintId, String newStatus, String remark) {
        String userId = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getId() : null;
        boolean res = complaintDAO.updateStatus(complaintId, newStatus, remark, userId);
        if (res && userId != null) {
            auditLogDAO.logAction(new AuditLog(userId, "UPDATE_STATUS", "Complaint", complaintId, "Status updated to " + newStatus));
        }
        return res;
    }

    public boolean updateComplaintStatus(String codeOrId, String newStatus, String remark) {
        Optional<Complaint> opt = complaintDAO.findByCode(codeOrId);
        String targetId = opt.isPresent() ? opt.get().getId() : codeOrId;
        return updateStatus(targetId, newStatus, remark);
    }

    public boolean updatePriority(String complaintId, String newPriority, String reason) {
        String userId = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getId() : null;
        boolean res = complaintDAO.updatePriority(complaintId, newPriority, reason);
        if (res && userId != null) {
            auditLogDAO.logAction(new AuditLog(userId, "CHANGE_PRIORITY", "Complaint", complaintId, "Priority changed to " + newPriority));
        }
        return res;
    }

    public boolean assignComplaint(String complaintId, String assignedToUserId, String remark) {
        String assignedBy = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getId() : null;
        boolean res = complaintDAO.assignComplaint(complaintId, assignedToUserId, assignedBy, remark);
        if (res && assignedBy != null) {
            auditLogDAO.logAction(new AuditLog(assignedBy, "ASSIGN_COMPLAINT", "Complaint", complaintId, "Assigned to user " + assignedToUserId));
        }
        return res;
    }

    public long getTotalCount() {
        return complaintDAO.countTotal();
    }

    public long getCountByStatus(String status) {
        return complaintDAO.countByStatus(status);
    }

    public long getCountByPriority(String priority) {
        return complaintDAO.countByPriority(priority);
    }
}

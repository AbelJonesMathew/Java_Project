package com.campusvoice.dao;

import com.campusvoice.model.Complaint;
import java.util.List;
import java.util.Optional;

public interface ComplaintDAO {
    boolean createComplaint(Complaint complaint);
    Optional<Complaint> findById(String id);
    Optional<Complaint> findByCode(String complaintCode);
    List<Complaint> findByStudentId(String studentId);
    List<Complaint> findByDepartmentId(int departmentId);
    List<Complaint> findByAssignedTo(String userId);
    List<Complaint> findByStatus(String status);
    List<Complaint> findByPriority(String priority);
    List<Complaint> findAll();
    List<Complaint> searchComplaints(String keyword, String priority, String status, Integer categoryId);
    boolean updateStatus(String complaintId, String newStatus, String remark, String userId);
    boolean updatePriority(String complaintId, String newPriority, String reason);
    boolean assignComplaint(String complaintId, String assignedToUserId, String assignedByUserId, String remark);
    long countTotal();
    long countByStatus(String status);
    long countByPriority(String priority);
}

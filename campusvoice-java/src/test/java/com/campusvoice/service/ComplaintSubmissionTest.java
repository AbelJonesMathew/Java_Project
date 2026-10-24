package com.campusvoice.service;

import com.campusvoice.model.Complaint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComplaintSubmissionTest {
    @Test
    void submittedComplaintReachesStudentsDepartmentRegardlessOfCategory() {
        ComplaintService service = new ComplaintService();
        Complaint complaint = new Complaint();
        complaint.setStudentId("55555555-5555-5555-5555-555555555555");
        complaint.setTitle("Complaint submission regression");
        complaint.setDescription("The lab projector needs repair.");
        complaint.setCategoryId(2);
        assertTrue(service.createComplaint(complaint), "Student submission must be saved");
        assertTrue(service.getComplaintsForStudent(complaint.getStudentId()).stream()
                .anyMatch(c -> c.getId().equals(complaint.getId())));
        assertTrue(service.getComplaintsForDepartment(1).stream()
                .anyMatch(c -> c.getId().equals(complaint.getId())), "CSE HoD must receive CSE complaints");
        assertFalse(service.getComplaintsForDepartment(2).stream()
                .anyMatch(c -> c.getId().equals(complaint.getId())), "Category 2 must not route to department 2");
    }
}

package com.campusvoice;

import com.campusvoice.dao.CommentDAO;
import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.dao.UserDAO;
import com.campusvoice.dao.impl.CommentDAOImpl;
import com.campusvoice.dao.impl.ComplaintDAOImpl;
import com.campusvoice.dao.impl.UserDAOImpl;
import com.campusvoice.model.Comment;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.service.AuthenticationService;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.security.SessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardIntegrationTest {
    private static ComplaintService complaintService;
    private static ComplaintDAO complaintDAO;
    private static CommentDAO commentDAO;
    private static UserDAO userDAO;
    private static AuthenticationService authService;

    @BeforeAll
    public static void setup() {
        complaintService = new ComplaintService();
        complaintDAO = new ComplaintDAOImpl();
        commentDAO = new CommentDAOImpl();
        userDAO = new UserDAOImpl();
        authService = new AuthenticationService(userDAO);
    }

    @Test
    public void testAllFourRolesLoginSuccessfully() {
        // 1. Student Login
        boolean stuLogin = authService.login("STU202501", "Password123!");
        assertTrue(stuLogin, "Student STU202501 should login successfully");
        User stu = SessionManager.getInstance().getCurrentUser();
        assertNotNull(stu);
        assertEquals(1, stu.getRoleId());
        SessionManager.getInstance().logout();

        // 2. HOD Login
        boolean hodLogin = authService.login("HODCS01", "Password123!");
        assertTrue(hodLogin, "HOD HODCS01 should login successfully");
        User hod = SessionManager.getInstance().getCurrentUser();
        assertNotNull(hod);
        assertEquals(2, hod.getRoleId());
        SessionManager.getInstance().logout();

        // 3. Principal Login
        boolean prinLogin = authService.login("PRIN001", "Password123!");
        assertTrue(prinLogin, "Principal PRIN001 should login successfully");
        User prin = SessionManager.getInstance().getCurrentUser();
        assertNotNull(prin);
        assertEquals(3, prin.getRoleId());
        SessionManager.getInstance().logout();

        // 4. Management Login
        boolean mgmtLogin = authService.login("ADMIN001", "Password123!");
        assertTrue(mgmtLogin, "Management ADMIN001 should login successfully");
        User mgmt = SessionManager.getInstance().getCurrentUser();
        assertNotNull(mgmt);
        assertEquals(4, mgmt.getRoleId());
        SessionManager.getInstance().logout();
    }

    @Test
    public void testMergedComplaintsDatasetIntegrity() {
        List<Complaint> complaints = complaintService.getAllComplaints();
        assertNotNull(complaints);
        assertTrue(complaints.size() >= 5, "Dataset must contain all 5 merged grievances");

        // Verify CP001: Exposed Wires (Safety / High Priority)
        Optional<Complaint> cp1 = complaints.stream().filter(c -> "#CP001".equalsIgnoreCase(c.getComplaintCode())).findFirst();
        assertTrue(cp1.isPresent(), "#CP001 must exist");
        assertEquals("HIGH", cp1.get().getPriority());
        assertTrue("IN PROGRESS".equals(cp1.get().getStatus()) || "RESOLVED".equals(cp1.get().getStatus()),
                "Status should be valid active or resolved workflow state");
        assertNotNull(cp1.get().getStudentName(), "Submitter name should be joined");
        assertNotNull(cp1.get().getAssignedToName(), "Assigned officer should be joined");

        // Verify CP002: Malayalam with standardized translation
        Optional<Complaint> cp2 = complaints.stream().filter(c -> "#CP002".equalsIgnoreCase(c.getComplaintCode())).findFirst();
        assertTrue(cp2.isPresent(), "#CP002 must exist");
        assertEquals("Malayalam", cp2.get().getOriginalLanguage());
        assertNotNull(cp2.get().getTranslatedText(), "Standardized English translation must be present");
        assertTrue(cp2.get().getTranslatedText().contains("projector"), "Translation should mention projector");

        // Verify CP003: Resolved Library Chairs
        Optional<Complaint> cp3 = complaints.stream().filter(c -> "#CP003".equalsIgnoreCase(c.getComplaintCode())).findFirst();
        assertTrue(cp3.isPresent(), "#CP003 must exist");
        assertEquals("RESOLVED", cp3.get().getStatus());
        assertEquals("LOW", cp3.get().getPriority());

        // Verify CP004: Anonymous Group Petition for WiFi
        Optional<Complaint> cp4 = complaints.stream().filter(c -> "#CP004".equalsIgnoreCase(c.getComplaintCode())).findFirst();
        assertTrue(cp4.isPresent(), "#CP004 must exist");
        assertTrue(cp4.get().isAnonymous(), "#CP004 should be anonymous");
        assertTrue(cp4.get().isGroupComplaint(), "#CP004 should be marked as group complaint");

        // Verify CP005: Hindi with Urgent Escalation to Principal
        Optional<Complaint> cp5 = complaints.stream().filter(c -> "#CP005".equalsIgnoreCase(c.getComplaintCode())).findFirst();
        assertTrue(cp5.isPresent(), "#CP005 must exist");
        assertEquals("Hindi", cp5.get().getOriginalLanguage());
        assertEquals("ESCALATED", cp5.get().getStatus());
        assertEquals("HIGH", cp5.get().getPriority());
    }

    @Test
    public void testActionCommentsLoading() {
        Optional<Complaint> cp1 = complaintDAO.findByCode("#CP001");
        assertTrue(cp1.isPresent());

        List<Comment> comments = commentDAO.findByComplaintId(cp1.get().getId());
        assertNotNull(comments);
        assertFalse(comments.isEmpty(), "#CP001 must have active electrician dispatch notes");
        assertTrue(comments.get(0).getCommentText().contains("cable rewiring") || comments.get(0).getCommentText().contains("Electrician"));
    }

    @Test
    public void testDynamicCountsAndMetrics() {
        long total = complaintService.getTotalCount();
        assertTrue(total >= 5);

        long high = complaintService.getCountByPriority("HIGH");
        assertTrue(high >= 2, "At least 2 High priority safety hazards should be logged");

        long resolved = complaintService.getCountByStatus("RESOLVED");
        assertTrue(resolved >= 1, "At least 1 resolved complaint should be recorded");

        long escalated = complaintService.getCountByStatus("ESCALATED");
        assertTrue(escalated >= 1, "At least 1 escalated crisis complaint should be recorded");
    }
}

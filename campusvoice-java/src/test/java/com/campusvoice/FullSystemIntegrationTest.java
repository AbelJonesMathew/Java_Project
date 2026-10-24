package com.campusvoice;

import com.campusvoice.dao.CategoryDAO;
import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.dao.UserDAO;
import com.campusvoice.dao.impl.CategoryDAOImpl;
import com.campusvoice.dao.impl.ComplaintDAOImpl;
import com.campusvoice.dao.impl.UserDAOImpl;
import com.campusvoice.model.Category;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.AuthenticationService;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.service.ReportService;
import com.campusvoice.ui.common.AIHubPanel;
import com.campusvoice.ui.common.AIPriorityDialog;
import com.campusvoice.ui.common.ComplaintDetailDialog;
import com.campusvoice.ui.hod.AssignOfficersPanel;
import com.campusvoice.ui.hod.HODDashboardFrame;
import com.campusvoice.ui.login.LoginFrame;
import com.campusvoice.ui.login.RegisterFrame;
import com.campusvoice.ui.management.AnalyticsPanel;
import com.campusvoice.ui.management.ManagementDashboardFrame;
import com.campusvoice.ui.management.SystemSettingsPanel;
import com.campusvoice.ui.management.UserManagementPanel;
import com.campusvoice.ui.principal.EscalationsPanel;
import com.campusvoice.ui.principal.PrincipalDashboardFrame;
import com.campusvoice.ui.principal.ReportsPanel;
import com.campusvoice.ui.splash.SplashScreen;
import com.campusvoice.ui.student.GroupComplaintPanel;
import com.campusvoice.ui.student.MyComplaintsPanel;
import com.campusvoice.ui.student.StudentDashboardFrame;
import com.campusvoice.ui.student.StudentProfilePanel;
import com.campusvoice.ui.student.SubmitComplaintPanel;
import com.campusvoice.ui.theme.FlatLafTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FullSystemIntegrationTest {

    @BeforeAll
    public static void setup() {
        FlatLafTheme.setup();
    }

    @AfterEach
    public void cleanup() {
        SessionManager.getInstance().logout();
    }

    private void runOnEDT(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
        }
    }

    @Test
    public void testSplashScreenConstruction() throws Exception {
        runOnEDT(() -> {
            SplashScreen splash = new SplashScreen();
            assertNotNull(splash);
            assertEquals(680, splash.getWidth());
            assertEquals(440, splash.getHeight());
            splash.dispose();
        });
    }

    @Test
    public void testLoginFrameConstruction() throws Exception {
        runOnEDT(() -> {
            LoginFrame loginFrame = new LoginFrame();
            assertNotNull(loginFrame);
            loginFrame.dispose();
        });
    }

    @Test
    public void testRegisterFrameConstruction() throws Exception {
        runOnEDT(() -> {
            RegisterFrame regFrame = new RegisterFrame();
            assertNotNull(regFrame);
            regFrame.dispose();
        });
    }

    @Test
    public void testStudentDashboardAndAllPanels() throws Exception {
        AuthenticationService authService = new AuthenticationService();
        boolean loggedIn = authService.login("STU202501", "Password123!");
        assertTrue(loggedIn, "Student login should succeed");

        runOnEDT(() -> {
            StudentDashboardFrame frame = new StudentDashboardFrame();
            assertNotNull(frame);

            // Test navigation
            frame.navigateTo("NEW_TEXT");
            frame.navigateTo("GROUP");
            frame.navigateTo("MY_COMPLAINTS");
            frame.navigateTo("AI_HUB");
            frame.navigateTo("PROFILE");
            frame.navigateTo("HOME");

            // Test individual student panels
            SubmitComplaintPanel submitPanel = new SubmitComplaintPanel(frame);
            assertNotNull(submitPanel);

            GroupComplaintPanel groupPanel = new GroupComplaintPanel(frame);
            assertNotNull(groupPanel);

            MyComplaintsPanel complaintsPanel = new MyComplaintsPanel();
            assertNotNull(complaintsPanel);
            complaintsPanel.loadComplaints();

            StudentProfilePanel profilePanel = new StudentProfilePanel(frame);
            assertNotNull(profilePanel);
            profilePanel.refreshProfileData();

            frame.dispose();
        });
    }

    @Test
    public void testHODDashboardAndPanels() throws Exception {
        AuthenticationService authService = new AuthenticationService();
        boolean loggedIn = authService.login("HODCS01", "Password123!");
        assertTrue(loggedIn, "HOD login should succeed");

        runOnEDT(() -> {
            HODDashboardFrame frame = new HODDashboardFrame();
            assertNotNull(frame);

            AssignOfficersPanel assignPanel = new AssignOfficersPanel();
            assertNotNull(assignPanel);

            AIHubPanel aiHub = new AIHubPanel();
            assertNotNull(aiHub);

            frame.dispose();
        });
    }

    @Test
    public void testPrincipalDashboardAndPanels() throws Exception {
        AuthenticationService authService = new AuthenticationService();
        boolean loggedIn = authService.login("PRIN001", "Password123!");
        assertTrue(loggedIn, "Principal login should succeed");

        runOnEDT(() -> {
            PrincipalDashboardFrame frame = new PrincipalDashboardFrame();
            assertNotNull(frame);

            EscalationsPanel escalationsPanel = new EscalationsPanel();
            assertNotNull(escalationsPanel);

            ReportsPanel reportsPanel = new ReportsPanel();
            assertNotNull(reportsPanel);

            frame.dispose();
        });
    }

    @Test
    public void testManagementDashboardAndPanels() throws Exception {
        AuthenticationService authService = new AuthenticationService();
        boolean loggedIn = authService.login("ADMIN001", "Password123!");
        assertTrue(loggedIn, "Management login should succeed");

        runOnEDT(() -> {
            ManagementDashboardFrame frame = new ManagementDashboardFrame();
            assertNotNull(frame);

            UserManagementPanel userPanel = new UserManagementPanel();
            assertNotNull(userPanel);

            AnalyticsPanel analyticsPanel = new AnalyticsPanel();
            assertNotNull(analyticsPanel);

            SystemSettingsPanel settingsPanel = new SystemSettingsPanel();
            assertNotNull(settingsPanel);

            frame.dispose();
        });
    }

    @Test
    public void testComplaintDetailDialogWithAllComplaints() throws Exception {
        ComplaintService complaintService = new ComplaintService();
        List<Complaint> complaints = complaintService.getAllComplaints();
        assertFalse(complaints.isEmpty());

        runOnEDT(() -> {
            JFrame dummyFrame = new JFrame();
            for (Complaint c : complaints) {
                ComplaintDetailDialog dlg = new ComplaintDetailDialog(dummyFrame, c);
                assertNotNull(dlg);
                dlg.dispose();
            }
            dummyFrame.dispose();
        });
    }
}

package com.campusvoice.ui.student;

import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.ui.components.DashboardCard;
import com.campusvoice.ui.components.HeaderPanel;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.SidebarButton;
import com.campusvoice.ui.login.LoginFrame;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class StudentDashboardFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;
    private User currentUser;

    private SidebarButton btnHome;
    private SidebarButton btnNewComplaint;
    private SidebarButton btnMyComplaints;
    private SidebarButton btnGroupComplaint;
    private SidebarButton btnAIHub;
    private SidebarButton btnProfile;
    private SidebarButton btnLogout;

    private JPanel sidebarPanel;

    public StudentDashboardFrame() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        setTitle("CampusVoice - Autonomous Student Grievance Redressal Portal");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.SIDEBAR_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, true);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeColors.BACKGROUND);

        // 1. SIDEBAR (Dark Navy, Expanded to 265px for complete left alignment)
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                int w = getWidth();
                int h = getHeight();

                // Subtle dark cosmic gradient
                GradientPaint gp = new GradientPaint(0, 0, ThemeColors.SIDEBAR_BG, 0, h, new Color(7, 10, 20));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Right edge border
                g2.setColor(ThemeColors.BORDER_COLOR);
                g2.drawLine(w - 1, 0, w - 1, h);

                // Ambient glow behind brand area
                g2.setColor(new Color(99, 102, 241, 18));
                g2.fillOval(-40, -40, 180, 180);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(265, 760));
        sidebarPanel.setMinimumSize(new Dimension(265, 760));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Brand & Institutional Authority Header
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.setBorder(BorderFactory.createEmptyBorder(22, 18, 12, 18));
        brandBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                int s = 28;
                g2.setColor(ThemeColors.PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, s, s, 8, 8);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, s - 1, s - 1, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(ThemeFonts.font(Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "CV";
                int tx = (s - fm.stringWidth(icon)) / 2;
                int ty = (s - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, tx, ty);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(28, 28));
        logoIcon.setOpaque(false);

        JLabel logo = new JLabel("CampusVoice");
        logo.setFont(ThemeFonts.TITLE_MEDIUM);
        logo.setForeground(Color.WHITE);

        logoRow.add(logoIcon);
        logoRow.add(logo);

        JLabel subCollege = new JLabel("SJCET Palai • Autonomous");
        subCollege.setFont(ThemeFonts.CAPTION);
        subCollege.setForeground(ThemeColors.TEXT_MUTED);
        subCollege.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        RoundedPanel roleBadge = new RoundedPanel(10, ThemeColors.CARD_SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                com.campusvoice.ui.theme.Java2DUtils.drawGlowingDot(g2, 10, getHeight() / 2, 3, ThemeColors.CYAN_ACCENT);
                g2.dispose();
            }
        };
        roleBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 4));
        roleBadge.setBorderColor(new Color(56, 189, 248, 80));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roleLbl = new JLabel("STUDENT PORTAL");
        roleLbl.setFont(ThemeFonts.font(Font.BOLD, 10));
        roleLbl.setForeground(ThemeColors.CYAN_ACCENT);
        roleBadge.add(roleLbl);
        com.campusvoice.ui.theme.CssEngine.apply(roleBadge, "card-glass");

        brandBox.add(logoRow);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(subCollege);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(roleBadge);

        sidebarPanel.add(brandBox);

        JLabel navHeader = new JLabel("MAIN NAVIGATION");
        navHeader.setFont(ThemeFonts.font(Font.BOLD, 10));
        navHeader.setForeground(ThemeColors.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 6, 10));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(navHeader);

        btnHome = createSidebarBtn("Home Overview", "HOME");
        btnNewComplaint = createSidebarBtn("Submit Complaint", "NEW_TEXT");
        btnMyComplaints = createSidebarBtn("Track Complaints", "MY_COMPLAINTS");
        btnGroupComplaint = createSidebarBtn("Group Petition", "GROUP");
        btnAIHub = createSidebarBtn("AI Intelligence Hub", "AI_HUB");
        btnProfile = createSidebarBtn("Student Profile", "PROFILE");
        btnLogout = createSidebarBtn("Logout", "LOGOUT");

        btnHome.setActive(true);

        sidebarPanel.add(btnHome);
        sidebarPanel.add(btnNewComplaint);
        sidebarPanel.add(btnMyComplaints);
        sidebarPanel.add(btnGroupComplaint);
        sidebarPanel.add(btnAIHub);
        sidebarPanel.add(btnProfile);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createVerticalStrut(20));

        // 2. MAIN CONTAINER (Glassmorphic Aurora Backdrop)
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                com.campusvoice.ui.theme.Java2DUtils.paintAuroraBackdrop(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);

        HeaderPanel header = new HeaderPanel(currentUser, "Together for a better campus.");
        mainContainer.add(header, BorderLayout.NORTH);

        // 3. CARD CONTENT AREA
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Add Screens
        contentArea.add(buildHomeDashboard(), "HOME");
        contentArea.add(new SubmitComplaintPanel(this), "NEW_TEXT");
        contentArea.add(new GroupComplaintPanel(this), "GROUP");
        contentArea.add(new MyComplaintsPanel(), "MY_COMPLAINTS");
        contentArea.add(new com.campusvoice.ui.common.AIHubPanel(), "AI_HUB");
        contentArea.add(new StudentProfilePanel(this), "PROFILE");

        mainContainer.add(contentArea, BorderLayout.CENTER);

        rootPanel.add(sidebarPanel, BorderLayout.WEST);
        rootPanel.add(mainContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private SidebarButton createSidebarBtn(String text, String cardName) {
        SidebarButton btn = new SidebarButton(text);
        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                SessionManager.getInstance().logout();
                dispose();
                new LoginFrame().setVisible(true);
                return;
            }
            resetSidebarButtons();
            btn.setActive(true);
            cardLayout.show(contentArea, cardName);
        });
        return btn;
    }

    public void navigateTo(String cardName) {
        resetSidebarButtons();
        if ("NEW_TEXT".equals(cardName)) btnNewComplaint.setActive(true);
        else if ("MY_COMPLAINTS".equals(cardName)) btnMyComplaints.setActive(true);
        else if ("AI_HUB".equals(cardName)) btnAIHub.setActive(true);
        else if ("GROUP".equals(cardName)) btnGroupComplaint.setActive(true);
        else if ("PROFILE".equals(cardName)) btnProfile.setActive(true);
        else btnHome.setActive(true);
        cardLayout.show(contentArea, cardName);
    }

    private void resetSidebarButtons() {
        btnHome.setActive(false);
        btnNewComplaint.setActive(false);
        btnMyComplaints.setActive(false);
        btnGroupComplaint.setActive(false);
        if (btnAIHub != null) btnAIHub.setActive(false);
        btnProfile.setActive(false);
    }

    private JPanel buildHomeDashboard() {
        JPanel home = new JPanel(new BorderLayout(20, 20));
        home.setOpaque(false);
        home.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // Grid Action Cards
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setOpaque(false);

        DashboardCard cardNew = new DashboardCard("New Complaint", "+ Submit", "Submit a new text complaint", ThemeColors.PRIMARY_BLUE);
        DashboardCard cardTrack = new DashboardCard("My Complaints", "Track", "Track your active complaint status", ThemeColors.PRIORITY_LOW);
        DashboardCard cardAI = new DashboardCard("AI Intelligence Hub", "Explore", "Multilingual translation & AI priority analysis", ThemeColors.PURPLE_ACCENT);
        DashboardCard cardGroup = new DashboardCard("Group Complaint", "Raise", "Raise issues together with supporters", ThemeColors.PRIORITY_MED);

        cardNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardTrack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardAI.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardGroup.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cardNew.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { navigateTo("NEW_TEXT"); }});
        cardTrack.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { navigateTo("MY_COMPLAINTS"); }});
        cardAI.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { navigateTo("AI_HUB"); }});
        cardGroup.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { navigateTo("GROUP"); }});

        grid.add(cardNew);
        grid.add(cardTrack);
        grid.add(cardAI);
        grid.add(cardGroup);

        home.add(grid, BorderLayout.NORTH);

        // Recent Complaints panel below
        MyComplaintsPanel recent = new MyComplaintsPanel();
        home.add(recent, BorderLayout.CENTER);

        return home;
    }
}

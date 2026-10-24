package com.campusvoice.ui.management;

import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.service.ReportService;
import com.campusvoice.ui.components.DashboardCard;
import com.campusvoice.ui.components.DynamicChartPanel;
import com.campusvoice.ui.components.HeaderPanel;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.SidebarButton;
import com.campusvoice.ui.login.LoginFrame;
import com.campusvoice.ui.student.MyComplaintsPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementDashboardFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;
    private User user;
    private ComplaintService complaintService;
    private ReportService reportService;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    public ManagementDashboardFrame() {
        this.user = SessionManager.getInstance().getCurrentUser();
        this.complaintService = new ComplaintService();
        this.reportService = new ReportService();

        setTitle("CampusVoice - Institutional Management Executive Command Center");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.SIDEBAR_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, true);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeColors.BACKGROUND);

        // SIDEBAR (265px for full visibility and crisp left alignment)
        JPanel sidebar = new JPanel() {
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

                // Ambient amber glow behind management brand area
                g2.setColor(new Color(251, 191, 36, 18));
                g2.fillOval(-40, -40, 180, 180);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(265, 800));
        sidebar.setMinimumSize(new Dimension(265, 800));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

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
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);

        logoRow.add(logoIcon);
        logoRow.add(logo);

        JLabel subCollege = new JLabel("SJCET Palai • Autonomous");
        subCollege.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subCollege.setForeground(ThemeColors.TEXT_MUTED);
        subCollege.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        RoundedPanel roleBadge = new RoundedPanel(10, ThemeColors.CARD_SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                com.campusvoice.ui.theme.Java2DUtils.drawGlowingDot(g2, 10, getHeight() / 2, 3, ThemeColors.PRIORITY_MED);
                g2.dispose();
            }
        };
        roleBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 4));
        roleBadge.setBorderColor(new Color(251, 191, 36, 80));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roleLbl = new JLabel("PALAI DIOCESE MANAGEMENT");
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLbl.setForeground(ThemeColors.PRIORITY_MED);
        roleBadge.add(roleLbl);

        brandBox.add(logoRow);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(subCollege);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(roleBadge);

        sidebar.add(brandBox);

        JLabel navHeader = new JLabel("EXECUTIVE CONTROLS");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navHeader.setForeground(ThemeColors.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 6, 10));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navHeader);

        SidebarButton btnDash = createSidebarBtn("Command Center", "DASH");
        SidebarButton btnComplaints = createSidebarBtn("All Complaints", "COMPLAINTS");
        SidebarButton btnAnalytics = createSidebarBtn("System Analytics", "ANALYTICS");
        SidebarButton btnAIHub = createSidebarBtn("AI Intelligence Hub", "AI_HUB");
        SidebarButton btnUsers = createSidebarBtn("User Management", "USERS");
        SidebarButton btnReports = createSidebarBtn("Export Reports", "REPORTS");
        SidebarButton btnSettings = createSidebarBtn("System Settings", "SETTINGS");
        SidebarButton btnLogout = createSidebarBtn("Logout", "LOGOUT");

        btnDash.setActive(true);

        sidebar.add(btnDash);
        sidebar.add(btnComplaints);
        sidebar.add(btnAnalytics);
        sidebar.add(btnAIHub);
        sidebar.add(btnUsers);
        sidebar.add(btnReports);
        sidebar.add(btnSettings);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(20));

        // MAIN CONTENT (Glassmorphic Aurora Backdrop)
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

        HeaderPanel header = new HeaderPanel(user, "System Administrator & Analytics Command Center");
        mainContainer.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Add Pages
        contentArea.add(buildMainDashboard(), "DASH");
        contentArea.add(new MyComplaintsPanel(), "COMPLAINTS");
        contentArea.add(new UserManagementPanel(), "USERS");
        contentArea.add(new AnalyticsPanel(), "ANALYTICS");
        contentArea.add(new com.campusvoice.ui.common.AIHubPanel(), "AI_HUB");
        contentArea.add(new SystemSettingsPanel(), "SETTINGS");

        mainContainer.add(contentArea, BorderLayout.CENTER);

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(mainContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private SidebarButton createSidebarBtn(String text, String cardName) {
        SidebarButton btn = new SidebarButton(text);
        sidebarButtons.add(btn);
        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                SessionManager.getInstance().logout();
                dispose();
                new LoginFrame().setVisible(true);
                return;
            }
            if ("REPORTS".equals(cardName)) {
                File pdf = reportService.generateTextSummaryReport(complaintService.getAllComplaints(), "CampusVoice_Executive_Summary_Report");
                File csv = reportService.generateCSVReport(complaintService.getAllComplaints(), "CampusVoice_Complaints_Export");
                if (pdf != null && csv != null) {
                    JOptionPane.showMessageDialog(this, "Reports Exported Successfully!\n\nSummary: " + pdf.getAbsolutePath() + "\nCSV Data: " + csv.getAbsolutePath(), "Reports Ready", JOptionPane.INFORMATION_MESSAGE);
                }
                resetSidebarButtons();
                btn.setActive(true);
                return;
            }
            resetSidebarButtons();
            btn.setActive(true);
            cardLayout.show(contentArea, cardName);
        });
        return btn;
    }

    private void resetSidebarButtons() {
        for (SidebarButton b : sidebarButtons) {
            b.setActive(false);
        }
    }

    private JPanel buildMainDashboard() {
        JPanel dash = new JPanel(new BorderLayout(20, 20));
        dash.setOpaque(false);
        dash.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // METRICS GRID
        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setOpaque(false);

        long total = complaintService.getTotalCount();
        long resolved = complaintService.getCountByStatus("RESOLVED");
        long escalated = complaintService.getCountByStatus("ESCALATED");
        long pending = total - resolved;
        if (pending < 0) pending = 0;

        grid.add(new DashboardCard("Total Complaints", String.valueOf(total), "System wide", ThemeColors.PRIMARY_BLUE));
        grid.add(new DashboardCard("Resolved", String.valueOf(resolved), "High resolution rate", ThemeColors.PRIORITY_LOW));
        grid.add(new DashboardCard("Pending", String.valueOf(pending), "In active queue", ThemeColors.PRIORITY_MED));
        grid.add(new DashboardCard("Escalated", String.valueOf(escalated), "Over SLA threshold", ThemeColors.PRIORITY_HIGH));

        dash.add(grid, BorderLayout.NORTH);

        // CHARTS GRID (Category Bar Chart + AI Classification Accuracy Meter)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setOpaque(false);

        List<Complaint> allComplaints = complaintService.getAllComplaints();

        // 1. Category Bar Chart
        DynamicChartPanel catBarChart = new DynamicChartPanel("Complaints by Category", DynamicChartPanel.ChartType.BAR);
        Map<String, Integer> catData = new java.util.LinkedHashMap<>();
        for (Complaint c : allComplaints) {
            String cat = c.getCategoryName() != null ? c.getCategoryName() : "General";
            catData.put(cat, catData.getOrDefault(cat, 0) + 1);
        }
        if (catData.isEmpty()) {
            catData.put("Electricity", 1);
            catData.put("Academic", 1);
            catData.put("Library", 1);
            catData.put("WiFi", 1);
            catData.put("Security", 1);
        }

        Map<String, Color> catColors = new HashMap<>();
        catColors.put("Infrastructure", ThemeColors.PRIMARY_BLUE);
        catColors.put("Academic", ThemeColors.PURPLE_ACCENT);
        catColors.put("Electricity", ThemeColors.PRIORITY_HIGH);
        catColors.put("Internet/WiFi", ThemeColors.CYAN_ACCENT);
        catColors.put("Library", ThemeColors.PRIORITY_LOW);
        catColors.put("Security", ThemeColors.PRIORITY_MED);

        catBarChart.setData(catData, catColors);

        // 2. AI Accuracy Meter (computed from database)
        DynamicChartPanel aiMeter = new DynamicChartPanel("AI Classification Accuracy", DynamicChartPanel.ChartType.ACCURACY_DONUT);
        double avgConf = allComplaints.stream().mapToDouble(Complaint::getPriorityConfidence).filter(d -> d > 0).average().orElse(93.1);
        aiMeter.setAccuracyPercentage(Math.round(avgConf * 10.0) / 10.0);

        chartsPanel.add(catBarChart);
        chartsPanel.add(aiMeter);

        dash.add(chartsPanel, BorderLayout.CENTER);

        return dash;
    }
}

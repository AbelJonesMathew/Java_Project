package com.campusvoice.ui.principal;

import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.service.ReportService;
import com.campusvoice.ui.components.DashboardCard;
import com.campusvoice.ui.components.HeaderPanel;
import com.campusvoice.ui.components.ModernButton;
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
import java.util.List;

public class PrincipalDashboardFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;
    private User user;
    private ComplaintService complaintService;
    private ReportService reportService;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    public PrincipalDashboardFrame() {
        this.user = SessionManager.getInstance().getCurrentUser();
        this.complaintService = new ComplaintService();
        this.reportService = new ReportService();

        setTitle("CampusVoice - Principal Institutional Oversight Dashboard");
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

                // Ambient purple glow behind executive brand area
                g2.setColor(new Color(168, 85, 247, 18));
                g2.fillOval(-40, -40, 180, 180);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(265, 780));
        sidebar.setMinimumSize(new Dimension(265, 780));
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
                com.campusvoice.ui.theme.Java2DUtils.drawGlowingDot(g2, 10, getHeight() / 2, 3, ThemeColors.PURPLE_ACCENT);
                g2.dispose();
            }
        };
        roleBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 4));
        roleBadge.setBorderColor(new Color(168, 85, 247, 80));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roleLbl = new JLabel("PRINCIPAL OVERSIGHT");
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLbl.setForeground(ThemeColors.PURPLE_ACCENT);
        roleBadge.add(roleLbl);

        brandBox.add(logoRow);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(subCollege);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(roleBadge);

        sidebar.add(brandBox);

        JLabel navHeader = new JLabel("EXECUTIVE MENU");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navHeader.setForeground(ThemeColors.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 6, 10));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navHeader);

        SidebarButton btnDash = createSidebarBtn("Institutional Overview", "DASH");
        SidebarButton btnEscalations = createSidebarBtn("SLA Escalations", "ESCALATIONS");
        SidebarButton btnReports = createSidebarBtn("Executive Reports", "REPORTS");
        SidebarButton btnAIHub = createSidebarBtn("AI Intelligence Hub", "AI_HUB");
        SidebarButton btnLogout = createSidebarBtn("Logout", "LOGOUT");

        btnDash.setActive(true);

        sidebar.add(btnDash);
        sidebar.add(btnEscalations);
        sidebar.add(btnReports);
        sidebar.add(btnAIHub);
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

        HeaderPanel header = new HeaderPanel(user, "Institutional Oversight & SLA Escalation Dashboard");
        mainContainer.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Add Pages
        contentArea.add(buildMainDashboard(), "DASH");
        contentArea.add(new EscalationsPanel(), "ESCALATIONS");
        contentArea.add(new ReportsPanel(), "REPORTS");
        contentArea.add(new com.campusvoice.ui.common.AIHubPanel(), "AI_HUB");

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
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // METRIC CARDS
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsGrid.setOpaque(false);

        long total = complaintService.getTotalCount();
        long high = complaintService.getCountByPriority("HIGH");
        long escalated = complaintService.getCountByStatus("ESCALATED");
        long resolved = complaintService.getCountByStatus("RESOLVED");

        cardsGrid.add(new DashboardCard("Total Complaints", String.valueOf(total), "Campus wide", ThemeColors.PRIMARY_BLUE));
        cardsGrid.add(new DashboardCard("High Priority", String.valueOf(high), "Safety hazards", ThemeColors.PRIORITY_HIGH));
        cardsGrid.add(new DashboardCard("Pending Escalations", String.valueOf(escalated), "Exceeded SLA", ThemeColors.STATUS_ESCALATED));
        cardsGrid.add(new DashboardCard("Resolved", String.valueOf(resolved), "Closed tickets", ThemeColors.PRIORITY_LOW));

        centerPanel.add(cardsGrid, BorderLayout.NORTH);

        // MIDDLE: Pending Escalations List
        MyComplaintsPanel escalationsTable = new MyComplaintsPanel();
        centerPanel.add(escalationsTable, BorderLayout.CENTER);

        // FOOTER: Quick Action Buttons
        JPanel footerBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footerBox.setOpaque(false);

        ModernButton btnGenReport = new ModernButton("Generate Executive Report", ThemeColors.PURPLE_ACCENT);
        btnGenReport.addActionListener(e -> {
            File reportFile = reportService.generateTextSummaryReport(complaintService.getAllComplaints(), "CampusVoice_Principal_Escalation_Report");
            if (reportFile != null) {
                JOptionPane.showMessageDialog(this, "Report Generated Successfully!\nSaved to: " + reportFile.getAbsolutePath(), "Report Ready", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        footerBox.add(btnGenReport);
        centerPanel.add(footerBox, BorderLayout.SOUTH);
        return centerPanel;
    }
}

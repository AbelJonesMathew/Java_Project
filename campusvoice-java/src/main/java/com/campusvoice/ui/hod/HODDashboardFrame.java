package com.campusvoice.ui.hod;

import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.ui.components.DashboardCard;
import com.campusvoice.ui.components.DynamicChartPanel;
import com.campusvoice.ui.components.HeaderPanel;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.SidebarButton;
import com.campusvoice.ui.login.LoginFrame;
import com.campusvoice.ui.student.MyComplaintsPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HODDashboardFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;
    private User user;
    private ComplaintService complaintService;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    public HODDashboardFrame() {
        this.user = SessionManager.getInstance().getCurrentUser();
        this.complaintService = new ComplaintService();

        setTitle("CampusVoice - Department Head Grievance Management Portal");
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

                // Ambient glow behind brand area
                g2.setColor(new Color(99, 102, 241, 18));
                g2.fillOval(-40, -40, 180, 180);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(265, 760));
        sidebar.setMinimumSize(new Dimension(265, 760));
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
        JLabel roleLbl = new JLabel("HOD (CSE) PORTAL");
        roleLbl.setFont(ThemeFonts.font(Font.BOLD, 10));
        roleLbl.setForeground(ThemeColors.CYAN_ACCENT);
        roleBadge.add(roleLbl);
        com.campusvoice.ui.theme.CssEngine.apply(roleBadge, "card-glass");

        brandBox.add(logoRow);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(subCollege);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(roleBadge);

        sidebar.add(brandBox);

        JLabel navHeader = new JLabel("DEPARTMENT MENU");
        navHeader.setFont(ThemeFonts.font(Font.BOLD, 10));
        navHeader.setForeground(ThemeColors.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 6, 10));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navHeader);

        SidebarButton btnDash = createSidebarBtn("Department Analytics", "DASH");
        SidebarButton btnComplaints = createSidebarBtn("Complaints Queue", "COMPLAINTS");
        SidebarButton btnAssign = createSidebarBtn("Assign Officers", "ASSIGN");
        SidebarButton btnAIHub = createSidebarBtn("AI Intelligence Hub", "AI_HUB");
        SidebarButton btnLogout = createSidebarBtn("Logout", "LOGOUT");

        btnDash.setActive(true);

        sidebar.add(btnDash);
        sidebar.add(btnComplaints);
        sidebar.add(btnAssign);
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

        HeaderPanel header = new HeaderPanel(user, "Computer Science & Engineering Department Control Portal");
        mainContainer.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Add Pages
        contentArea.add(buildMainDashboard(), "DASH");
        contentArea.add(new MyComplaintsPanel(), "COMPLAINTS");
        contentArea.add(new AssignOfficersPanel(), "ASSIGN");
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

        // TOP METRIC CARDS
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsGrid.setOpaque(false);

        long highCount = complaintService.getCountByPriority("HIGH");
        long medCount = complaintService.getCountByPriority("MEDIUM");
        long lowCount = complaintService.getCountByPriority("LOW");

        cardsGrid.add(new DashboardCard("High Priority", String.valueOf(highCount), "Urgent action required", ThemeColors.PRIORITY_HIGH));
        cardsGrid.add(new DashboardCard("Medium Priority", String.valueOf(medCount), "Standard queue", ThemeColors.PRIORITY_MED));
        cardsGrid.add(new DashboardCard("Low Priority", String.valueOf(lowCount), "Scheduled maintenance", ThemeColors.PRIORITY_LOW));

        centerPanel.add(cardsGrid, BorderLayout.NORTH);

        // MIDDLE SPLIT (Pie Chart + Complaints Table)
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        middleSplit.setOpaque(false);

        DynamicChartPanel chartPanel = new DynamicChartPanel("Priority Distribution", DynamicChartPanel.ChartType.PIE);
        Map<String, Integer> pData = new HashMap<>();
        pData.put("High", (int) highCount);
        pData.put("Medium", (int) medCount);
        pData.put("Low", (int) lowCount);

        Map<String, Color> pColors = new HashMap<>();
        pColors.put("High", ThemeColors.PRIORITY_HIGH);
        pColors.put("Medium", ThemeColors.PRIORITY_MED);
        pColors.put("Low", ThemeColors.PRIORITY_LOW);

        chartPanel.setData(pData, pColors);

        MyComplaintsPanel recentComplaints = new MyComplaintsPanel();

        middleSplit.add(chartPanel);
        middleSplit.add(recentComplaints);

        centerPanel.add(middleSplit, BorderLayout.CENTER);
        return centerPanel;
    }
}

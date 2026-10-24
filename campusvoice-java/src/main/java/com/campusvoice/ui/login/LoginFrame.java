package com.campusvoice.ui.login;

import com.campusvoice.config.AppConfig;
import com.campusvoice.model.Role;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.AuthenticationService;
import com.campusvoice.ui.hod.HODDashboardFrame;
import com.campusvoice.ui.management.ManagementDashboardFrame;
import com.campusvoice.ui.principal.PrincipalDashboardFrame;
import com.campusvoice.ui.student.StudentDashboardFrame;
import com.campusvoice.ui.theme.ThemeColors;
import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Premium Institutional Login Interface for CampusVoice
 * St. Joseph's College of Engineering and Technology (SJCET), Palai
 * 
 * Features:
 * - Widescreen two-column layout (55% branding & highlights, 45% login card)
 * - Deep navy (#0B1426) & glassmorphism aesthetic with electric blue and violet accents
 * - Responsive layout supporting high-DPI displays
 * - Proportional college photograph and sharp institutional logo
 * - Interactive 4-card role selector synchronized with dropdown
 * - Modern inputs with illuminated focus borders and user/lock vector icons
 * - Working show/hide password toggle button
 * - Preserved authentication, database connectivity, and session management
 */
public class LoginFrame extends JFrame {
    private final AuthenticationService authService;

    // Assets
    private final BufferedImage campusImg;
    private final BufferedImage logoImg;

    // Login Form Components
    private int selectedRoleIndex = 0; // 0: Student, 1: HOD, 2: Principal, 3: Mgmt
    private RoleCard[] roleCards;
    private JComboBox<String> roleCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckBox;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginFrame() {
        authService = new AuthenticationService();

        setTitle("CampusVoice - St. Joseph's College of Engineering and Technology, Palai");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1040, 740));
        setLocationRelativeTo(null);
        setResizable(true);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.LOGIN_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, true);

        // Load Images with resource and filesystem fallbacks
        campusImg = loadAsset("/images/sjcet_campus.jpg",
                "src/main/resources/images/sjcet_campus.jpg",
                "campusvoice-java/src/main/resources/images/sjcet_campus.jpg",
                "sjcet_campus.jpg",
                "../sjcet_campus.jpg");

        logoImg = loadAsset("/images/sjcet_logo_white.png",
                "/images/sjcet_logo.png",
                "src/main/resources/images/sjcet_logo_white.png",
                "src/main/resources/images/sjcet_logo.png",
                "campusvoice-java/src/main/resources/images/sjcet_logo_white.png",
                "campusvoice-java/src/main/resources/images/sjcet_logo.png",
                "sjcet_logo_white.png",
                "sjcet_logo.png",
                "../sjcet_logo_white.png",
                "../sjcet_logo.png");

        // Set Frame Icon if logo is available
        if (logoImg != null) {
            setIconImage(logoImg);
        }

        // Two-Column Responsive Split Layout (55% Left, 45% Right)
        JPanel mainContainer = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int leftW = (int) (w * 0.53);
                int rightW = w - leftW;
                if (getComponentCount() >= 2) {
                    getComponent(0).setBounds(0, 0, leftW, h);
                    getComponent(1).setBounds(leftW, 0, rightW, h);
                }
            }
        };
        mainContainer.setLayout(null);
        mainContainer.setBackground(ThemeColors.LOGIN_BG);

        // 1. Left Panel (College Branding & Highlights)
        LeftBrandingPanel leftPanel = new LeftBrandingPanel();

        // 2. Right Panel (Login Card)
        JPanel rightPanel = createRightLoginPanel();

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);
        setContentPane(mainContainer);

        // Initialize default credentials
        syncRoleCredentials(false);

        // Bind Enter key to login
        if (loginButton != null) {
            getRootPane().setDefaultButton(loginButton);
        }
        if (usernameField != null) {
            usernameField.addActionListener(e -> handleLogin());
        }
        if (passwordField != null) {
            passwordField.addActionListener(e -> handleLogin());
        }
    }

    private static BufferedImage loadAsset(String resourcePath, String... fileFallbacks) {
        try {
            InputStream is = LoginFrame.class.getResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}

        for (String path : fileFallbacks) {
            try {
                File f = new File(path);
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return img;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // =========================================================================
    // LEFT PANEL: COLLEGE BRANDING & INSTITUTIONAL HIGHLIGHTS (55% WIDTH)
    // =========================================================================
    private class LeftBrandingPanel extends JPanel {
        public LeftBrandingPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            int padX = Math.max(28, (int) (w * 0.05));
            int contentW = w - (padX * 2);

            g2.setColor(ThemeColors.LOGIN_BG);
            g2.fillRect(0, 0, w, h);
            padX = 40;
            contentW = w - padX * 2;
            if (logoImg != null) {
                int logoH = 54;
                int logoW = (int) (logoH * (double) logoImg.getWidth() / logoImg.getHeight());
                g2.drawImage(logoImg, padX, 30, logoW, logoH, null);
            }
            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("CAMPUSVOICE  /  SJCET PALAI", padX, 126);
            g2.setColor(ThemeColors.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
            g2.drawString("A better campus", padX, 178);
            g2.drawString("starts with your voice.", padX, 220);
            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2.drawString("Report a concern. Follow its progress. Make a difference.", padX, 254);
            int photoY = 290;
            int photoH = Math.max(150, Math.min(250, h - 435));
            if (campusImg != null) {
                double scale = Math.max((double) contentW / campusImg.getWidth(), (double) photoH / campusImg.getHeight());
                int dw = (int) (campusImg.getWidth() * scale);
                int dh = (int) (campusImg.getHeight() * scale);
                Shape oldClip = g2.getClip();
                g2.clip(new RoundRectangle2D.Float(padX, photoY, contentW, photoH, 12, 12));
                g2.drawImage(campusImg, padX + (contentW - dw) / 2, photoY + (photoH - dh) / 2, dw, dh, null);
                g2.setClip(oldClip);
            }
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.drawString("St. Joseph's College of Engineering and Technology, Palai", padX, photoY + photoH + 25);
            g2.setColor(ThemeColors.LOGIN_BORDER);
            g2.drawLine(padX, h - 75, padX + contentW, h - 75);
            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.drawString("Your campus. Your community. Your voice.", padX, h - 45);

            g2.dispose();
        }

        private void drawHighlightCard(Graphics2D g2, String title, String subtitle, int iconType, int x, int y, int w, int h) {
            // Card surface
            g2.setColor(ThemeColors.LOGIN_CARD_BG);
            g2.fillRoundRect(x, y, w, h, 12, 12);
            g2.setColor(ThemeColors.LOGIN_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(x, y, w, h, 12, 12);

            // Icon Pill Box
            int pillX = x + 10;
            int pillY = y + (h - 32) / 2;
            int pillSize = 32;
            g2.setColor(new Color(22, 37, 68));
            g2.fillRoundRect(pillX, pillY, pillSize, pillSize, 8, 8);
            g2.setColor(ThemeColors.LOGIN_BORDER);
            g2.drawRoundRect(pillX, pillY, pillSize, pillSize, 8, 8);

            // Vector Icon
            int iconX = pillX + 8;
            int iconY = pillY + 8;
            int iSize = 16;
            if (iconType == 1) {
                VectorIcons.drawMedal(g2, iconX, iconY, iSize, ThemeColors.ELECTRIC_BLUE);
            } else if (iconType == 2) {
                VectorIcons.drawSparkle(g2, iconX, iconY, iSize, ThemeColors.ELECTRIC_VIOLET);
            } else if (iconType == 3) {
                VectorIcons.drawLightbulb(g2, iconX, iconY, iSize, ThemeColors.CYAN_ACCENT);
            } else {
                VectorIcons.drawLocationPin(g2, iconX, iconY, iSize, ThemeColors.ELECTRIC_BLUE);
            }

            // Text Labels
            int tX = pillX + pillSize + 10;
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(title, tX, y + 21);

            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(subtitle, tX, y + 36);
        }

        private void drawStatItem(Graphics2D g2, String val, String lbl, int x, int y, int w) {
            g2.setColor(ThemeColors.CYAN_ACCENT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString(val, x + 4, y);

            g2.setColor(ThemeColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(lbl, x + 4, y + 15);
        }
    }

    // =========================================================================
    // RIGHT PANEL: LOGIN INTERFACE CARD (45% WIDTH)
    // =========================================================================
    private JPanel createRightLoginPanel() {
        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setBackground(ThemeColors.LOGIN_BG);

        // Premium Translucent Glass Login Card
        JPanel loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Drop Shadow
                g2.setColor(new Color(4, 7, 15, 140));
                g2.fillRoundRect(4, 4, w - 8, h - 8, 20, 20);

                // Card Background
                g2.setColor(ThemeColors.LOGIN_CARD_BG);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);

                // Fine Outer Border
                g2.setColor(ThemeColors.LOGIN_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

                g2.dispose();
            }
        };
        loginCard.setLayout(new GridBagLayout());
        loginCard.setOpaque(false);
        loginCard.setPreferredSize(new Dimension(460, 620));
        loginCard.setBorder(new EmptyBorder(22, 28, 22, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int row = 0;

        // 1. Heading
        JLabel titleLabel = new JLabel("Welcome back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = row++;
        loginCard.add(titleLabel, gbc);

        // 2. Subtitle
        JLabel subLabel = new JLabel("Access your campus complaint portal");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        gbc.gridy = row++;
        loginCard.add(subLabel, gbc);

        // Spacing
        gbc.insets = new Insets(10, 0, 4, 0);

        // 3. Four Interactive Role Cards Arranged Horizontally
        JPanel rolesPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        rolesPanel.setOpaque(false);
        rolesPanel.setPreferredSize(new Dimension(400, 64));

        roleCards = new RoleCard[4];
        roleCards[0] = new RoleCard("Student", 0, 0);
        roleCards[1] = new RoleCard("HOD", 1, 1);
        roleCards[2] = new RoleCard("Principal", 2, 2);
        roleCards[3] = new RoleCard("Admin", 3, 3);

        for (RoleCard rc : roleCards) {
            rolesPanel.add(rc);
        }

        gbc.gridy = row++;
        loginCard.add(rolesPanel, gbc);

        // Reset insets for form rows
        gbc.insets = new Insets(8, 0, 2, 0);

        // 4. Role Dropdown (Synchronized with role cards)
        JLabel roleLabel = new JLabel("Portal");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleLabel.setForeground(Color.WHITE);
        gbc.gridy = row++;
        loginCard.add(roleLabel, gbc);

        roleCombo = new JComboBox<>(new String[]{
                "Student Portal (STU202501)",
                "HOD / Dept Portal (HODCS01)",
                "Principal Portal (PRIN001)",
                "Management Portal (ADMIN001)"
        });
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleCombo.setPreferredSize(new Dimension(380, 38));
        roleCombo.setBackground(ThemeColors.INPUT_DARK_BG);
        roleCombo.setForeground(Color.WHITE);
        roleCombo.setFocusable(true);
        roleCombo.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        roleCombo.addActionListener(e -> {
            int sel = roleCombo.getSelectedIndex();
            if (sel >= 0 && sel != selectedRoleIndex) {
                selectRole(sel, true);
            }
        });
        gbc.gridy = row++;
        loginCard.add(roleCombo, gbc);

        // 5. Username / Registration Number Field with User Icon
        JLabel userLabel = new JLabel("Username or registration number");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(Color.WHITE);
        gbc.gridy = row++;
        loginCard.add(userLabel, gbc);

        usernameField = new JTextField("STU202501");
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your Reg No or Username");
        usernameField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        ModernInputBox userBox = new ModernInputBox(usernameField, 1, null);
        gbc.gridy = row++;
        loginCard.add(userBox, gbc);

        // 6. Password Field with Lock Icon and Working Show/Hide Button
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(Color.WHITE);
        gbc.gridy = row++;
        loginCard.add(passLabel, gbc);

        passwordField = new JPasswordField("Password123!");
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        ModernInputBox passBox = new ModernInputBox(passwordField, 2, passwordField);
        gbc.gridy = row++;
        loginCard.add(passBox, gbc);

        // 7. Remember Me & Forgot Password Row
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);

        rememberCheckBox = new JCheckBox("Remember me");
        rememberCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberCheckBox.setForeground(ThemeColors.TEXT_SECONDARY);
        rememberCheckBox.setSelected(true);
        rememberCheckBox.setOpaque(false);
        rememberCheckBox.setFocusPainted(false);

        JLabel forgotLabel = new JLabel("Forgot Password?");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(ThemeColors.ELECTRIC_BLUE);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPasswordRecoveryDialog();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLabel.setText("<html><u>Forgot Password?</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotLabel.setText("Forgot Password?");
            }
        });

        optionsRow.add(rememberCheckBox, BorderLayout.WEST);
        JButton databaseButton = new JButton("Database connection");
        databaseButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        databaseButton.addActionListener(e -> SharedDatabaseSetupDialog.show(this,
                () -> errorLabel.setText("Shared database connected. Sign in to continue.")));
        optionsRow.add(databaseButton, BorderLayout.CENTER);
        optionsRow.add(forgotLabel, BorderLayout.EAST);
        gbc.insets = new Insets(4, 0, 2, 0);
        gbc.gridy = row++;
        loginCard.add(optionsRow, gbc);

        // 8. Error / Validation Message Label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLabel.setForeground(ThemeColors.PRIORITY_HIGH);
        gbc.insets = new Insets(0, 0, 2, 0);
        gbc.gridy = row++;
        loginCard.add(errorLabel, gbc);

        // 9. Full-width Blue-to-Violet Gradient "Sign In to Portal" Button
        loginButton = new com.campusvoice.ui.components.ModernButton("Sign in to CampusVoice");
        loginButton.setPreferredSize(new Dimension(380, 44));
        loginButton.addActionListener(e -> handleLogin());
        gbc.insets = new Insets(4, 0, 8, 0);
        gbc.gridy = row++;
        loginCard.add(loginButton, gbc);

        // 10. Register Link Navigation
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        regRow.setOpaque(false);

        JLabel noAccLabel = new JLabel("Don't have an account?");
        noAccLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noAccLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel regLink = new JLabel("Register here");
        regLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regLink.setForeground(ThemeColors.CYAN_ACCENT);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.setBorder(new EmptyBorder(0, 2, 0, 8));
        regLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame().setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                regLink.setText("<html><u>Register here</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                regLink.setText("Register here");
            }
        });

        regRow.add(noAccLabel);
        regRow.add(regLink);
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.gridy = row++;
        loginCard.add(regRow, gbc);

        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.gridx = 0;
        gbcCard.gridy = 0;
        gbcCard.weightx = 1.0;
        gbcCard.weighty = 1.0;
        gbcCard.anchor = GridBagConstraints.CENTER;
        rightWrapper.add(loginCard, gbcCard);
        return rightWrapper;
    }

    // Role selection handler
    private void selectRole(int index, boolean updateCombo) {
        this.selectedRoleIndex = index;
        for (int i = 0; i < roleCards.length; i++) {
            roleCards[i].setSelected(i == index);
        }
        if (updateCombo && roleCombo.getSelectedIndex() != index) {
            roleCombo.setSelectedIndex(index);
        }
        syncRoleCredentials(true);
    }

    private void syncRoleCredentials(boolean fromUserClick) {
        if (selectedRoleIndex == 0) {
            usernameField.setText("STU202501");
        } else if (selectedRoleIndex == 1) {
            usernameField.setText("HODCS01");
        } else if (selectedRoleIndex == 2) {
            usernameField.setText("PRIN001");
        } else if (selectedRoleIndex == 3) {
            usernameField.setText("ADMIN001");
        }
        passwordField.setText("Password123!");
        errorLabel.setText(" ");
    }

    private void showPasswordRecoveryDialog() {
        JOptionPane.showMessageDialog(this,
                "<html><div style='width:340px; font-family: Segoe UI;'>"
                        + "<b style='color:#368BFF; font-size:14px;'>CampusVoice Credential Recovery</b><br><br>"
                        + "For password resets or login assistance, please contact the "
                        + "<b>SJCET Automation & IT Cell</b>:<br><br>"
                        + "• <b>Email:</b> itcell@sjcetpalai.ac.in<br>"
                        + "• <b>Office:</b> Admin Block, Room A-104 (Choondacherry)<br>"
                        + "• <b>Phone:</b> Ext. 210 / +91 4822 239700<br><br>"
                        + "<span style='color:#8FA0C0; font-size:11px;'>Please present your institutional ID card or faculty code for verification.</span>"
                        + "</div></html>",
                "Password Recovery - SJCET Palai",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        errorLabel.setText(" ");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        User loggedUser = SessionManager.getInstance().getCurrentUser();
                        dispose();

                        if (loggedUser.getRoleId() == Role.ROLE_STUDENT) {
                            new StudentDashboardFrame().setVisible(true);
                        } else if (loggedUser.getRoleId() == Role.ROLE_HOD) {
                            new HODDashboardFrame().setVisible(true);
                        } else if (loggedUser.getRoleId() == Role.ROLE_PRINCIPAL) {
                            new PrincipalDashboardFrame().setVisible(true);
                        } else if (loggedUser.getRoleId() == Role.ROLE_MANAGEMENT) {
                            new ManagementDashboardFrame().setVisible(true);
                        }
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In to Portal");
                        errorLabel.setText("Invalid credentials or account inactive.");
                    }
                } catch (Exception ex) {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In to Portal");
                    errorLabel.setText("Login failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // INTERACTIVE ROLE SELECTION CARD COMPONENT
    // =========================================================================
    private class RoleCard extends JPanel {
        private final String label;
        private final int roleIndex;
        private final int iconType;
        private boolean isSelected;
        private boolean isHovered;

        public RoleCard(String label, int roleIndex, int iconType) {
            this.label = label;
            this.roleIndex = roleIndex;
            this.iconType = iconType;
            this.isSelected = (roleIndex == 0);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    selectRole(roleIndex, true);
                }
            });
        }

        public void setSelected(boolean sel) {
            this.isSelected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isSelected) {
                // Bright Blue-to-Violet Gradient Background
                GradientPaint gp = new GradientPaint(0, 0, ThemeColors.ELECTRIC_BLUE, w, h, ThemeColors.ELECTRIC_VIOLET);
                g2.setColor(ThemeColors.SIDEBAR_ACTIVE_BG);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

                // Highlighted glowing border
                g2.setColor(ThemeColors.CYAN_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            } else {
                // Dark Background with subtle hover brighten
                if (isHovered) {
                    g2.setColor(new Color(24, 38, 70));
                } else {
                    g2.setColor(ThemeColors.INPUT_DARK_BG);
                }
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

                // Thin border
                g2.setColor(isHovered ? ThemeColors.ELECTRIC_BLUE : ThemeColors.LOGIN_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }

            // Draw Vector Icon Above Label
            Color iconColor = isSelected ? Color.WHITE : (isHovered ? Color.WHITE : ThemeColors.TEXT_SECONDARY);
            int iconSize = 20;
            int iconX = (w - iconSize) / 2;
            int iconY = 10;

            if (iconType == 0) {
                VectorIcons.drawGraduationCap(g2, iconX, iconY, iconSize, iconSize, iconColor);
            } else if (iconType == 1) {
                VectorIcons.drawProfessionalUser(g2, iconX, iconY, iconSize, iconSize, iconColor);
            } else if (iconType == 2) {
                VectorIcons.drawBuilding(g2, iconX, iconY, iconSize, iconSize, iconColor);
            } else if (iconType == 3) {
                VectorIcons.drawGroup(g2, iconX, iconY, iconSize, iconSize, iconColor);
            }

            // Draw Label Below Icon
            g2.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 11));
            g2.setColor(isSelected ? Color.WHITE : ThemeColors.TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(label)) / 2;
            g2.drawString(label, tx, h - 11);

            g2.dispose();
        }
    }

    // =========================================================================
    // MODERN INPUT BOX CONTAINER (Illuminated Focus & Vector Icons)
    // =========================================================================
    private static class ModernInputBox extends JPanel {
        private final JTextField inputField;
        private final int iconType; // 1: User, 2: Lock
        private final JPasswordField pwdRef;
        private boolean isFocused = false;
        private boolean isPwdVisible = false;

        public ModernInputBox(JTextField field, int iconType, JPasswordField pwdRef) {
            this.inputField = field;
            this.iconType = iconType;
            this.pwdRef = pwdRef;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(380, 42));
            setOpaque(false);

            // Style inner text component
            field.setOpaque(false);
            field.setBorder(new EmptyBorder(8, 4, 8, 8));
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });

            add(field, BorderLayout.CENTER);

            // If password field, add working eye toggle button
            if (pwdRef != null) {
                JButton eyeBtn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color c = isPwdVisible ? ThemeColors.CYAN_ACCENT : ThemeColors.TEXT_SECONDARY;
                        VectorIcons.drawEye(g2, 8, 12, 18, 14, c, !isPwdVisible);
                        g2.dispose();
                    }
                };
                eyeBtn.setPreferredSize(new Dimension(36, 38));
                eyeBtn.setOpaque(false);
                eyeBtn.setContentAreaFilled(false);
                eyeBtn.setBorderPainted(false);
                eyeBtn.setFocusPainted(false);
                eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                eyeBtn.setToolTipText("Show/Hide Password");

                eyeBtn.addActionListener(e -> {
                    isPwdVisible = !isPwdVisible;
                    if (isPwdVisible) {
                        pwdRef.setEchoChar((char) 0);
                    } else {
                        pwdRef.setEchoChar('\u2022');
                    }
                    eyeBtn.repaint();
                });

                add(eyeBtn, BorderLayout.EAST);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background surface
            g2.setColor(ThemeColors.INPUT_DARK_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Border (Illuminated Electric Blue on Focus)
            if (isFocused) {
                g2.setColor(ThemeColors.ELECTRIC_BLUE);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            } else {
                g2.setColor(ThemeColors.LOGIN_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
            }

            // Left Vector Icon
            Color iconColor = isFocused ? ThemeColors.ELECTRIC_BLUE : ThemeColors.TEXT_SECONDARY;
            int iconX = 12;
            int iconY = 12;
            if (iconType == 1) {
                VectorIcons.drawUserSilhouette(g2, iconX, iconY, 18, iconColor);
            } else if (iconType == 2) {
                VectorIcons.drawLock(g2, iconX, iconY, 18, iconColor);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            // Reserve space on the left for the vector icon
            return new Insets(0, 36, 0, 0);
        }
    }

    // =========================================================================
    // GRADIENT BUTTON (Blue-to-Violet with Right-Arrow Icon)
    // =========================================================================
    private static class GradientButton extends JButton {
        private boolean isHovered = false;
        private boolean isPressed = false;

        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color c1 = isPressed ? new Color(42, 118, 230) : (isHovered ? new Color(72, 151, 255) : ThemeColors.ELECTRIC_BLUE);
            Color c2 = isPressed ? new Color(84, 68, 216) : (isHovered ? new Color(118, 101, 250) : ThemeColors.ELECTRIC_VIOLET);

            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Text and Right-Arrow
            FontMetrics fm = g2.getFontMetrics();
            String txt = getText();
            int textW = fm.stringWidth(txt);
            int totalW = textW + 20; // text + arrow gap
            int startX = (w - totalW) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(Color.WHITE);
            g2.drawString(txt, startX, textY);

            // Right-Arrow Vector
            VectorIcons.drawRightArrow(g2, startX + textW + 8, h / 2 - 5, 12, Color.WHITE);

            g2.dispose();
        }
    }

    // =========================================================================
    // HIGH-QUALITY CRISP VECTOR ICONS (Zero Missing-Glyph Dependencies)
    // =========================================================================
    public static class VectorIcons {

        // Graduation Cap Icon (Student)
        public static void drawGraduationCap(Graphics2D g2, int x, int y, int w, int h, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int midX = x + w / 2;
            int topY = y + 2;
            int midY = y + h / 2 - 2;

            // Diamond top
            Path2D diamond = new Path2D.Double();
            diamond.moveTo(midX, topY);
            diamond.lineTo(x + w - 1, midY);
            diamond.lineTo(midX, y + h - 8);
            diamond.lineTo(x + 1, midY);
            diamond.closePath();
            g2.fill(diamond);

            // Skull underneath
            Path2D skull = new Path2D.Double();
            skull.moveTo(x + 4, midY + 2);
            skull.curveTo(x + 5, y + h - 4, x + w - 5, y + h - 4, x + w - 4, midY + 2);
            g2.draw(skull);

            // Tassel
            g2.drawLine(x + w - 3, midY, x + w - 3, y + h - 2);
            g2.fillOval(x + w - 5, y + h - 3, 4, 4);
        }

        // Professional User Icon (HOD)
        public static void drawProfessionalUser(Graphics2D g2, int x, int y, int w, int h, Color color) {
            g2.setColor(color);
            int headSize = w * 2 / 5;
            int headX = x + (w - headSize) / 2;
            int headY = y + 1;
            g2.fillOval(headX, headY, headSize, headSize);

            int bodyY = headY + headSize + 2;
            Path2D body = new Path2D.Double();
            body.moveTo(x + 2, y + h);
            body.curveTo(x + 3, bodyY, x + w - 3, bodyY, x + w - 2, y + h);
            g2.fill(body);

            // Tie accent
            g2.setColor(ThemeColors.LOGIN_BG);
            Path2D tie = new Path2D.Double();
            tie.moveTo(x + w / 2, bodyY + 1);
            tie.lineTo(x + w / 2 - 2, bodyY + 5);
            tie.lineTo(x + w / 2, bodyY + 8);
            tie.lineTo(x + w / 2 + 2, bodyY + 5);
            tie.closePath();
            g2.fill(tie);
        }

        // Institutional Building Icon (Principal)
        public static void drawBuilding(Graphics2D g2, int x, int y, int w, int h, Color color) {
            g2.setColor(color);

            // Triangular Pediment
            Path2D roof = new Path2D.Double();
            roof.moveTo(x + w / 2, y + 1);
            roof.lineTo(x + w - 1, y + 6);
            roof.lineTo(x + 1, y + 6);
            roof.closePath();
            g2.fill(roof);

            // Roof Beam
            g2.fillRect(x + 1, y + 6, w - 2, 2);

            // 4 Pillars
            int pY = y + 8;
            int pH = h - 13;
            int colW = 2;
            int gap = (w - 4 - (4 * colW)) / 3;
            for (int i = 0; i < 4; i++) {
                int cx = x + 2 + i * (colW + gap);
                g2.fillRect(cx, pY, colW, pH);
            }

            // Steps
            g2.fillRect(x + 1, y + h - 4, w - 2, 2);
            g2.fillRect(x, y + h - 2, w, 2);
        }

        // Group Icon (Management)
        public static void drawGroup(Graphics2D g2, int x, int y, int w, int h, Color color) {
            // Left person
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
            g2.fillOval(x + 1, y + 4, w / 3, w / 3);
            Path2D leftBody = new Path2D.Double();
            leftBody.moveTo(x, y + h);
            leftBody.curveTo(x + 1, y + 11, x + w / 2 - 1, y + 11, x + w / 2 - 1, y + h);
            g2.fill(leftBody);

            // Right person
            g2.fillOval(x + w - 1 - w / 3, y + 4, w / 3, w / 3);
            Path2D rightBody = new Path2D.Double();
            rightBody.moveTo(x + w / 2 + 1, y + h);
            rightBody.curveTo(x + w / 2 + 1, y + 11, x + w - 1, y + 11, x + w, y + h);
            g2.fill(rightBody);

            // Center leader
            g2.setColor(color);
            int headSize = w * 2 / 5;
            g2.fillOval(x + (w - headSize) / 2, y + 1, headSize, headSize);
            Path2D centerBody = new Path2D.Double();
            centerBody.moveTo(x + 3, y + h);
            centerBody.curveTo(x + 4, y + 8 + headSize / 2, x + w - 4, y + 8 + headSize / 2, x + w - 3, y + h);
            g2.fill(centerBody);
        }

        // User Silhouette Icon (Input field)
        public static void drawUserSilhouette(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            int headSize = size * 2 / 5;
            int headX = x + (size - headSize) / 2;
            int headY = y + 1;
            g2.fillOval(headX, headY, headSize, headSize);

            int bodyY = headY + headSize + 2;
            Path2D body = new Path2D.Double();
            body.moveTo(x + 1, y + size - 1);
            body.curveTo(x + 2, bodyY, x + size - 2, bodyY, x + size - 1, y + size - 1);
            g2.fill(body);
        }

        // Lock Icon (Password field)
        public static void drawLock(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            int w = size - 4;
            int h = size - 2;

            // Shackle
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawArc(x + w / 4 + 1, y + 1, w / 2, h / 2, 0, 180);

            // Body
            int bodyY = y + h / 2;
            int bodyH = h / 2;
            g2.fillRoundRect(x + 2, bodyY, w, bodyH, 4, 4);

            // Keyhole
            g2.setColor(ThemeColors.INPUT_DARK_BG);
            g2.fillOval(x + w / 2, bodyY + 3, 4, 4);
            g2.drawLine(x + w / 2 + 2, bodyY + 5, x + w / 2 + 2, bodyY + 7);
        }

        // Eye Icon (Show/Hide Password)
        public static void drawEye(Graphics2D g2, int x, int y, int w, int h, Color color, boolean slashed) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Eye outline
            Path2D eye = new Path2D.Double();
            eye.moveTo(x, y + h / 2);
            eye.curveTo(x + w / 4, y, x + 3 * w / 4, y, x + w, y + h / 2);
            eye.curveTo(x + 3 * w / 4, y + h, x + w / 4, y + h, x, y + h / 2);
            g2.draw(eye);

            // Pupil
            g2.fillOval(x + w / 2 - 3, y + h / 2 - 3, 6, 6);

            // Slash line if hidden
            if (slashed) {
                g2.drawLine(x + 2, y + 1, x + w - 2, y + h - 1);
            }
        }

        // Right Arrow
        public static void drawRightArrow(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int midY = y + size / 2;
            g2.drawLine(x, midY, x + size, midY);
            g2.drawLine(x + size - 4, midY - 4, x + size, midY);
            g2.drawLine(x + size - 4, midY + 4, x + size, midY);
        }

        // Medal / Star
        public static void drawMedal(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            int r = size / 2 - 2;
            int cx = x + size / 2;
            int cy = y + r + 1;
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.fillOval(cx - 2, cy - 2, 4, 4);

            // Ribbons below
            g2.drawLine(cx - 3, cy + r, cx - 5, y + size);
            g2.drawLine(cx + 3, cy + r, cx + 5, y + size);
        }

        // Sparkle
        public static void drawSparkle(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + size / 2;
            int cy = y + size / 2;
            g2.drawLine(cx, y + 1, cx, y + size - 1);
            g2.drawLine(x + 1, cy, x + size - 1, cy);
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        }

        // Lightbulb
        public static void drawLightbulb(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int bulbSize = size * 3 / 4;
            int bx = x + (size - bulbSize) / 2;
            g2.drawOval(bx, y + 1, bulbSize, bulbSize);
            g2.fillRect(x + size / 2 - 2, y + bulbSize + 1, 4, 3);
        }

        // Location Pin
        public static void drawLocationPin(Graphics2D g2, int x, int y, int size, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            int head = size * 3 / 5;
            int cx = x + size / 2;
            int cy = y + head / 2 + 1;
            g2.fillOval(cx - head / 2, cy - head / 2, head, head);
            g2.setColor(ThemeColors.LOGIN_BG);
            g2.fillOval(cx - 2, cy - 2, 4, 4);

            g2.setColor(color);
            Path2D pin = new Path2D.Double();
            pin.moveTo(cx - head / 2 + 1, cy + 2);
            pin.lineTo(cx, y + size);
            pin.lineTo(cx + head / 2 - 1, cy + 2);
            g2.fill(pin);
        }
    }
}

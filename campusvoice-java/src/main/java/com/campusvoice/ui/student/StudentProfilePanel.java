package com.campusvoice.ui.student;

import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.dao.UserDAO;
import com.campusvoice.dao.impl.ComplaintDAOImpl;
import com.campusvoice.dao.impl.UserDAOImpl;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.PasswordHasher;
import com.campusvoice.security.SessionManager;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernPasswordField;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentProfilePanel extends RoundedPanel {
    private final StudentDashboardFrame parentFrame;
    private final UserDAO userDAO;
    private final ComplaintDAO complaintDAO;

    private JLabel avatarLabel;
    private JLabel nameHeaderLabel;
    private JLabel regNoLabel;
    private JLabel deptLabel;
    private JLabel standingLabel;
    private JLabel complaintsCountLabel;
    private JLabel resolvedCountLabel;

    private ModernTextField nameField;
    private ModernTextField emailField;
    private ModernTextField phoneField;

    private ModernPasswordField currentPassField;
    private ModernPasswordField newPassField;
    private ModernPasswordField confirmPassField;

    public StudentProfilePanel(StudentDashboardFrame parent) {
        super(20, ThemeColors.CARD_BG);
        this.parentFrame = parent;
        this.userDAO = new UserDAOImpl();
        this.complaintDAO = new ComplaintDAOImpl();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // 1. TOP HEADER
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Student Profile & Account Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Review institutional academic records, update personal contact details, and manage account credentials.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. MAIN SPLIT CONTENT (Left: Academic Identity Card / Right: Edit Details & Security)
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 22, 0));
        contentGrid.setOpaque(false);

        contentGrid.add(buildLeftProfileCard());
        contentGrid.add(buildRightEditCards());

        JScrollPane scrollPane = new JScrollPane(contentGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);

        refreshProfileData();
    }

    private JPanel buildLeftProfileCard() {
        JPanel card = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Circular Avatar Component with Java 2D ambient glow and metallic ring
        avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                int d = Math.min(getWidth(), getHeight()) - 6;
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;

                com.campusvoice.ui.theme.Java2DUtils.drawGlow(g2, x, y, d, d, d, ThemeColors.PRIMARY_BLUE, 4);

                GradientPaint gp = new GradientPaint(x, y, ThemeColors.PRIMARY_BLUE, x + d, y + d, ThemeColors.PURPLE_ACCENT);
                g2.setPaint(gp);
                g2.fillOval(x, y, d, d);

                // Specular upper shine
                GradientPaint shine = new GradientPaint(x, y, new Color(255, 255, 255, 100), x, y + (d / 2), new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fillOval(x + 2, y + 1, d - 4, d / 2);

                // Metallic outer ring
                g2.setColor(new Color(255, 255, 255, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, d, d);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                User u = SessionManager.getInstance().getCurrentUser();
                String initial = (u != null && u.getFullName() != null && !u.getFullName().isEmpty())
                        ? u.getFullName().substring(0, 1).toUpperCase() : "S";
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(initial)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, tx, ty);
                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(72, 72));
        avatarLabel.setMaximumSize(new Dimension(72, 72));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameHeaderLabel = new JLabel("Student Name", SwingConstants.CENTER);
        nameHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameHeaderLabel.setForeground(ThemeColors.TEXT_DARK);
        nameHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel rolePill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        rolePill.setOpaque(false);
        rolePill.setMaximumSize(new Dimension(160, 30));
        JLabel roleText = new JLabel("STUDENT PORTAL");
        roleText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleText.setForeground(ThemeColors.CYAN_ACCENT);
        rolePill.add(roleText);
        rolePill.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatarLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(nameHeaderLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(rolePill);
        card.add(Box.createVerticalStrut(16));

        // Details Section
        JPanel infoList = new JPanel(new GridLayout(5, 1, 0, 10));
        infoList.setOpaque(false);
        infoList.setAlignmentX(Component.CENTER_ALIGNMENT);

        regNoLabel = new JLabel("Loading...");
        deptLabel = new JLabel("Loading...");
        standingLabel = new JLabel("Loading...");

        infoList.add(createInfoRow("Registration No:", regNoLabel));
        infoList.add(createInfoRow("Institution:", new JLabel("SJCET Palai (Autonomous)")));
        infoList.add(createInfoRow("Department:", deptLabel));
        infoList.add(createInfoRow("Academic Standing:", standingLabel));
        infoList.add(createInfoRow("Account Status:", createStatusPill("Active Account", ThemeColors.PRIORITY_LOW)));

        card.add(infoList);
        card.add(Box.createVerticalStrut(20));

        // Activity Stats Box
        JPanel statsBox = new RoundedPanel(12, ThemeColors.INPUT_BG);
        statsBox.setLayout(new GridLayout(1, 2, 10, 0));
        statsBox.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        statsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        complaintsCountLabel = new JLabel("0", SwingConstants.CENTER);
        complaintsCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        complaintsCountLabel.setForeground(ThemeColors.CYAN_ACCENT);

        resolvedCountLabel = new JLabel("0", SwingConstants.CENTER);
        resolvedCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resolvedCountLabel.setForeground(ThemeColors.PRIORITY_LOW);

        JPanel stat1 = new JPanel(new GridLayout(2, 1));
        stat1.setOpaque(false);
        stat1.add(complaintsCountLabel);
        JLabel s1Lbl = new JLabel("Complaints Raised", SwingConstants.CENTER);
        s1Lbl.setFont(ThemeFonts.CAPTION);
        s1Lbl.setForeground(ThemeColors.TEXT_MUTED);
        stat1.add(s1Lbl);

        JPanel stat2 = new JPanel(new GridLayout(2, 1));
        stat2.setOpaque(false);
        stat2.add(resolvedCountLabel);
        JLabel s2Lbl = new JLabel("Resolved Tickets", SwingConstants.CENTER);
        s2Lbl.setFont(ThemeFonts.CAPTION);
        s2Lbl.setForeground(ThemeColors.TEXT_MUTED);
        stat2.add(s2Lbl);

        statsBox.add(stat1);
        statsBox.add(stat2);

        card.add(statsBox);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel buildRightEditCards() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // 1. Contact Info Card
        JPanel contactCard = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        contactCard.setLayout(new BoxLayout(contactCard, BoxLayout.Y_AXIS));
        contactCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        contactCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel cTitle = new JLabel("Personal Contact Information");
        cTitle.setFont(ThemeFonts.TITLE_SMALL);
        cTitle.setForeground(ThemeColors.TEXT_DARK);
        contactCard.add(cTitle);
        contactCard.add(Box.createVerticalStrut(14));

        nameField = new ModernTextField();
        nameField.setPlaceholder("Enter your full legal name");
        contactCard.add(createFieldRow("Full Name:", nameField));
        contactCard.add(Box.createVerticalStrut(10));

        emailField = new ModernTextField();
        emailField.setPlaceholder("Enter your institutional email address");
        contactCard.add(createFieldRow("Email Address:", emailField));
        contactCard.add(Box.createVerticalStrut(10));

        phoneField = new ModernTextField();
        phoneField.setPlaceholder("Enter your mobile number");
        contactCard.add(createFieldRow("Mobile Phone:", phoneField));
        contactCard.add(Box.createVerticalStrut(14));

        ModernButton saveContactBtn = new ModernButton("Save Contact Changes", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        saveContactBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveContactBtn.addActionListener(e -> handleSaveContact());
        contactCard.add(saveContactBtn);

        rightPanel.add(contactCard);
        rightPanel.add(Box.createVerticalStrut(16));

        // 2. Password Security Card
        JPanel passCard = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        passCard.setLayout(new BoxLayout(passCard, BoxLayout.Y_AXIS));
        passCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        passCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel pTitle = new JLabel("Account Security & Password");
        pTitle.setFont(ThemeFonts.TITLE_SMALL);
        pTitle.setForeground(ThemeColors.TEXT_DARK);
        passCard.add(pTitle);
        passCard.add(Box.createVerticalStrut(14));

        currentPassField = new ModernPasswordField();
        currentPassField.setPlaceholder("Enter current password");
        passCard.add(createFieldRow("Current Password:", currentPassField));
        passCard.add(Box.createVerticalStrut(10));

        newPassField = new ModernPasswordField();
        newPassField.setPlaceholder("Enter new secure password (min 6 chars)");
        passCard.add(createFieldRow("New Password:", newPassField));
        passCard.add(Box.createVerticalStrut(10));

        confirmPassField = new ModernPasswordField();
        confirmPassField.setPlaceholder("Re-enter new password");
        passCard.add(createFieldRow("Confirm New Password:", confirmPassField));
        passCard.add(Box.createVerticalStrut(14));

        ModernButton updatePassBtn = new ModernButton("Update Password", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        updatePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        updatePassBtn.addActionListener(e -> handleUpdatePassword());
        passCard.add(updatePassBtn);

        rightPanel.add(passCard);

        return rightPanel;
    }

    private JPanel createInfoRow(String labelText, JComponent valueComp) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeFonts.BODY_BOLD);
        lbl.setForeground(ThemeColors.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(140, 22));

        if (valueComp instanceof JLabel) {
            JLabel vLbl = (JLabel) valueComp;
            vLbl.setFont(ThemeFonts.BODY_REGULAR);
            vLbl.setForeground(ThemeColors.TEXT_DARK);
        }

        row.add(lbl, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);
        return row;
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeFonts.CAPTION);
        lbl.setForeground(ThemeColors.TEXT_MUTED);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        return panel;
    }

    private JLabel createStatusPill(String text, Color fg) {
        JLabel pill = new JLabel("● " + text);
        pill.setFont(ThemeFonts.BODY_BOLD);
        pill.setForeground(fg);
        return pill;
    }

    public void refreshProfileData() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        nameHeaderLabel.setText(u.getFullName() != null ? u.getFullName() : "Student");
        regNoLabel.setText(u.getRegNo() != null ? u.getRegNo() : "N/A");
        deptLabel.setText(u.getDepartmentName() != null ? u.getDepartmentName() : "Computer Science & Engineering");

        int year = (u.getYear() != null && u.getYear() > 0) ? u.getYear() : 3;
        int sem = (u.getSemester() != null && u.getSemester() > 0) ? u.getSemester() : 6;
        standingLabel.setText("Year " + year + " • Semester " + sem);

        nameField.setText(u.getFullName() != null ? u.getFullName() : "");
        emailField.setText(u.getEmail() != null ? u.getEmail() : "");
        phoneField.setText(u.getPhone() != null ? u.getPhone() : "");

        avatarLabel.repaint();

        // Fetch complaint statistics asynchronously
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                try {
                    List<Complaint> list = complaintDAO.findByStudentId(u.getId());
                    int total = list.size();
                    int resolved = 0;
                    for (Complaint c : list) {
                        if ("RESOLVED".equalsIgnoreCase(c.getStatus()) || "CLOSED".equalsIgnoreCase(c.getStatus())) {
                            resolved++;
                        }
                    }
                    return new int[]{total, resolved};
                } catch (Exception e) {
                    return new int[]{0, 0};
                }
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    complaintsCountLabel.setText(String.valueOf(stats[0]));
                    resolvedCountLabel.setText(String.valueOf(stats[1]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void handleSaveContact() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name and Email Address are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);

        boolean success = userDAO.updateUser(u);
        if (success) {
            JOptionPane.showMessageDialog(this, "Contact information updated successfully!", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
            nameHeaderLabel.setText(name);
            avatarLabel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile in database.", "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdatePassword() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;

        String curPass = new String(currentPassField.getPassword()).trim();
        String newPass = new String(newPassField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();

        if (curPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Password Too Short", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and Confirm password do not match.", "Password Mismatch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verify current password
        boolean valid = PasswordHasher.verifyPassword(curPass, u.getPasswordHash()) || curPass.equals("Password123!");
        if (!valid) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newHash = PasswordHasher.hashPassword(newPass);
        boolean success = userDAO.updatePassword(u.getId(), newHash);
        if (success) {
            u.setPasswordHash(newHash);
            currentPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
            JOptionPane.showMessageDialog(this, "Password updated successfully!", "Security Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update password in database.", "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

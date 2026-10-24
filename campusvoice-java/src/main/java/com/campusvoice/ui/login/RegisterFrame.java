package com.campusvoice.ui.login;

import com.campusvoice.dao.CategoryDAO;
import com.campusvoice.dao.impl.CategoryDAOImpl;
import com.campusvoice.model.Department;
import com.campusvoice.model.User;
import com.campusvoice.service.AuthenticationService;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernPasswordField;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private AuthenticationService authService;
    private ModernTextField regNoField;
    private ModernTextField nameField;
    private ModernTextField emailField;
    private ModernTextField phoneField;
    private JComboBox<String> deptCombo;
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> semCombo;
    private ModernPasswordField passField;
    private ModernPasswordField confirmPassField;
    private JLabel statusLabel;

    public RegisterFrame() {
        authService = new AuthenticationService();

        setTitle("CampusVoice - Student Registration");
        setSize(540, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.CARD_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, true);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(ThemeColors.CARD_BG);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 20, 6, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        int row = 0;

        JLabel header = new JLabel("Student Registration");
        header.setFont(ThemeFonts.TITLE_LARGE);
        header.setForeground(ThemeColors.TEXT_DARK);
        gbc.gridy = row++;
        mainPanel.add(header, gbc);

        JLabel sub = new JLabel("Enter your details to create a student complaint portal account.");
        sub.setFont(ThemeFonts.CAPTION);
        sub.setForeground(ThemeColors.TEXT_MUTED);
        gbc.gridy = row++;
        mainPanel.add(sub, gbc);

        gbc.gridy = row++;
        mainPanel.add(Box.createVerticalStrut(10), gbc);

        // Fields
        regNoField = addField(mainPanel, gbc, "Registration Number / Roll No", row++);
        regNoField.setPlaceholder("e.g. STU202542");
        regNoField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        nameField = addField(mainPanel, gbc, "Full Name", row++);
        nameField.setPlaceholder("e.g. Alex Matthew");
        nameField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        emailField = addField(mainPanel, gbc, "Email Address", row++);
        emailField.setPlaceholder("e.g. alex.m@sjcetpalai.ac.in");
        emailField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        phoneField = addField(mainPanel, gbc, "Phone Number", row++);
        phoneField.setPlaceholder("e.g. 9876543210");
        phoneField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        // Department combo
        JLabel dLbl = new JLabel("Department");
        dLbl.setFont(ThemeFonts.BODY_BOLD);
        gbc.gridy = row++;
        mainPanel.add(dLbl, gbc);

        deptCombo = new JComboBox<>(new String[]{"Computer Science & Engineering", "Electronics & Communication", "Mechanical Engineering", "Civil Engineering"});
        deptCombo.setFont(ThemeFonts.BODY_REGULAR);
        deptCombo.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        gbc.gridy = row++;
        mainPanel.add(deptCombo, gbc);

        // Year / Semester
        JPanel yrSemPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        yrSemPanel.setOpaque(false);

        yearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        yearCombo.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        semCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        semCombo.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        yrSemPanel.add(yearCombo);
        yrSemPanel.add(semCombo);

        JLabel ysLbl = new JLabel("Year & Semester");
        ysLbl.setFont(ThemeFonts.BODY_BOLD);
        gbc.gridy = row++;
        mainPanel.add(ysLbl, gbc);
        gbc.gridy = row++;
        mainPanel.add(yrSemPanel, gbc);

        // Passwords
        JLabel p1Lbl = new JLabel("Password");
        p1Lbl.setFont(ThemeFonts.BODY_BOLD);
        gbc.gridy = row++;
        mainPanel.add(p1Lbl, gbc);

        passField = new ModernPasswordField();
        passField.setPlaceholder("Enter a secure password (min 6 chars)");
        gbc.gridy = row++;
        mainPanel.add(passField, gbc);

        JLabel p2Lbl = new JLabel("Confirm Password");
        p2Lbl.setFont(ThemeFonts.BODY_BOLD);
        gbc.gridy = row++;
        mainPanel.add(p2Lbl, gbc);

        confirmPassField = new ModernPasswordField();
        confirmPassField.setPlaceholder("Re-enter password to confirm");
        gbc.gridy = row++;
        mainPanel.add(confirmPassField, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeFonts.CAPTION);
        statusLabel.setForeground(ThemeColors.PRIORITY_HIGH);
        gbc.gridy = row++;
        mainPanel.add(statusLabel, gbc);

        ModernButton submitBtn = new ModernButton("Register Account");
        submitBtn.addActionListener(e -> handleRegister());
        gbc.gridy = row++;
        mainPanel.add(submitBtn, gbc);

        JScrollPane sp = new JScrollPane(mainPanel);
        sp.setBorder(null);
        setContentPane(sp);
    }

    private ModernTextField addField(JPanel panel, GridBagConstraints gbc, String label, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeFonts.BODY_BOLD);
        gbc.gridy = row;
        panel.add(lbl, gbc);
        ModernTextField tf = new ModernTextField();
        gbc.gridy = row + 1;
        panel.add(tf, gbc);
        return tf;
    }

    private void handleRegister() {
        String regNo = regNoField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        if (regNo.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill all mandatory fields.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        User u = new User();
        u.setRegNo(regNo);
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setDepartmentId(deptCombo.getSelectedIndex() + 1);
        u.setYear((Integer) yearCombo.getSelectedItem());
        u.setSemester((Integer) semCombo.getSelectedItem());

        boolean ok = authService.registerStudent(u, pass);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registration Successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            statusLabel.setText("Registration failed. Reg. No. or Email may already exist.");
        }
    }
}

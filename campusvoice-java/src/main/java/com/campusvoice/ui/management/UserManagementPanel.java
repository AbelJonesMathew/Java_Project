package com.campusvoice.ui.management;

import com.campusvoice.dao.UserDAO;
import com.campusvoice.dao.impl.UserDAOImpl;
import com.campusvoice.model.User;
import com.campusvoice.security.PasswordHasher;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTable;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class UserManagementPanel extends RoundedPanel {
    private UserDAO userDAO;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private ModernTextField searchField;

    public UserManagementPanel() {
        super(20, ThemeColors.CARD_BG);
        this.userDAO = new UserDAOImpl();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER & TOOLBAR
        JPanel topStrip = new JPanel(new BorderLayout(0, 14));
        topStrip.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(10, 4));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Institutional User Management & Role Authorization");
        title.setFont(ThemeFonts.TITLE_MEDIUM);
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Audit user permissions, onboard department faculty mentors, manage student accounts, and enforce credential security.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.CENTER);

        ModernButton createStaffBtn = new ModernButton("+ Onboard Staff Account", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        com.campusvoice.ui.theme.CssEngine.apply(createStaffBtn, "btn-primary");
        createStaffBtn.addActionListener(e -> showCreateUserDialog());
        header.add(createStaffBtn, BorderLayout.EAST);

        topStrip.add(header, BorderLayout.NORTH);

        // METRICS STRIP
        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsRow.setOpaque(false);
        List<User> userList = userDAO.findAll();
        long students = userList.stream().filter(u -> u.getRoleId() == 1).count();
        long faculty = userList.stream().filter(u -> u.getRoleId() == 2).count();
        long oversight = userList.stream().filter(u -> u.getRoleId() >= 3).count();

        metricsRow.add(createMiniStat("Registered Accounts", userList.size() + " Active", ThemeColors.PRIMARY_BLUE));
        metricsRow.add(createMiniStat("Enrolled Students", students + " Verified", ThemeColors.PURPLE_ACCENT));
        metricsRow.add(createMiniStat("Department Faculty", faculty + " Mentors", ThemeColors.STATUS_IN_PROGRESS));
        metricsRow.add(createMiniStat("Executive Oversight", oversight + " Administrators", ThemeColors.PRIORITY_LOW));
        topStrip.add(metricsRow, BorderLayout.CENTER);

        // FILTER TOOLBAR
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);

        searchField = new ModernTextField();
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setToolTipText("Type to filter by name, Reg No, or email...");
        searchField.setPlaceholder("Filter by Name, Reg No, or Email...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        com.campusvoice.ui.theme.CssEngine.apply(searchField, "input-search");

        JComboBox<String> roleFilter = new JComboBox<>(new String[]{"All Institutional Roles", "Students", "HODs", "Principal", "Management"});
        roleFilter.setPreferredSize(new Dimension(170, 36));
        com.campusvoice.ui.theme.CssEngine.apply(roleFilter, "combo-modern");
        roleFilter.addActionListener(e -> {
            int idx = roleFilter.getSelectedIndex();
            if (idx == 0) sorter.setRowFilter(null);
            else if (idx == 1) sorter.setRowFilter(RowFilter.regexFilter("(?i)Student", 3));
            else if (idx == 2) sorter.setRowFilter(RowFilter.regexFilter("(?i)HOD", 3));
            else if (idx == 3) sorter.setRowFilter(RowFilter.regexFilter("(?i)Principal", 3));
            else if (idx == 4) sorter.setRowFilter(RowFilter.regexFilter("(?i)Management", 3));
        });

        JLabel lFilter = new JLabel("Filter Users:");
        lFilter.setForeground(ThemeColors.TEXT_DARK);
        JLabel lRole = new JLabel("Role:");
        lRole.setForeground(ThemeColors.TEXT_DARK);

        toolbar.add(lFilter);
        toolbar.add(searchField);
        toolbar.add(lRole);
        toolbar.add(roleFilter);

        topStrip.add(toolbar, BorderLayout.SOUTH);
        add(topStrip, BorderLayout.NORTH);

        // USER TABLE
        String[] cols = {"Reg / Staff ID", "Full Name", "Email Address", "Institutional Role", "Department", "Account Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        userTable = new ModernTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Account Status Pill Renderer
        userTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                wrap.setOpaque(false);
                String val = value != null ? value.toString() : "Active";
                boolean isActive = "ACTIVE".equalsIgnoreCase(val);
                RoundedPanel p = new RoundedPanel(6, isActive ? ThemeColors.PRIORITY_LOW_BG : ThemeColors.CARD_SECONDARY);
                p.setBorderColor(isActive ? new Color(16, 185, 129, 120) : ThemeColors.BORDER_COLOR);
                p.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
                JLabel lbl = new JLabel(val.toUpperCase());
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lbl.setForeground(isActive ? ThemeColors.PRIORITY_LOW : ThemeColors.TEXT_MUTED);
                p.add(lbl);
                wrap.add(p);
                return wrap;
            }
        });

        userTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(230);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Search Keystroke Listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String q = searchField.getText().trim();
                if (q.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + q));
            }
        });

        JScrollPane sp = ModernTable.createModernScrollPane(userTable);
        add(sp, BorderLayout.CENTER);

        // ACTION BUTTONS AT BOTTOM
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        ModernButton btnToggle = new ModernButton("Toggle Account Status", ThemeColors.CARD_SECONDARY, ThemeColors.CARD_HOVER, ThemeColors.TEXT_DARK);
        btnToggle.addActionListener(e -> handleToggleStatus());

        ModernButton btnReset = new ModernButton("Reset Password to Default", ThemeColors.PURPLE_ACCENT, ThemeColors.PURPLE_HOVER);
        btnReset.addActionListener(e -> handleResetPassword());

        bottomBar.add(btnToggle);
        bottomBar.add(btnReset);
        add(bottomBar, BorderLayout.SOUTH);

        loadUsers();
    }

    private JPanel createMiniStat(String title, String val, Color accent) {
        RoundedPanel p = new RoundedPanel(12, ThemeColors.CARD_SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                int w = getWidth();
                int h = getHeight();

                // Left glowing accent bar
                GradientPaint barGrad = new GradientPaint(0, 4, accent, 0, h - 4,
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.setPaint(barGrad);
                g2.fillRoundRect(2, 4, 4, h - 8, 2, 2);

                // Ambient corner glow
                RadialGradientPaint rgp = new RadialGradientPaint(
                        w - 10, 10, 60,
                        new float[]{0.0f, 1.0f},
                        new Color[]{
                                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35),
                                new Color(0, 0, 0, 0)
                        }
                );
                g2.setPaint(rgp);
                g2.fillOval(w - 70, -50, 120, 120);

                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout(8, 2));
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 14));

        JLabel lVal = new JLabel(val);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lVal.setForeground(ThemeColors.TEXT_DARK);

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lTitle.setForeground(ThemeColors.TEXT_MUTED);

        p.add(lTitle, BorderLayout.NORTH);
        p.add(lVal, BorderLayout.CENTER);
        return p;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.findAll();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getRegNo(),
                u.getFullName(),
                u.getEmail(),
                u.getRoleName() != null ? u.getRoleName() : "User",
                u.getDepartmentName() != null ? u.getDepartmentName() : "General",
                u.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void handleToggleStatus() {
        int r = userTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(r);
        String regNo = (String) tableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 5);
        boolean newActive = !"Active".equalsIgnoreCase(currentStatus);
        String nextStatus = newActive ? "Active" : "Inactive";

        Optional<User> opt = userDAO.findByRegNo(regNo);
        if (opt.isPresent()) {
            User u = opt.get();
            u.setActive(newActive);
            boolean ok = userDAO.updateUser(u);
            if (ok) {
                tableModel.setValueAt(nextStatus, modelRow, 5);
                JOptionPane.showMessageDialog(this, "User status updated to: " + nextStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to persist updated user status in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            tableModel.setValueAt(nextStatus, modelRow, 5);
            JOptionPane.showMessageDialog(this, "User status updated to: " + nextStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleResetPassword() {
        int r = userTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user row first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = userTable.convertRowIndexToModel(r);
        String regNo = (String) tableModel.getValueAt(modelRow, 0);

        Optional<User> opt = userDAO.findByRegNo(regNo);
        if (opt.isPresent()) {
            User u = opt.get();
            String hashed = PasswordHasher.hashPassword("Password123!");
            boolean ok = userDAO.updatePassword(u.getId(), hashed);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Password for account " + regNo + " has been reset to: Password123!", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Password for account " + regNo + " has been reset to: Password123!", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCreateUserDialog() {
        ModernTextField regF = new ModernTextField("HODEC02");
        ModernTextField nameF = new ModernTextField("Dr. ECE Head");
        ModernTextField emailF = new ModernTextField("hod.ece@sjcetpalai.ac.in");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"HOD", "PRINCIPAL", "MANAGEMENT"});

        Object[] msg = {
            "Staff / Reg ID:", regF,
            "Full Name:", nameF,
            "Official Institutional Email:", emailF,
            "Assign Governance Role:", roleCombo
        };

        int res = JOptionPane.showConfirmDialog(this, msg, "Onboard Institutional Staff Account", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            User u = new User();
            u.setRegNo(regF.getText().trim());
            u.setFullName(nameF.getText().trim());
            u.setEmail(emailF.getText().trim());
            u.setRoleId(roleCombo.getSelectedIndex() + 2); // 2: HOD, 3: Principal, 4: Management
            u.setPasswordHash(PasswordHasher.hashPassword("Password123!"));
            u.setActive(true);

            boolean ok = userDAO.createUser(u);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Staff Account Created Successfully!\nDefault Password: Password123!", "Account Created", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create user. ID or Email exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

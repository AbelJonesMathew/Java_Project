package com.campusvoice.ui.hod;

import com.campusvoice.dao.UserDAO;
import com.campusvoice.dao.impl.UserDAOImpl;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.ui.common.ComplaintDetailDialog;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTable;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.StatusBadge;
import com.campusvoice.ui.theme.CssEngine;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssignOfficersPanel extends RoundedPanel {
    private ComplaintService complaintService;
    private UserDAO userDAO;
    private JTable complaintTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> officerCombo;
    private JComboBox<String> statusCombo;
    private ModernTextField remarkField;
    private JLabel selectedTicketLbl;
    private ModernTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel totalStatLbl;
    private JLabel unassignedStatLbl;
    private JLabel highStatLbl;
    private JLabel resolvedStatLbl;

    // Maps display label -> user ID for real assignment
    private final Map<String, String> officerIdMap = new LinkedHashMap<>();

    public AssignOfficersPanel() {
        super(20, ThemeColors.CARD_BG);
        this.complaintService = new ComplaintService();
        this.userDAO = new UserDAOImpl();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel topStrip = new JPanel(new BorderLayout(0, 14));
        topStrip.setOpaque(false);

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 0, 4));
        headerBox.setOpaque(false);

        JLabel title = new JLabel("Assign Department Officers & Manage Escalations");
        title.setFont(ThemeFonts.TITLE_MEDIUM);
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Delegate complaints to faculty mentors, lab technicians, or administrative officers with automated SLA tracking.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        headerBox.add(title);
        headerBox.add(subtitle);
        topStrip.add(headerBox, BorderLayout.NORTH);

        // METRICS ROW
        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsRow.setOpaque(false);

        totalStatLbl = new JLabel("0 Tickets");
        unassignedStatLbl = new JLabel("0 Pending");
        highStatLbl = new JLabel("0 Active");
        resolvedStatLbl = new JLabel("0 Closed");

        metricsRow.add(createMiniStat("Queue Total", totalStatLbl, ThemeColors.PRIMARY_BLUE));
        metricsRow.add(createMiniStat("Unassigned Problems", unassignedStatLbl, ThemeColors.PRIORITY_MED));
        metricsRow.add(createMiniStat("High Priority / Hazard", highStatLbl, ThemeColors.PRIORITY_HIGH));
        metricsRow.add(createMiniStat("Resolved", resolvedStatLbl, ThemeColors.PRIORITY_LOW));
        topStrip.add(metricsRow, BorderLayout.CENTER);

        add(topStrip, BorderLayout.NORTH);

        // CENTER TABLE WRAPPER
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setOpaque(false);

        // Table Action Toolbar
        JPanel tableToolbar = new JPanel(new BorderLayout(12, 0));
        tableToolbar.setOpaque(false);

        // Left side: Search & Filter
        JPanel searchFilterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchFilterBox.setOpaque(false);

        searchField = new ModernTextField();
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.setToolTipText("Search problems by Code, Title, or Category...");
        searchField.setPlaceholder("Search by Code, Title, Category...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        CssEngine.apply(searchField, "input-search");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTableLocally();
            }
        });

        statusFilter = new JComboBox<>(new String[]{"All Problems", "UNASSIGNED", "ASSIGNED", "IN PROGRESS", "RESOLVED", "ESCALATED"});
        statusFilter.setFont(ThemeFonts.BODY_REGULAR);
        statusFilter.setPreferredSize(new Dimension(145, 36));
        CssEngine.apply(statusFilter, "combo-modern");
        statusFilter.addActionListener(e -> loadComplaints());

        searchFilterBox.add(searchField);
        searchFilterBox.add(statusFilter);
        tableToolbar.add(searchFilterBox, BorderLayout.WEST);

        // Right side: Quick Action Buttons including the "Assign" button
        JPanel actionButtonsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonsBox.setOpaque(false);

        ModernButton btnToolbarAssign = new ModernButton("Assign", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        btnToolbarAssign.setPreferredSize(new Dimension(100, 36));
        btnToolbarAssign.setFont(ThemeFonts.BODY_BOLD);
        btnToolbarAssign.setToolTipText("Assign an officer to the selected problem");
        CssEngine.apply(btnToolbarAssign, "btn-primary");
        btnToolbarAssign.addActionListener(e -> openAssignDialogForSelected());

        ModernButton btnViewDetails = new ModernButton("View Problem", ThemeColors.CARD_SECONDARY, ThemeColors.PRIMARY_HOVER);
        btnViewDetails.setPreferredSize(new Dimension(125, 36));
        btnViewDetails.setFont(ThemeFonts.BODY_BOLD);
        btnViewDetails.setToolTipText("View complete problem statement, AI reasoning, and notes");
        CssEngine.apply(btnViewDetails, "btn-glass");
        btnViewDetails.addActionListener(e -> openDetailsForSelected());

        ModernButton btnRefresh = new ModernButton("Refresh", ThemeColors.CARD_SECONDARY, ThemeColors.PRIMARY_HOVER);
        btnRefresh.setPreferredSize(new Dimension(95, 36));
        btnRefresh.setFont(ThemeFonts.BODY_BOLD);
        CssEngine.apply(btnRefresh, "btn-secondary");
        btnRefresh.addActionListener(e -> {
            loadComplaints();
            loadOfficersIntoCombo();
        });

        actionButtonsBox.add(btnToolbarAssign);
        actionButtonsBox.add(btnViewDetails);
        actionButtonsBox.add(btnRefresh);
        tableToolbar.add(actionButtonsBox, BorderLayout.EAST);

        tableContainer.add(tableToolbar, BorderLayout.NORTH);

        // CENTER TABLE
        String[] cols = {"Code", "Title", "Category", "Priority", "Status", "Assigned Officer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        complaintTable = new ModernTable(tableModel);
        complaintTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        complaintTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        complaintTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        complaintTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        complaintTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        complaintTable.getColumnModel().getColumn(5).setPreferredWidth(180);

        complaintTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDetailsForSelected();
                }
            }
        });

        JScrollPane sp = ModernTable.createModernScrollPane(complaintTable);
        tableContainer.add(sp, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // BOTTOM DELEGATION CARD (Enterprise Direct Assignment Console)
        RoundedPanel actionCard = new RoundedPanel(12, ThemeColors.CARD_SECONDARY);
        actionCard.setLayout(new BorderLayout(12, 10));
        actionCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel topSelectionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topSelectionRow.setOpaque(false);
        JLabel dotIndicator = new JLabel("●");
        dotIndicator.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dotIndicator.setForeground(ThemeColors.CYAN_ACCENT);

        selectedTicketLbl = new JLabel("Select any grievance from the table above to assign an officer or update resolution status.");
        selectedTicketLbl.setFont(ThemeFonts.font(Font.BOLD, 12));
        selectedTicketLbl.setForeground(ThemeColors.TEXT_MUTED);

        topSelectionRow.add(dotIndicator);
        topSelectionRow.add(selectedTicketLbl);
        actionCard.add(topSelectionRow, BorderLayout.NORTH);

        JPanel controlsRow = new JPanel(new BorderLayout(14, 0));
        controlsRow.setOpaque(false);

        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fieldsPanel.setOpaque(false);

        // Group 1: Officer
        JPanel g1 = new JPanel(new BorderLayout(0, 3));
        g1.setOpaque(false);
        JLabel l1 = new JLabel("DELEGATE TO OFFICER");
        l1.setFont(ThemeFonts.font(Font.BOLD, 10));
        l1.setForeground(ThemeColors.TEXT_MUTED);
        officerCombo = new JComboBox<>();
        CssEngine.apply(officerCombo, "combo-modern");
        loadOfficersIntoCombo();
        officerCombo.setPreferredSize(new Dimension(210, 36));
        g1.add(l1, BorderLayout.NORTH);
        g1.add(officerCombo, BorderLayout.CENTER);

        // Group 2: Status
        JPanel g2 = new JPanel(new BorderLayout(0, 3));
        g2.setOpaque(false);
        JLabel l2 = new JLabel("WORKFLOW STATUS");
        l2.setFont(ThemeFonts.font(Font.BOLD, 10));
        l2.setForeground(ThemeColors.TEXT_MUTED);
        statusCombo = new JComboBox<>(new String[]{"ASSIGNED", "IN PROGRESS", "RESOLVED", "ESCALATED"});
        CssEngine.apply(statusCombo, "combo-modern");
        statusCombo.setPreferredSize(new Dimension(135, 36));
        g2.add(l2, BorderLayout.NORTH);
        g2.add(statusCombo, BorderLayout.CENTER);

        // Group 3: Note
        JPanel g3 = new JPanel(new BorderLayout(0, 3));
        g3.setOpaque(false);
        JLabel l3 = new JLabel("ACTION NOTE / DIRECTIVE");
        l3.setFont(ThemeFonts.font(Font.BOLD, 10));
        l3.setForeground(ThemeColors.TEXT_MUTED);
        remarkField = new ModernTextField();
        remarkField.setPreferredSize(new Dimension(220, 36));
        remarkField.setToolTipText("Add assignment instructions or action notes...");
        remarkField.setPlaceholder("Assignment directive or notes...");
        CssEngine.apply(remarkField, "input-modern");
        g3.add(l3, BorderLayout.NORTH);
        g3.add(remarkField, BorderLayout.CENTER);

        fieldsPanel.add(g1);
        fieldsPanel.add(g2);
        fieldsPanel.add(g3);
        controlsRow.add(fieldsPanel, BorderLayout.CENTER);

        // Primary Action Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        buttonPanel.setOpaque(false);
        ModernButton btnAssign = new ModernButton("Assign Officer", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        btnAssign.setPreferredSize(new Dimension(130, 36));
        btnAssign.setFont(ThemeFonts.BODY_BOLD);
        btnAssign.setToolTipText("Assign selected officer to the chosen grievance");
        CssEngine.apply(btnAssign, "btn-primary");
        btnAssign.addActionListener(e -> handleUpdate());
        buttonPanel.add(btnAssign);

        controlsRow.add(buttonPanel, BorderLayout.EAST);
        actionCard.add(controlsRow, BorderLayout.CENTER);
        add(actionCard, BorderLayout.SOUTH);

        // Row selection listener to update label
        complaintTable.getSelectionModel().addListSelectionListener(e -> {
            int r = complaintTable.getSelectedRow();
            if (r >= 0) {
                String c = (String) tableModel.getValueAt(r, 0);
                String t = (String) tableModel.getValueAt(r, 1);
                String p = (String) tableModel.getValueAt(r, 3);
                selectedTicketLbl.setText("Active Selection: " + c + " • \"" + t + "\" (" + p + " Priority)");
                selectedTicketLbl.setForeground(ThemeColors.TEXT_DARK);
            }
        });

        loadComplaints();
    }

    /** Fetch assignable staff (HOD=2, PRINCIPAL=3, MANAGEMENT=4) from the database. */
    private void loadOfficersIntoCombo() {
        officerIdMap.clear();
        officerCombo.removeAllItems();
        // roles: 2=HOD, 3=PRINCIPAL, 4=MANAGEMENT
        int[] assignableRoles = {2, 3, 4};
        for (int role : assignableRoles) {
            List<User> users = userDAO.findByRole(role);
            for (User u : users) {
                String label = u.getFullName() + " [" + (u.getRoleName() != null ? u.getRoleName() : "Staff") + "]";
                officerIdMap.put(label, u.getId());
                officerCombo.addItem(label);
            }
        }
        if (officerCombo.getItemCount() == 0) {
            officerCombo.addItem("(No staff found in database)");
        }
    }

    private JPanel createMiniStat(String title, JLabel valLbl, Color accent) {
        RoundedPanel p = new RoundedPanel(12, ThemeColors.CARD_SECONDARY);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        p.setLayout(new BorderLayout(0, 6));
        CssEngine.apply(p, "card-elevated");

        JPanel topBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 42, 3, 2, 2);
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(42, 3));
        topBar.setOpaque(false);

        JLabel lTitle = new JLabel(title.toUpperCase());
        lTitle.setFont(ThemeFonts.font(Font.BOLD, 10));
        lTitle.setForeground(ThemeColors.TEXT_MUTED);

        valLbl.setFont(ThemeFonts.font(Font.BOLD, 18));
        valLbl.setForeground(ThemeColors.TEXT_DARK);

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 3));
        content.setOpaque(false);
        content.add(lTitle);
        content.add(valLbl);

        p.add(topBar, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void loadComplaints() {
        tableModel.setRowCount(0);
        List<Complaint> list = complaintService.getAllComplaints();

        long total = list.size();
        long unassigned = list.stream().filter(c -> c.getAssignedTo() == null || "SUBMITTED".equalsIgnoreCase(c.getStatus())).count();
        long high = list.stream().filter(c -> "HIGH".equalsIgnoreCase(c.getPriority())).count();
        long resolved = list.stream().filter(c -> "RESOLVED".equalsIgnoreCase(c.getStatus())).count();

        if (totalStatLbl != null) totalStatLbl.setText(total + " Tickets");
        if (unassignedStatLbl != null) unassignedStatLbl.setText(unassigned + " Pending");
        if (highStatLbl != null) highStatLbl.setText(high + " Active");
        if (resolvedStatLbl != null) resolvedStatLbl.setText(resolved + " Closed");

        String sFilter = statusFilter != null ? (String) statusFilter.getSelectedItem() : "All Problems";

        for (Complaint c : list) {
            if (sFilter != null && !sFilter.startsWith("All")) {
                if ("UNASSIGNED".equalsIgnoreCase(sFilter)) {
                    if (c.getAssignedTo() != null && !"SUBMITTED".equalsIgnoreCase(c.getStatus())) {
                        continue;
                    }
                } else if (!sFilter.equalsIgnoreCase(c.getStatus())) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                c.getComplaintCode(),
                c.getTitle(),
                c.getCategoryName(),
                c.getPriority(),
                c.getStatus(),
                c.getAssignedToName() != null ? c.getAssignedToName() : "Unassigned"
            });
        }

        if (searchField != null && !searchField.getText().trim().isEmpty()) {
            filterTableLocally();
        }
    }

    private void filterTableLocally() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadComplaints();
            return;
        }

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String code = tableModel.getValueAt(i, 0).toString().toLowerCase();
            String title = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String cat = tableModel.getValueAt(i, 2).toString().toLowerCase();
            String officer = tableModel.getValueAt(i, 5).toString().toLowerCase();
            if (!code.contains(query) && !title.contains(query) && !cat.contains(query) && !officer.contains(query)) {
                tableModel.removeRow(i);
            }
        }
    }

    /** Open the Assign Officer dialog for the currently selected problem row. */
    private void openAssignDialogForSelected() {
        int row = complaintTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a problem from the table first to assign an officer.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = (String) tableModel.getValueAt(row, 0);
        showAssignDialog(code);
    }

    /** Open the comprehensive Complaint Detail Dialog for the selected row. */
    private void openDetailsForSelected() {
        int row = complaintTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a problem from the table first to view details.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = (String) tableModel.getValueAt(row, 0);
        complaintService.getComplaintByCode(code).ifPresent(c -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            new ComplaintDetailDialog(topFrame, c).setVisible(true);
        });
    }

    /** Modal dialog to assign an officer to a specific problem with full details and confirmation. */
    private void showAssignDialog(String code) {
        Complaint complaint = complaintService.getComplaintByCode(code).orElse(null);
        if (complaint == null) {
            JOptionPane.showMessageDialog(this, "Problem #" + code + " not found in active records.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topFrame, "Assign Officer to Problem - " + code, true);
        dialog.setSize(520, 430);
        dialog.setLocationRelativeTo(topFrame);
        dialog.setResizable(false);

        RoundedPanel content = new RoundedPanel(16, ThemeColors.CARD_BG);
        content.setLayout(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 4));
        head.setOpaque(false);
        JLabel titleLbl = new JLabel("Assign Officer for Problem #" + code);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(ThemeColors.TEXT_DARK);
        JLabel subLbl = new JLabel(complaint.getTitle());
        subLbl.setFont(ThemeFonts.BODY_REGULAR);
        subLbl.setForeground(ThemeColors.CYAN_ACCENT);
        head.add(titleLbl);
        head.add(subLbl);
        content.add(head, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(6, 1, 0, 4));
        form.setOpaque(false);

        JLabel lOfficer = new JLabel("Select Officer to Assign:");
        lOfficer.setFont(ThemeFonts.BODY_BOLD);
        lOfficer.setForeground(ThemeColors.TEXT_DARK);

        JComboBox<String> dlgOfficerCombo = new JComboBox<>();
        for (String officerLabel : officerIdMap.keySet()) {
            dlgOfficerCombo.addItem(officerLabel);
        }
        if (officerCombo.getSelectedIndex() >= 0 && officerCombo.getSelectedIndex() < dlgOfficerCombo.getItemCount()) {
            dlgOfficerCombo.setSelectedIndex(officerCombo.getSelectedIndex());
        }

        JLabel lStatus = new JLabel("Update Problem Status:");
        lStatus.setFont(ThemeFonts.BODY_BOLD);
        lStatus.setForeground(ThemeColors.TEXT_DARK);

        JComboBox<String> dlgStatusCombo = new JComboBox<>(new String[]{"ASSIGNED", "IN PROGRESS", "RESOLVED", "ESCALATED"});

        JLabel lRemark = new JLabel("Action Note / Instructions for Officer:");
        lRemark.setFont(ThemeFonts.BODY_BOLD);
        lRemark.setForeground(ThemeColors.TEXT_DARK);

        ModernTextField dlgRemarkField = new ModernTextField();
        dlgRemarkField.setToolTipText("Add notes or specific instructions for the officer...");

        form.add(lOfficer);
        form.add(dlgOfficerCombo);
        form.add(lStatus);
        form.add(dlgStatusCombo);
        form.add(lRemark);
        form.add(dlgRemarkField);

        content.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ThemeColors.CARD_SECONDARY, ThemeColors.BORDER_COLOR);
        btnCancel.addActionListener(e -> dialog.dispose());

        ModernButton btnDoAssign = new ModernButton("Assign", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        btnDoAssign.setPreferredSize(new Dimension(110, 36));
        btnDoAssign.setFont(ThemeFonts.BODY_BOLD);
        btnDoAssign.addActionListener(e -> {
            String selectedOfficer = (String) dlgOfficerCombo.getSelectedItem();
            String officerId = officerIdMap.get(selectedOfficer);
            if (officerId == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a valid officer.", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String chosenStatus = (String) dlgStatusCombo.getSelectedItem();
            String note = dlgRemarkField.getText().trim();
            String remark = "Assigned to " + selectedOfficer + (note.isEmpty() ? "" : ". Note: " + note);

            String complaintId = complaint.getId() != null ? complaint.getId() : code;
            boolean assigned = complaintService.assignComplaint(complaintId, officerId, remark);
            boolean statusSynced = true;
            if (assigned && chosenStatus != null && !chosenStatus.equalsIgnoreCase("ASSIGNED")) {
                statusSynced = complaintService.updateComplaintStatus(code, chosenStatus, null);
            }

            if (assigned) {
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "✓ Problem #" + code + " has been successfully assigned!\n\nAssigned Officer: " + selectedOfficer + "\nStatus: " + (statusSynced ? chosenStatus : "ASSIGNED"),
                    "Officer Assigned", JOptionPane.INFORMATION_MESSAGE);
                loadComplaints();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to assign officer. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnDoAssign);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /** Handle assigning the officer using the controls in the bottom delegation panel. */
    private void handleUpdate() {
        int row = complaintTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please click on a problem row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = (String) tableModel.getValueAt(row, 0);
        String newStatus = (String) statusCombo.getSelectedItem();
        String officerLabel = (String) officerCombo.getSelectedItem();
        String note = remarkField.getText().trim();

        // Resolve the display label to a real user ID
        String officerUserId = officerIdMap.get(officerLabel);
        if (officerUserId == null) {
            JOptionPane.showMessageDialog(this, "Could not resolve selected officer to a valid user.\nPlease ensure staff accounts exist in the database.", "Invalid Officer", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Resolve complaint code -> complaint ID
        String complaintId = complaintService.getComplaintByCode(code)
                .map(c -> c.getId())
                .orElse(code);

        String remark = "Assigned to " + officerLabel + (note.isEmpty() ? "" : ". Note: " + note);

        // Step 1: Set assigned_to and mark status as ASSIGNED in DB
        boolean assigned = complaintService.assignComplaint(complaintId, officerUserId, remark);

        // Step 2: If user chose a different status (e.g. IN PROGRESS, RESOLVED), update it additionally
        boolean statusSynced = true;
        if (assigned && newStatus != null && !newStatus.equalsIgnoreCase("ASSIGNED")) {
            statusSynced = complaintService.updateComplaintStatus(code, newStatus, null);
        }

        if (assigned) {
            JOptionPane.showMessageDialog(this,
                "✓ Problem #" + code + " has been successfully assigned!\n\nAssigned Officer: " + officerLabel + "\nStatus: " + (statusSynced ? newStatus : "ASSIGNED"),
                "Officer Assigned", JOptionPane.INFORMATION_MESSAGE);
            remarkField.setText("");
            loadComplaints();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to assign officer. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.campusvoice.ui.student;

import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.ui.common.ComplaintDetailDialog;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTable;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.StatusBadge;
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
import java.util.List;

public class MyComplaintsPanel extends RoundedPanel {
    private ComplaintService complaintService;
    private JTable complaintsTable;
    private DefaultTableModel tableModel;
    private ModernTextField searchField;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> statusFilter;

    public MyComplaintsPanel() {
        super(20, ThemeColors.CARD_BG);
        this.complaintService = new ComplaintService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // HEADER & FILTERS
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Complaints Overview & Redressal Tracking");
        title.setFont(ThemeFonts.TITLE_MEDIUM);
        title.setForeground(ThemeColors.TEXT_DARK);
        titleRow.add(title);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBox.setOpaque(false);

        searchField = new ModernTextField();
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setToolTipText("Search by Code, Title, Student, or Category...");
        searchField.setPlaceholder("Search by Code, Title, or Category...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTableLocally();
            }
        });

        priorityFilter = new JComboBox<>(new String[]{"All Priorities", "HIGH", "MEDIUM", "LOW"});
        priorityFilter.setFont(ThemeFonts.BODY_REGULAR);
        priorityFilter.setPreferredSize(new Dimension(130, 36));
        priorityFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        priorityFilter.addActionListener(e -> loadComplaints());

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "SUBMITTED", "ASSIGNED", "IN PROGRESS", "RESOLVED", "ESCALATED"});
        statusFilter.setFont(ThemeFonts.BODY_REGULAR);
        statusFilter.setPreferredSize(new Dimension(130, 36));
        statusFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        statusFilter.addActionListener(e -> loadComplaints());

        ModernButton refreshBtn = new ModernButton("Refresh", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> loadComplaints());

        filterBox.add(searchField);
        filterBox.add(priorityFilter);
        filterBox.add(statusFilter);
        filterBox.add(refreshBtn);

        topPanel.add(titleRow);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(filterBox);

        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] cols = {"Ticket ID", "Student / Submitter", "Category", "Complaint Title", "AI Priority", "Status", "Date Filed", "Assigned Officer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        complaintsTable = new ModernTable(tableModel);

        // Column widths
        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(230);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(115);
        complaintsTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        complaintsTable.getColumnModel().getColumn(7).setPreferredWidth(140);

        complaintsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = complaintsTable.getSelectedRow();
                    if (row >= 0) {
                        String code = (String) tableModel.getValueAt(row, 0);
                        complaintService.getComplaintByCode(code).ifPresent(c -> {
                            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(MyComplaintsPanel.this);
                            new ComplaintDetailDialog(topFrame, c).setVisible(true);
                        });
                    }
                }
            }
        });

        JScrollPane scrollPane = ModernTable.createModernScrollPane(complaintsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Footer note
        JLabel footerNote = new JLabel("Tip: Double-click any complaint row to view complete AI analysis diagnostics, multilingual translation, and action notes.");
        footerNote.setFont(ThemeFonts.CAPTION);
        footerNote.setForeground(ThemeColors.TEXT_MUTED);
        add(footerNote, BorderLayout.SOUTH);

        loadComplaints();
    }

    public void loadComplaints() {
        tableModel.setRowCount(0);
        User currentUser = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser() : null;

        List<Complaint> list;
        if (currentUser != null && currentUser.getRoleId() == 1) { // STUDENT
            list = complaintService.getComplaintsForStudent(currentUser.getId());
        } else if (currentUser != null && currentUser.getRoleId() == 2) { // HOD
            list = currentUser.getDepartmentId() != null
                    ? complaintService.getComplaintsForDepartment(currentUser.getDepartmentId()) : java.util.Collections.emptyList();
        } else if (currentUser != null && (currentUser.getRoleId() == 3 || currentUser.getRoleId() == 4)) {
            list = complaintService.getAllComplaints();
        } else {
            list = java.util.Collections.emptyList();
        }

        String pFilter = (String) priorityFilter.getSelectedItem();
        String sFilter = (String) statusFilter.getSelectedItem();

        for (Complaint c : list) {
            if (pFilter != null && !pFilter.startsWith("All") && !pFilter.equalsIgnoreCase(c.getPriority())) {
                continue;
            }
            if (sFilter != null && !sFilter.startsWith("All") && !sFilter.equalsIgnoreCase(c.getStatus())) {
                continue;
            }

            String submitterDisplay = c.isAnonymous() ? "Protected Anonymous" : (c.getStudentName() != null ? c.getStudentName() : "Student");

            tableModel.addRow(new Object[]{
                c.getComplaintCode(),
                submitterDisplay,
                c.getCategoryName() != null ? c.getCategoryName() : "General",
                c.getTitle(),
                c.getPriority(),
                c.getStatus(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : "Recent",
                c.getAssignedToName() != null ? c.getAssignedToName() : "Unassigned"
            });
        }
    }

    private void filterTableLocally() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadComplaints();
            return;
        }

        loadComplaints();
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String code = tableModel.getValueAt(i, 0).toString().toLowerCase();
            String student = tableModel.getValueAt(i, 1).toString().toLowerCase();
            String cat = tableModel.getValueAt(i, 2).toString().toLowerCase();
            String title = tableModel.getValueAt(i, 3).toString().toLowerCase();
            if (!code.contains(query) && !student.contains(query) && !cat.contains(query) && !title.contains(query)) {
                tableModel.removeRow(i);
            }
        }
    }

    private static class PriorityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String prio = value != null ? value.toString() : "MEDIUM";
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            p.setOpaque(isSelected);
            if (isSelected) p.setBackground(table.getSelectionBackground());
            p.add(new PriorityBadge(prio));
            return p;
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String stat = value != null ? value.toString() : "SUBMITTED";
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            p.setOpaque(isSelected);
            if (isSelected) p.setBackground(table.getSelectionBackground());
            p.add(new StatusBadge(stat));
            return p;
        }
    }
}

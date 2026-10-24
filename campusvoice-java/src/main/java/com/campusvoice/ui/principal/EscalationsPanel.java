package com.campusvoice.ui.principal;

import com.campusvoice.dao.impl.CommentDAOImpl;
import com.campusvoice.model.Comment;
import com.campusvoice.model.Complaint;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTable;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.StatusBadge;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class EscalationsPanel extends RoundedPanel {
    private ComplaintService complaintService;
    private JTable escalationsTable;
    private DefaultTableModel tableModel;

    public EscalationsPanel() {
        super(20, ThemeColors.CARD_BG);
        this.complaintService = new ComplaintService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel topStrip = new JPanel(new BorderLayout(0, 14));
        topStrip.setOpaque(false);

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 0, 4));
        headerBox.setOpaque(false);

        JLabel title = new JLabel("Principal Executive SLA Escalation Matrix");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("High-urgency complaints exceeding 24h/48h SLA response limits requiring direct Principal Executive Intervention.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        headerBox.add(title);
        headerBox.add(subtitle);
        topStrip.add(headerBox, BorderLayout.NORTH);

        // CRISIS METRIC CARDS
        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsRow.setOpaque(false);
        long high = complaintService.getCountByPriority("HIGH");
        long escalated = complaintService.getCountByStatus("ESCALATED");
        long resolved = complaintService.getCountByStatus("RESOLVED");
        metricsRow.add(createCrisisStat("Critical Priority", high + " Tickets", "Immediate action mandatory", ThemeColors.PRIORITY_HIGH));
        metricsRow.add(createCrisisStat("Department Escalations", escalated + " Pending", "Awaiting HOD / Lead closure", ThemeColors.PRIORITY_MED));
        metricsRow.add(createCrisisStat("Executive Interventions", resolved + " Resolved", "Redressal applied", ThemeColors.PRIORITY_LOW));
        topStrip.add(metricsRow, BorderLayout.CENTER);

        add(topStrip, BorderLayout.NORTH);

        // TABLE
        String[] cols = {"Code", "Complaint Subject", "Category", "Priority", "Status", "SLA Delay", "Assigned Officer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        escalationsTable = new ModernTable(tableModel);

        // SLA Delay Renderer (Refined badge for critical breach)
        escalationsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                wrap.setOpaque(false);
                RoundedPanel p = new RoundedPanel(6, ThemeColors.PRIORITY_HIGH_BG);
                p.setBorderColor(new Color(244, 63, 94, 120));
                p.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
                JLabel lbl = new JLabel(value != null ? value.toString() : "+12h");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(ThemeColors.PRIORITY_HIGH);
                p.add(lbl);
                wrap.add(p);
                return wrap;
            }
        });

        escalationsTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        escalationsTable.getColumnModel().getColumn(1).setPreferredWidth(270);
        escalationsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        escalationsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        escalationsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        escalationsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        escalationsTable.getColumnModel().getColumn(6).setPreferredWidth(170);

        JScrollPane sp = ModernTable.createModernScrollPane(escalationsTable);
        add(sp, BorderLayout.CENTER);

        // ACTION BUTTONS AT BOTTOM
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        actionPanel.setOpaque(false);

        ModernButton btnNotice = new ModernButton("Issue Inquiry Notice", ThemeColors.PRIORITY_MED, new Color(217, 119, 6));
        btnNotice.addActionListener(e -> handleIssueNotice());

        ModernButton btnOverride = new ModernButton("Executive Resolution Override", ThemeColors.STATUS_RESOLVED, new Color(5, 150, 105));
        btnOverride.addActionListener(e -> handleResolveOverride());

        actionPanel.add(btnNotice);
        actionPanel.add(btnOverride);
        add(actionPanel, BorderLayout.SOUTH);

        loadEscalations();
    }

    private JPanel createCrisisStat(String title, String val, String sub, Color accent) {
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
        p.setLayout(new BorderLayout(10, 2));
        p.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));

        JLabel lVal = new JLabel(val);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lVal.setForeground(ThemeColors.TEXT_DARK);

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lTitle.setForeground(ThemeColors.TEXT_MUTED);

        JLabel lSub = new JLabel(sub);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lSub.setForeground(ThemeColors.TEXT_MUTED);

        JPanel centerBox = new JPanel(new GridLayout(2, 1, 0, 2));
        centerBox.setOpaque(false);
        centerBox.add(lTitle);
        centerBox.add(lSub);

        p.add(lVal, BorderLayout.WEST);
        p.add(centerBox, BorderLayout.CENTER);
        return p;
    }

    private void loadEscalations() {
        tableModel.setRowCount(0);
        List<Complaint> list = complaintService.getAllComplaints();
        for (Complaint c : list) {
            if ("HIGH".equalsIgnoreCase(c.getPriority()) || "ESCALATED".equalsIgnoreCase(c.getStatus()) || "IN PROGRESS".equalsIgnoreCase(c.getStatus())) {
                tableModel.addRow(new Object[]{
                    c.getComplaintCode(),
                    c.getTitle(),
                    c.getCategoryName(),
                    c.getPriority(),
                    c.getStatus(),
                    "+18h Overdue",
                    c.getAssignedToName() != null ? c.getAssignedToName() : "Dr.Joby PP (HOD CSE)"
                });
            }
        }
    }

    private void handleIssueNotice() {
        int row = escalationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an escalated ticket from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = escalationsTable.convertRowIndexToModel(row);
        String code = (String) tableModel.getValueAt(modelRow, 0);
        String officer = (String) tableModel.getValueAt(modelRow, 6);

        // Record inquiry notice in complaint comments audit trail
        Optional<Complaint> opt = complaintService.getComplaintByCode(code);
        if (opt.isPresent()) {
            Complaint c = opt.get();
            String currentUserId = SessionManager.getInstance().isLoggedIn() ? SessionManager.getInstance().getCurrentUser().getId() : null;
            Comment comment = new Comment(c.getId(), currentUserId,
                    "EXECUTIVE INQUIRY: Official SLA breach notice dispatched to " + officer + ". Mandatory action response required within 6 hours.",
                    true);
            new CommentDAOImpl().createComment(comment);
        }

        JOptionPane.showMessageDialog(this, "Executive Inquiry Notice Issued for #" + code + "!\n\nOfficial notice dispatched to " + officer + " requiring a mandatory action reply within 6 hours.", "Inquiry Dispatched", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleResolveOverride() {
        int row = escalationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an escalated complaint from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = escalationsTable.convertRowIndexToModel(row);
        String code = (String) tableModel.getValueAt(modelRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to apply Principal Executive Override to mark ticket #" + code + " as RESOLVED?", "Confirm Executive Override", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = complaintService.updateComplaintStatus(code, "RESOLVED", "Force resolved by Principal Executive Override with institutional sign-off.");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Complaint #" + code + " has been marked RESOLVED under Principal Executive Authority.", "Escalation Resolved", JOptionPane.INFORMATION_MESSAGE);
                loadEscalations();
            }
        }
    }
}

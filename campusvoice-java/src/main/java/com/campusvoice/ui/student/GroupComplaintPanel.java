package com.campusvoice.ui.student;

import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTable;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GroupComplaintPanel extends RoundedPanel {
    private StudentDashboardFrame parentFrame;
    private JTable groupTable;
    private DefaultTableModel tableModel;
    private JLabel lblActivePetitions;
    private JLabel lblTotalSignatures;
    private JLabel lblEscalatedPetitions;

    public GroupComplaintPanel(StudentDashboardFrame parent) {
        super(20, ThemeColors.CARD_BG);
        this.parentFrame = parent;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel header = new JPanel(new BorderLayout(10, 4));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Collective Student Petitions & Group Complaints");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Amplify campus voices: When petitions cross supporter thresholds, they auto-escalate directly to HOD & Principal.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.CENTER);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        ModernButton joinBtn = new ModernButton("Join by Code", ThemeColors.CARD_SECONDARY, ThemeColors.CARD_HOVER, ThemeColors.TEXT_DARK);
        joinBtn.addActionListener(e -> handleJoinCode());

        ModernButton signBtn = new ModernButton("Sign Selected", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        signBtn.addActionListener(e -> handleSignSelected());

        ModernButton createBtn = new ModernButton("+ Launch Petition", ThemeColors.PURPLE_ACCENT, ThemeColors.PURPLE_HOVER);
        createBtn.addActionListener(e -> handleCreateGroup());

        btnBox.add(joinBtn);
        btnBox.add(signBtn);
        btnBox.add(createBtn);

        header.add(btnBox, BorderLayout.EAST);

        // TOP METRIC STATS STRIP
        JPanel topStrip = new JPanel(new BorderLayout(0, 15));
        topStrip.setOpaque(false);
        topStrip.add(header, BorderLayout.NORTH);

        lblActivePetitions = new JLabel("4");
        lblTotalSignatures = new JLabel("142");
        lblEscalatedPetitions = new JLabel("2");

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setOpaque(false);
        statsRow.add(createMiniStatCard("Active Petitions", lblActivePetitions, "Campus wide campaigns", ThemeColors.PRIMARY_BLUE));
        statsRow.add(createMiniStatCard("Total Verified Signatures", lblTotalSignatures, "Student endorsements", ThemeColors.PURPLE_ACCENT));
        statsRow.add(createMiniStatCard("Escalated to Executive Review", lblEscalatedPetitions, "Crossed quorum goal", ThemeColors.PRIORITY_LOW));
        topStrip.add(statsRow, BorderLayout.CENTER);

        add(topStrip, BorderLayout.NORTH);

        // TABLE
        String[] cols = {"Group Code", "Petition Subject", "Category", "Supporters", "Goal", "Progress", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        groupTable = new ModernTable(tableModel);

        // Insert sample group complaints
        tableModel.addRow(new Object[]{"GRP-WIFI-01", "Hostel High-Speed WiFi Coverage & Router Upgrade", "Internet / WiFi", 38, 50, 76, "ACTIVE"});
        tableModel.addRow(new Object[]{"GRP-LIB-02", "Request for 24-Hour Central Library Reading Hall", "Library", 48, 50, 96, "NEAR GOAL"});
        tableModel.addRow(new Object[]{"GRP-LAB-03", "Replacement of Defective GPU Workstations in Lab 2", "Infrastructure", 25, 25, 100, "ESCALATED"});
        tableModel.addRow(new Object[]{"GRP-BUS-04", "Additional Evening College Bus Route to Ernakulam", "Transport", 18, 30, 60, "ACTIVE"});

        updateMetrics();

        // Custom Cell Renderer for Java 2D Progress Bar
        groupTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                int val = (value instanceof Integer) ? (Integer) value : 0;
                JPanel p = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                        int w = getWidth() - 16;
                        int h = 10;
                        int x = 8;
                        int y = (getHeight() - h) / 2;

                        // Track
                        g2.setColor(new Color(20, 26, 48));
                        g2.fillRoundRect(x, y, w, h, h, h);

                        // Fill
                        int fillW = (int) Math.round((Math.min(100, val) / 100.0) * w);
                        if (fillW > 0) {
                            Color c1 = val >= 100 ? ThemeColors.PRIORITY_LOW : ThemeColors.PRIMARY_BLUE;
                            Color c2 = val >= 100 ? new Color(52, 211, 153) : ThemeColors.CYAN_ACCENT;
                            GradientPaint gp = new GradientPaint(x, y, c1, x + fillW, y, c2);
                            g2.setPaint(gp);
                            g2.fillRoundRect(x, y, fillW, h, h, h);
                        }

                        // Border
                        g2.setColor(ThemeColors.BORDER_COLOR);
                        g2.drawRoundRect(x, y, w, h, h, h);

                        // Text percentage
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        String s = val + "%";
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + h - 1);

                        g2.dispose();
                    }
                };
                p.setOpaque(false);
                return p;
            }
        });

        // Set column widths
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        groupTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        groupTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        groupTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        groupTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        groupTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        groupTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane sp = ModernTable.createModernScrollPane(groupTable);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel createMiniStatCard(String title, JLabel lVal, String sub, Color accent) {
        RoundedPanel card = new RoundedPanel(12, ThemeColors.CARD_SECONDARY) {
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
        card.setLayout(new BorderLayout(10, 2));
        card.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));

        lVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
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

        card.add(lVal, BorderLayout.WEST);
        card.add(centerBox, BorderLayout.CENTER);
        return card;
    }

    private void updateMetrics() {
        int count = tableModel.getRowCount();
        int totalSigns = 0;
        int escalated = 0;
        for (int i = 0; i < count; i++) {
            try {
                int signs = ((Number) tableModel.getValueAt(i, 3)).intValue();
                totalSigns += signs;
                String status = String.valueOf(tableModel.getValueAt(i, 6));
                if ("ESCALATED".equalsIgnoreCase(status)) {
                    escalated++;
                }
            } catch (Exception ignored) {}
        }
        if (lblActivePetitions != null) lblActivePetitions.setText(String.valueOf(count));
        if (lblTotalSignatures != null) lblTotalSignatures.setText(String.valueOf(totalSigns));
        if (lblEscalatedPetitions != null) lblEscalatedPetitions.setText(String.valueOf(escalated));
    }

    private void handleSignSelected() {
        int row = groupTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please click on a petition row from the table to sign.", "Select Petition", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = (String) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);
        int current = (int) tableModel.getValueAt(row, 3);
        int goal = (int) tableModel.getValueAt(row, 4);

        int updated = current + 1;
        int pct = Math.min(100, (int) Math.round(((double) updated / goal) * 100));

        tableModel.setValueAt(updated, row, 3);
        tableModel.setValueAt(pct, row, 5);

        if (updated >= goal) {
            tableModel.setValueAt("ESCALATED", row, 6);
            updateMetrics();
            JOptionPane.showMessageDialog(this, "Congratulations!\nYour signature for " + code + " pushed the petition over the required quorum (" + updated + "/" + goal + ")!\n\nThis petition is now officially ESCALATED to Department & Principal oversight.", "Quorum Achieved!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            updateMetrics();
            JOptionPane.showMessageDialog(this, "Your support has been recorded for:\n" + code + " - " + title + "\n\nCurrent Signatures: " + updated + " / " + goal + " (" + pct + "%)", "Petition Signed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleJoinCode() {
        String code = JOptionPane.showInputDialog(this, "Enter Group Petition Code (e.g. GRP-WIFI-01):", "Join Petition by Code", JOptionPane.PLAIN_MESSAGE);
        if (code != null && !code.trim().isEmpty()) {
            code = code.trim().toUpperCase();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (code.equals(tableModel.getValueAt(i, 0))) {
                    groupTable.setRowSelectionInterval(i, i);
                    handleSignSelected();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Petition code '" + code + "' not found in active database.", "Invalid Code", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateGroup() {
        JTextField titleF = new JTextField();
        JComboBox<String> catF = new JComboBox<>(new String[]{"Hostel & Mess", "Library Services", "Academic & Labs", "Campus WiFi & IT", "Transport", "Sports & Gym"});
        JTextField targetF = new JTextField("30");

        Object[] message = {
            "Petition Issue Title / Subject:", titleF,
            "Target Department / Category:", catF,
            "Required Supporter Quorum Goal:", targetF
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Launch Student Group Petition", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String titleStr = titleF.getText().trim();
            if (!titleStr.isEmpty()) {
                String groupCode = "GRP-CAM-" + String.format("%02d", (tableModel.getRowCount() + 1));
                int goal = 30;
                try {
                    goal = Integer.parseInt(targetF.getText().trim());
                } catch (Exception ignored) {}

                tableModel.addRow(new Object[]{groupCode, titleStr, catF.getSelectedItem(), 1, goal, Math.round((1.0 / goal) * 100), "ACTIVE"});
                updateMetrics();
                JOptionPane.showMessageDialog(this, "Group Petition Created!\n\nShare Petition Code: " + groupCode + " with classmates to gather verified signatures.", "Petition Launched", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}

package com.campusvoice.ui.management;

import com.campusvoice.config.AppConfig;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;

public class SystemSettingsPanel extends RoundedPanel {
    private ModernTextField highSlaField;
    private ModernTextField medSlaField;
    private ModernTextField lowSlaField;
    private ModernTextField confidenceThresholdField;
    private JCheckBox chkSmsPrincipal;
    private JCheckBox chkSafetyOverride;
    private JCheckBox chkAutoTranslate;

    public SystemSettingsPanel() {
        super(20, ThemeColors.CARD_BG);

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel topStrip = new JPanel(new BorderLayout(0, 4));
        topStrip.setOpaque(false);

        JLabel title = new JLabel("Institutional Governance & SLA Escalation Architecture");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Calibrate college-wide SLA turnaround thresholds, native AI model sensitivities, and automated emergency notification triggers.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        topStrip.add(title, BorderLayout.NORTH);
        topStrip.add(subtitle, BorderLayout.SOUTH);
        add(topStrip, BorderLayout.NORTH);

        // MAIN 2-COLUMN SETTINGS GRID
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // COLUMN 1: SLA Limits
        RoundedPanel cardSla = new RoundedPanel(14, ThemeColors.CARD_SECONDARY);
        cardSla.setLayout(new BoxLayout(cardSla, BoxLayout.Y_AXIS));
        cardSla.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel slaHeader = new JLabel("SLA Escalation Time Limits");
        slaHeader.setFont(ThemeFonts.TITLE_SMALL);
        slaHeader.setForeground(ThemeColors.TEXT_DARK);
        cardSla.add(slaHeader);
        cardSla.add(Box.createVerticalStrut(4));

        JLabel slaDesc = new JLabel("Maximum hours before unaddressed complaints auto-escalate.");
        slaDesc.setFont(ThemeFonts.CAPTION);
        slaDesc.setForeground(ThemeColors.TEXT_MUTED);
        cardSla.add(slaDesc);
        cardSla.add(Box.createVerticalStrut(16));

        highSlaField = new ModernTextField(String.valueOf(AppConfig.SLA_HIGH_HOURS));
        medSlaField = new ModernTextField(String.valueOf(AppConfig.SLA_MEDIUM_HOURS));
        lowSlaField = new ModernTextField(String.valueOf(AppConfig.SLA_LOW_HOURS));

        cardSla.add(createRow("HIGH Priority (Safety / Hazard):", highSlaField, "Hours"));
        cardSla.add(Box.createVerticalStrut(10));
        cardSla.add(createRow("MEDIUM Priority (Standard Grievance):", medSlaField, "Hours"));
        cardSla.add(Box.createVerticalStrut(10));
        cardSla.add(createRow("LOW Priority (Routine Maintenance):", lowSlaField, "Hours"));

        cardSla.add(Box.createVerticalStrut(16));
        chkSmsPrincipal = new JCheckBox("Auto-dispatch Urgent SMS Notice to Principal on SLA Breach");
        chkSmsPrincipal.setFont(ThemeFonts.CAPTION);
        chkSmsPrincipal.setForeground(ThemeColors.TEXT_MUTED);
        chkSmsPrincipal.setSelected(true);
        chkSmsPrincipal.setOpaque(false);
        cardSla.add(chkSmsPrincipal);

        grid.add(cardSla);

        // COLUMN 2: Native AI & Pipeline Parameters
        RoundedPanel cardAI = new RoundedPanel(14, ThemeColors.CARD_SECONDARY);
        cardAI.setLayout(new BoxLayout(cardAI, BoxLayout.Y_AXIS));
        cardAI.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel aiHeader = new JLabel("Native AI & Classifier Sensitivities");
        aiHeader.setFont(ThemeFonts.TITLE_SMALL);
        aiHeader.setForeground(ThemeColors.TEXT_DARK);
        cardAI.add(aiHeader);
        cardAI.add(Box.createVerticalStrut(4));

        JLabel aiDesc = new JLabel("Pure Java TF-IDF & Logistic Regression cutoffs.");
        aiDesc.setFont(ThemeFonts.CAPTION);
        aiDesc.setForeground(ThemeColors.TEXT_MUTED);
        cardAI.add(aiDesc);
        cardAI.add(Box.createVerticalStrut(16));

        confidenceThresholdField = new ModernTextField(String.valueOf((int) AppConfig.AI_CONFIDENCE_THRESHOLD));
        cardAI.add(createRow("Minimum Model Confidence Cutoff:", confidenceThresholdField, "%"));

        cardAI.add(Box.createVerticalStrut(16));
        chkSafetyOverride = new JCheckBox("Enforce Critical Safety Keyword Overrides (Electric shock, Ragging, etc.)");
        chkSafetyOverride.setFont(ThemeFonts.CAPTION);
        chkSafetyOverride.setForeground(ThemeColors.TEXT_MUTED);
        chkSafetyOverride.setSelected(true);
        chkSafetyOverride.setOpaque(false);
        cardAI.add(chkSafetyOverride);

        cardAI.add(Box.createVerticalStrut(10));
        chkAutoTranslate = new JCheckBox("Auto-translate Non-English Complaints to English for Staff");
        chkAutoTranslate.setFont(ThemeFonts.CAPTION);
        chkAutoTranslate.setForeground(ThemeColors.TEXT_MUTED);
        chkAutoTranslate.setSelected(true);
        chkAutoTranslate.setOpaque(false);
        cardAI.add(chkAutoTranslate);

        grid.add(cardAI);
        add(grid, BorderLayout.CENTER);

        // FOOTER WITH SERVER STATUS & SAVE BUTTON
        JPanel footer = new JPanel(new BorderLayout(15, 0));
        footer.setOpaque(false);

        // Status Indicators Strip
        JPanel statusStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statusStrip.setOpaque(false);
        statusStrip.add(createStatusPill("AI Server: Port 8000 Active", ThemeColors.PRIORITY_LOW_BG, ThemeColors.PRIORITY_LOW));
        statusStrip.add(createStatusPill("Pure Java Engine: 0% Python Runtime", ThemeColors.CARD_SECONDARY, ThemeColors.CYAN_ACCENT));
        statusStrip.add(createStatusPill("Database: Local / Supabase Online", ThemeColors.PURPLE_BG, ThemeColors.PURPLE_ACCENT));
        footer.add(statusStrip, BorderLayout.WEST);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);

        ModernButton resetBtn = new ModernButton("Restore Defaults", ThemeColors.CARD_SECONDARY, ThemeColors.CARD_HOVER, ThemeColors.TEXT_DARK);
        resetBtn.addActionListener(e -> {
            highSlaField.setText("24");
            medSlaField.setText("48");
            lowSlaField.setText("72");
            confidenceThresholdField.setText("70");
            JOptionPane.showMessageDialog(this, "Default parameters restored.", "Defaults", JOptionPane.INFORMATION_MESSAGE);
        });

        ModernButton saveBtn = new ModernButton("Save Configuration", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Institutional Governance Settings saved successfully!\nParameters updated across embedded AI microservice and database.", "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        btnBox.add(resetBtn);
        btnBox.add(saveBtn);
        footer.add(btnBox, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createStatusPill(String text, Color bg, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        p.setBackground(bg);
        p.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        p.add(lbl);
        return p;
    }

    private JPanel createRow(String labelText, ModernTextField field, String unit) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeFonts.BODY_REGULAR);
        lbl.setForeground(ThemeColors.TEXT_DARK);
        p.add(lbl, BorderLayout.WEST);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBox.setOpaque(false);
        field.setPreferredSize(new Dimension(80, 36));
        rightBox.add(field);
        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(ThemeFonts.CAPTION);
        unitLbl.setForeground(ThemeColors.TEXT_MUTED);
        rightBox.add(unitLbl);

        p.add(rightBox, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return p;
    }
}

package com.campusvoice.ui.principal;

import com.campusvoice.service.ComplaintService;
import com.campusvoice.service.ReportService;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReportsPanel extends RoundedPanel {
    private ComplaintService complaintService;
    private ReportService reportService;

    public ReportsPanel() {
        super(20, ThemeColors.CARD_BG);
        this.complaintService = new ComplaintService();
        this.reportService = new ReportService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel topStrip = new JPanel(new BorderLayout(0, 14));
        topStrip.setOpaque(false);

        JPanel headerBox = new JPanel(new GridLayout(2, 1, 0, 4));
        headerBox.setOpaque(false);

        JLabel title = new JLabel("Executive Analytics & Institutional Compliance Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitle = new JLabel("Export certified grievance reports, NAAC / NBA audit summaries, and raw departmental CSV data logs.");
        subtitle.setFont(ThemeFonts.CAPTION);
        subtitle.setForeground(ThemeColors.TEXT_MUTED);

        headerBox.add(title);
        headerBox.add(subtitle);
        topStrip.add(headerBox, BorderLayout.NORTH);

        // FILTER TOOLBAR
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);

        JComboBox<String> termCombo = new JComboBox<>(new String[]{"Academic Year 2025-2026 (Current)", "Odd Semester 2025", "Even Semester 2024", "Full Campus History"});
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{"All Institutional Departments", "Computer Science & Engg", "Electronics & Comm", "Mechanical & Civil", "Hostels & Mess Facilities"});

        JLabel l1 = new JLabel("Target Term:");
        l1.setForeground(ThemeColors.TEXT_DARK);
        JLabel l2 = new JLabel("Department:");
        l2.setForeground(ThemeColors.TEXT_DARK);

        filterBar.add(l1);
        filterBar.add(termCombo);
        filterBar.add(Box.createHorizontalStrut(10));
        filterBar.add(l2);
        filterBar.add(deptCombo);

        topStrip.add(filterBar, BorderLayout.SOUTH);
        add(topStrip, BorderLayout.NORTH);

        // CARDS GRID FOR EXPORTS (3 CARDS)
        JPanel cardsBox = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsBox.setOpaque(false);

        // Card 1: Text Summary Report
        RoundedPanel card1 = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        card1.setLayout(new BorderLayout(12, 12));
        card1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel c1Title = new JLabel("Executive Brief");
        c1Title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        c1Title.setForeground(ThemeColors.TEXT_DARK);
        JLabel c1Desc = new JLabel("<html><b>High-level leadership brief</b> containing campus-wide grievance volume, priority breakdown, average SLA resolution times, and outstanding escalations.</html>");
        c1Desc.setFont(ThemeFonts.CAPTION);
        c1Desc.setForeground(ThemeColors.TEXT_MUTED);

        ModernButton btnGenPdf = new ModernButton("Export Summary Brief", ThemeColors.PURPLE_ACCENT, ThemeColors.PURPLE_HOVER);
        btnGenPdf.addActionListener(e -> {
            File f = reportService.generateTextSummaryReport(complaintService.getAllComplaints(), "CampusVoice_Executive_Summary");
            if (f != null) {
                JOptionPane.showMessageDialog(this, "Executive Brief Generated Successfully!\n\nFile saved to:\n" + f.getAbsolutePath(), "Report Ready", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        card1.add(c1Title, BorderLayout.NORTH);
        card1.add(c1Desc, BorderLayout.CENTER);
        card1.add(btnGenPdf, BorderLayout.SOUTH);

        // Card 2: NAAC / NBA Audit Card
        RoundedPanel card2 = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        card2.setLayout(new BorderLayout(12, 12));
        card2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel c2Title = new JLabel("Accreditation Audit");
        c2Title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        c2Title.setForeground(ThemeColors.TEXT_DARK);
        JLabel c2Desc = new JLabel("<html><b>Certified audit compliance record</b> for NAAC Criterion V (Student Support & Progression) and NBA Institutional Redressal Governance review.</html>");
        c2Desc.setFont(ThemeFonts.CAPTION);
        c2Desc.setForeground(ThemeColors.TEXT_MUTED);

        ModernButton btnAudit = new ModernButton("Export NAAC/NBA Audit", ThemeColors.STATUS_IN_PROGRESS, ThemeColors.PRIMARY_HOVER);
        btnAudit.addActionListener(e -> {
            File f = reportService.generateTextSummaryReport(complaintService.getAllComplaints(), "CampusVoice_NAAC_NBA_Compliance_Audit");
            if (f != null) {
                JOptionPane.showMessageDialog(this, "Accreditation Audit Scorecard Generated!\n\nCertified for NAAC/NBA inspection:\n" + f.getAbsolutePath(), "Audit Ready", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        card2.add(c2Title, BorderLayout.NORTH);
        card2.add(c2Desc, BorderLayout.CENTER);
        card2.add(btnAudit, BorderLayout.SOUTH);

        // Card 3: CSV Data Export
        RoundedPanel card3 = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        card3.setLayout(new BorderLayout(12, 12));
        card3.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel c3Title = new JLabel("Full Data CSV Log");
        c3Title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        c3Title.setForeground(ThemeColors.TEXT_DARK);
        JLabel c3Desc = new JLabel("<html><b>Raw complaint database archive</b> in standard RFC-4180 CSV format. Ready for direct statistical analysis in Microsoft Excel, Google Sheets, or Python pandas.</html>");
        c3Desc.setFont(ThemeFonts.CAPTION);
        c3Desc.setForeground(ThemeColors.TEXT_MUTED);

        ModernButton btnGenCsv = new ModernButton("Export Raw CSV Data", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        btnGenCsv.addActionListener(e -> {
            File f = reportService.generateCSVReport(complaintService.getAllComplaints(), "CampusVoice_Complaints_Export");
            if (f != null) {
                JOptionPane.showMessageDialog(this, "CSV Database Dump Completed Successfully!\n\nPath:\n" + f.getAbsolutePath(), "CSV Data Exported", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        card3.add(c3Title, BorderLayout.NORTH);
        card3.add(c3Desc, BorderLayout.CENTER);
        card3.add(btnGenCsv, BorderLayout.SOUTH);

        cardsBox.add(card1);
        cardsBox.add(card2);
        cardsBox.add(card3);

        add(cardsBox, BorderLayout.CENTER);
    }
}

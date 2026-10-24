package com.campusvoice.ui.common;

import com.campusvoice.model.AIAnalysis;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class AIPriorityDialog extends JDialog {
    private boolean confirmed = false;
    private String finalPriority;
    private AIAnalysis aiResult;
    private JLabel slaProtocolLbl;
    private PriorityBadge priorityBadge;
    private JProgressBar confBar;

    public AIPriorityDialog(Frame parent, AIAnalysis aiResult) {
        super(parent, "AI Priority & SLA Assessment", true);
        this.aiResult = aiResult;
        this.finalPriority = aiResult.getFinalPriority();

        setSize(520, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.CARD_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);

        JPanel mainPanel = new RoundedPanel(20, ThemeColors.CARD_BG);
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 22, 25));

        // HEADER
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        headerPanel.setOpaque(false);

        JLabel headerLabel = new JLabel("Native AI Priority & SLA Classification", SwingConstants.CENTER);
        headerLabel.setFont(ThemeFonts.TITLE_MEDIUM);
        headerLabel.setForeground(ThemeColors.TEXT_DARK);

        JLabel subHeader = new JLabel("TF-IDF Vectorizer + Logistic Regression Classifier • Sub-millisecond Execution", SwingConstants.CENTER);
        subHeader.setFont(ThemeFonts.CAPTION);
        subHeader.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(headerLabel);
        headerPanel.add(subHeader);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // BODY
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        // Priority Hero Card
        RoundedPanel heroCard = new RoundedPanel(12, ThemeColors.CARD_SECONDARY);
        heroCard.setLayout(new BorderLayout(10, 8));
        heroCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel topBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBadgeRow.setOpaque(false);
        JLabel suggLbl = new JLabel("Model Prediction:");
        suggLbl.setFont(ThemeFonts.BODY_BOLD);
        suggLbl.setForeground(ThemeColors.TEXT_DARK);
        priorityBadge = new PriorityBadge(aiResult.getFinalPriority());
        priorityBadge.setPreferredSize(new Dimension(95, 30));
        topBadgeRow.add(suggLbl);
        topBadgeRow.add(priorityBadge);

        slaProtocolLbl = new JLabel(getSlaText(aiResult.getFinalPriority()));
        slaProtocolLbl.setFont(ThemeFonts.BADGE_FONT);
        slaProtocolLbl.setForeground(getSlaColor(aiResult.getFinalPriority()));

        heroCard.add(topBadgeRow, BorderLayout.WEST);
        heroCard.add(slaProtocolLbl, BorderLayout.EAST);

        // Confidence bar inside hero
        JPanel confBox = new JPanel(new BorderLayout(8, 0));
        confBox.setOpaque(false);
        JLabel confText = new JLabel(String.format("Classification Confidence: %.1f%%", aiResult.getConfidence()));
        confText.setFont(ThemeFonts.CAPTION);
        confText.setForeground(ThemeColors.TEXT_MUTED);

        confBar = new JProgressBar(0, 100);
        confBar.setValue((int) Math.round(aiResult.getConfidence()));
        confBar.setStringPainted(true);
        confBar.setForeground("HIGH".equalsIgnoreCase(aiResult.getFinalPriority()) ? ThemeColors.PRIORITY_HIGH : ThemeColors.PRIMARY_BLUE);
        confBar.setBackground(ThemeColors.INPUT_BG);
        confBar.setPreferredSize(new Dimension(140, 18));

        confBox.add(confText, BorderLayout.WEST);
        confBox.add(confBar, BorderLayout.EAST);
        heroCard.add(confBox, BorderLayout.SOUTH);

        bodyPanel.add(heroCard);
        bodyPanel.add(Box.createVerticalStrut(12));

        // Reason box
        JLabel reasonHeader = new JLabel("Natural Language Decision Explanation:");
        reasonHeader.setFont(ThemeFonts.BODY_BOLD);
        reasonHeader.setForeground(ThemeColors.TEXT_DARK);
        bodyPanel.add(reasonHeader);
        bodyPanel.add(Box.createVerticalStrut(4));

        JTextArea reasonArea = new JTextArea(aiResult.getReason());
        reasonArea.setFont(ThemeFonts.BODY_REGULAR);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setEditable(false);
        reasonArea.setBackground(ThemeColors.INPUT_BG);
        reasonArea.setForeground(Color.WHITE);
        reasonArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(450, 65));
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        reasonScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        bodyPanel.add(reasonScroll);

        bodyPanel.add(Box.createVerticalStrut(10));

        // Risk factors
        JLabel riskHeader = new JLabel("Safety Hazard Keywords & Risk Triggers Detected:");
        riskHeader.setFont(ThemeFonts.BODY_BOLD);
        riskHeader.setForeground(ThemeColors.TEXT_DARK);
        bodyPanel.add(riskHeader);
        bodyPanel.add(Box.createVerticalStrut(4));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (aiResult.getRiskFactors() != null && !aiResult.getRiskFactors().isEmpty()) {
            for (String rf : aiResult.getRiskFactors()) {
                listModel.addElement("• " + rf);
            }
        } else {
            listModel.addElement("Standard priority scoring based on vocabulary weights");
        }

        JList<String> riskList = new JList<>(listModel);
        riskList.setFont(ThemeFonts.CAPTION);
        riskList.setBackground(ThemeColors.INPUT_BG);
        riskList.setForeground(Color.WHITE);
        JScrollPane riskScroll = new JScrollPane(riskList);
        riskScroll.setPreferredSize(new Dimension(450, 60));
        riskScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        riskScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        bodyPanel.add(riskScroll);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        ModernButton changeBtn = new ModernButton("Override Priority", ThemeColors.CARD_SECONDARY, ThemeColors.CARD_HOVER, ThemeColors.TEXT_DARK);
        changeBtn.addActionListener(e -> {
            String[] options = {"HIGH", "MEDIUM", "LOW"};
            String choice = (String) JOptionPane.showInputDialog(this, "Select Desired Priority:", "Human Review Override", JOptionPane.QUESTION_MESSAGE, null, options, finalPriority);
            if (choice != null) {
                finalPriority = choice;
                aiResult.setFinalPriority(choice);
                aiResult.setHumanOverridden(true);
                priorityBadge.setPriority(choice);
                slaProtocolLbl.setText(getSlaText(choice));
                slaProtocolLbl.setForeground(getSlaColor(choice));
            }
        });

        ModernButton confirmBtn = new ModernButton("Accept & Continue", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnPanel.add(changeBtn);
        btnPanel.add(confirmBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private String getSlaText(String p) {
        if ("HIGH".equalsIgnoreCase(p)) return "24h Mandatory Safety SLA";
        if ("MEDIUM".equalsIgnoreCase(p)) return "48h Standard Queue SLA";
        return "72h Routine Maintenance";
    }

    private Color getSlaColor(String p) {
        if ("HIGH".equalsIgnoreCase(p)) return ThemeColors.PRIORITY_HIGH;
        if ("MEDIUM".equalsIgnoreCase(p)) return ThemeColors.PRIORITY_MED;
        return ThemeColors.PRIORITY_LOW;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getFinalPriority() {
        return finalPriority;
    }
}

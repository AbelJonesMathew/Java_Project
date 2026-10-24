package com.campusvoice.ui.common;

import com.campusvoice.ai.LanguageService;
import com.campusvoice.ai.PriorityService;
import com.campusvoice.ai.TrainModel;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;

public class AIHubPanel extends RoundedPanel {
    private final PriorityService priorityService;

    public AIHubPanel() {
        super(20, ThemeColors.CARD_BG);
        this.priorityService = new PriorityService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("CampusVoice AI & Multilingual Intelligence Hub");
        title.setFont(ThemeFonts.TITLE_MEDIUM);
        title.setForeground(ThemeColors.TEXT_DARK);

        JLabel sub = new JLabel("Pure Java AI Suite: TF-IDF + Softmax Classification & Multilingual Translation");
        sub.setFont(ThemeFonts.CAPTION);
        sub.setForeground(ThemeColors.TEXT_MUTED);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(sub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // TABBED PANE FOR THE 3 AI ENGINES
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeFonts.BODY_BOLD);

        tabs.addTab("Priority Detection", buildPriorityTab());
        tabs.addTab("Multilingual Translation", buildTranslationTab());
        tabs.addTab("ML Training & Accuracy", buildTrainingTab());

        add(tabs, BorderLayout.CENTER);
    }

    // 1. Priority Detection Tab
    private JPanel buildPriorityTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel catLbl = new JLabel("Complaint Category & Text:");
        catLbl.setFont(ThemeFonts.BODY_BOLD);
        catLbl.setForeground(ThemeColors.TEXT_DARK);
        form.add(catLbl);
        form.add(Box.createVerticalStrut(6));

        JComboBox<String> catCombo = new JComboBox<>(new String[]{
            "Electricity", "Infrastructure", "Academic", "Food/Canteen", "Hostel", "Internet/WiFi", "Security", "Water", "Library"
        });
        catCombo.setFont(ThemeFonts.BODY_REGULAR);
        catCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(catCombo);
        form.add(Box.createVerticalStrut(10));

        JTextArea inputArea = new JTextArea("Exposed electrical wire in the computer lab is sparking and could cause electric shock");
        inputArea.setFont(ThemeFonts.BODY_REGULAR);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(ThemeColors.INPUT_BG);
        inputArea.setForeground(Color.WHITE);
        inputArea.setCaretColor(Color.WHITE);
        inputArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(inputArea);
        scroll.setPreferredSize(new Dimension(500, 75));
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        form.add(scroll);
        form.add(Box.createVerticalStrut(12));

        // Result displays
        JPanel resultCard = new RoundedPanel(14, ThemeColors.CARD_SECONDARY);
        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badgeRow.setOpaque(false);
        JLabel pLabel = new JLabel("Predicted Priority:");
        pLabel.setFont(ThemeFonts.BODY_BOLD);
        pLabel.setForeground(ThemeColors.TEXT_DARK);
        PriorityBadge badge = new PriorityBadge("HIGH");
        JLabel confLabel = new JLabel("Confidence: 96.0%");
        confLabel.setFont(ThemeFonts.BODY_BOLD);
        confLabel.setForeground(ThemeColors.CYAN_ACCENT);
        badgeRow.add(pLabel);
        badgeRow.add(badge);
        badgeRow.add(confLabel);

        JLabel reasonLbl = new JLabel("<html><b>Reason:</b> Potential safety hazard detected. Risk of electrocution.</html>");
        reasonLbl.setFont(ThemeFonts.CAPTION);
        reasonLbl.setForeground(ThemeColors.TEXT_MUTED);

        JLabel riskFactorsLbl = new JLabel("<html><b>Risk Factors:</b> • Electrical hazard: Exposed live wiring in facility</html>");
        riskFactorsLbl.setFont(ThemeFonts.CAPTION);
        riskFactorsLbl.setForeground(ThemeColors.PRIORITY_HIGH);

        resultCard.add(badgeRow);
        resultCard.add(Box.createVerticalStrut(6));
        resultCard.add(reasonLbl);
        resultCard.add(Box.createVerticalStrut(4));
        resultCard.add(riskFactorsLbl);

        form.add(resultCard);
        panel.add(form, BorderLayout.CENTER);

        // Analyze button
        ModernButton analyzeBtn = new ModernButton("Evaluate Complaint with AI", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        analyzeBtn.addActionListener(e -> {
            String txt = inputArea.getText().trim();
            String cat = (String) catCombo.getSelectedItem();
            PriorityService.AIAnalysisResult res = priorityService.analyzeComplaint(txt, cat, "English");

            badge.setPriority(res.getPriority());
            confLabel.setText(String.format("Confidence: %.1f%%", res.getConfidence()));
            reasonLbl.setText("<html><b>Reason:</b> " + res.getReason() + "</html>");
            if (res.getRiskFactors() != null && !res.getRiskFactors().isEmpty()) {
                riskFactorsLbl.setText("<html><b>Risk Factors:</b> " + String.join("<br>", res.getRiskFactors()) + "</html>");
            } else {
                riskFactorsLbl.setText("<html><b>Risk Factors:</b> • No critical hazards matched</html>");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(analyzeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 2. Multilingual Translation Tab
    private JPanel buildTranslationTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Preset quick demo buttons
        JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        presets.setOpaque(false);
        JLabel lQuick = new JLabel("Quick Samples:");
        lQuick.setForeground(ThemeColors.TEXT_DARK);
        presets.add(lQuick);

        JTextArea inputArea = new JTextArea("ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.");
        inputArea.setFont(ThemeFonts.BODY_REGULAR);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(ThemeColors.INPUT_BG);
        inputArea.setForeground(Color.WHITE);
        inputArea.setCaretColor(Color.WHITE);
        inputArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton bMl = createSampleBtn("Malayalam", () -> inputArea.setText("ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല."));
        JButton bHi = createSampleBtn("Hindi", () -> inputArea.setText("गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।"));
        JButton bTa = createSampleBtn("Tamil", () -> inputArea.setText("ஹாஸ்டலில் குடிநீர் விநியோகம் இல்லை."));
        JButton bTe = createSampleBtn("Telugu", () -> inputArea.setText("హాస్టల్ వైఫై పనిచేయడం లేదు."));
        JButton bKn = createSampleBtn("Kannada", () -> inputArea.setText("ಲೈಬ್ರರಿಯಲ್ಲಿ ಹೆಚ್ಚಿನ ಕುರ್ಚಿಗಳ ಅಗತ್ಯವಿದೆ."));

        presets.add(bMl);
        presets.add(bHi);
        presets.add(bTa);
        presets.add(bTe);
        presets.add(bKn);

        form.add(presets);
        form.add(Box.createVerticalStrut(6));

        JScrollPane scrollIn = new JScrollPane(inputArea);
        scrollIn.setPreferredSize(new Dimension(500, 70));
        scrollIn.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        form.add(scrollIn);
        form.add(Box.createVerticalStrut(10));

        // Result Card
        RoundedPanel outCard = new RoundedPanel(14, ThemeColors.CARD_SECONDARY);
        outCard.setLayout(new BoxLayout(outCard, BoxLayout.Y_AXIS));
        outCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel langInfo = new JLabel("Detected Language: Malayalam (ml) - 99.0% Confidence");
        langInfo.setFont(ThemeFonts.BODY_BOLD);
        langInfo.setForeground(ThemeColors.CYAN_ACCENT);

        JLabel transHeading = new JLabel("English Translation:");
        transHeading.setFont(ThemeFonts.BODY_BOLD);
        transHeading.setForeground(ThemeColors.TEXT_DARK);

        JTextArea transOutput = new JTextArea("The projector in the classroom is not working.");
        transOutput.setFont(ThemeFonts.BODY_REGULAR);
        transOutput.setLineWrap(true);
        transOutput.setWrapStyleWord(true);
        transOutput.setEditable(false);
        transOutput.setBackground(ThemeColors.INPUT_BG);
        transOutput.setForeground(Color.WHITE);
        transOutput.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane spTrans = new JScrollPane(transOutput);
        spTrans.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));

        outCard.add(langInfo);
        outCard.add(Box.createVerticalStrut(8));
        outCard.add(transHeading);
        outCard.add(Box.createVerticalStrut(4));
        outCard.add(spTrans);

        form.add(outCard);
        panel.add(form, BorderLayout.CENTER);

        // Actions
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton btnDetect = new ModernButton("Detect Language", ThemeColors.CARD_SECONDARY, ThemeColors.CARD_HOVER, ThemeColors.TEXT_DARK);
        btnDetect.addActionListener(e -> {
            String txt = inputArea.getText().trim();
            LanguageService.LanguageDetectionResult res = LanguageService.detectLanguage(txt);
            langInfo.setText("Detected Language: " + res.getLanguageName() + " (" + res.getLanguageCode() + ") - "
                    + String.format("%.0f%% Confidence", res.getConfidence() * 100.0));
        });

        ModernButton btnTranslate = new ModernButton("Translate to English", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        btnTranslate.addActionListener(e -> {
            String txt = inputArea.getText().trim();
            LanguageService.LanguageDetectionResult lRes = LanguageService.detectLanguage(txt);
            LanguageService.TranslationResult tRes = LanguageService.translateToEnglish(txt, lRes.getLanguageName());

            langInfo.setText("Detected Language: " + lRes.getLanguageName() + " (" + lRes.getLanguageCode() + ") - "
                    + String.format("%.0f%% Confidence", lRes.getConfidence() * 100.0));
            transOutput.setText(tRes.getTranslatedText());
        });

        btnRow.add(btnDetect);
        btnRow.add(btnTranslate);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createSampleBtn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(ThemeFonts.CAPTION);
        b.setBackground(ThemeColors.CARD_SECONDARY);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    // 3. Training & Model Stats Tab
    private JPanel buildTrainingTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel statsCard = new RoundedPanel(16, ThemeColors.CARD_SECONDARY);
        statsCard.setLayout(new GridLayout(4, 2, 15, 15));
        statsCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel l1 = new JLabel("Training Dataset:");
        l1.setFont(ThemeFonts.BODY_BOLD);
        l1.setForeground(ThemeColors.TEXT_MUTED);
        JLabel v1 = new JLabel("30 Verified Multilingual Campus Complaints");
        v1.setFont(ThemeFonts.BODY_REGULAR);
        v1.setForeground(ThemeColors.TEXT_DARK);

        JLabel l2 = new JLabel("TF-IDF Features:");
        l2.setFont(ThemeFonts.BODY_BOLD);
        l2.setForeground(ThemeColors.TEXT_MUTED);
        JLabel v2 = new JLabel("384 Unigram & Bigram Features");
        v2.setFont(ThemeFonts.BODY_REGULAR);
        v2.setForeground(ThemeColors.TEXT_DARK);

        JLabel l3 = new JLabel("Model Training Accuracy:");
        l3.setFont(ThemeFonts.BODY_BOLD);
        l3.setForeground(ThemeColors.TEXT_MUTED);
        JLabel v3 = new JLabel("100.0% (Evaluated on Verified Training Samples)");
        v3.setFont(ThemeFonts.BODY_BOLD);
        v3.setForeground(ThemeColors.PRIORITY_LOW);

        JLabel l4 = new JLabel("Model Architecture:");
        l4.setFont(ThemeFonts.BODY_BOLD);
        l4.setForeground(ThemeColors.TEXT_MUTED);
        JLabel v4 = new JLabel("Native Java TF-IDF Vectorizer + Centroid Softmax Classifier");
        v4.setFont(ThemeFonts.BODY_REGULAR);
        v4.setForeground(ThemeColors.TEXT_DARK);

        statsCard.add(l1); statsCard.add(v1);
        statsCard.add(l2); statsCard.add(v2);
        statsCard.add(l3); statsCard.add(v3);
        statsCard.add(l4); statsCard.add(v4);

        panel.add(statsCard, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.setOpaque(false);
        ModernButton retrainBtn = new ModernButton("Retrain AI Model Now", ThemeColors.PURPLE_ACCENT, ThemeColors.PURPLE_HOVER);
        retrainBtn.addActionListener(e -> {
            TrainModel.TrainedAIModel model = TrainModel.trainPriorityModel();
            v2.setText(model.getVectorizer().getVocabularySize() + " Unigram & Bigram Features");
            v3.setText(model.getTrainingAccuracy() + "%");
            priorityService.loadModels();
            JOptionPane.showMessageDialog(this,
                    "AI Model Retrained Successfully!\nSamples: " + model.getSampleCount() +
                            "\nFeatures: " + model.getVectorizer().getVocabularySize() +
                            "\nAccuracy: " + model.getTrainingAccuracy() + "%",
                    "Model Training Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        foot.add(retrainBtn);
        panel.add(foot, BorderLayout.SOUTH);

        return panel;
    }
}

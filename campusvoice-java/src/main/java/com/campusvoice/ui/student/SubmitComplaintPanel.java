package com.campusvoice.ui.student;

import com.campusvoice.ai.PriorityService;
import com.campusvoice.dao.CategoryDAO;
import com.campusvoice.dao.impl.CategoryDAOImpl;
import com.campusvoice.model.Attachment;
import com.campusvoice.model.Category;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.service.ComplaintService;
import com.campusvoice.ui.common.AIPriorityDialog;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SubmitComplaintPanel extends RoundedPanel {
    private StudentDashboardFrame parentFrame;
    private ComplaintService complaintService;
    private PriorityService priorityService;
    private CategoryDAO categoryDAO;

    private ModernTextField titleField;
    private JComboBox<Category> categoryCombo;
    private JComboBox<String> languageCombo;
    private JTextArea descriptionArea;
    private JCheckBox anonymousCheckBox;
    private JLabel aiStatusLabel;
    private PriorityBadge priorityBadge;

    private String analyzedPriority = null;
    private double analyzedConfidence = 0.0;
    private String analyzedReason = "";
    private List<String> analyzedRiskFactors = null;
    private boolean isOverridden = false;

    // The evidence file chosen by the student (may be null)
    private File selectedEvidenceFile = null;

    public SubmitComplaintPanel(StudentDashboardFrame parent) {
        super(20, ThemeColors.CARD_BG);
        this.parentFrame = parent;
        this.complaintService = new ComplaintService();
        this.priorityService = new PriorityService();
        this.categoryDAO = new CategoryDAOImpl();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout(10, 4));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("Submit Official Grievance");
        titleLabel.setFont(ThemeFonts.TITLE_MEDIUM);
        titleLabel.setForeground(ThemeColors.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Evaluated instantly by Embedded AI • Auto-routed to CSE / Institutional Authority with SLA guarantees");
        subtitleLabel.setFont(ThemeFonts.CAPTION);
        subtitleLabel.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(subtitleLabel);
        headerPanel.add(titleBox, BorderLayout.CENTER);

        // Institutional Badge on top right
        RoundedPanel slaPill = new RoundedPanel(10, ThemeColors.CARD_SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                com.campusvoice.ui.theme.Java2DUtils.drawGlowingDot(g2, 12, getHeight() / 2, 3, ThemeColors.CYAN_ACCENT);
                g2.dispose();
            }
        };
        slaPill.setLayout(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        slaPill.setBorderColor(new Color(56, 189, 248, 80));
        JLabel slaLbl = new JLabel("24h SLA Safety Hazard Protocol");
        slaLbl.setFont(ThemeFonts.BADGE_FONT);
        slaLbl.setForeground(ThemeColors.CYAN_ACCENT);
        slaPill.add(slaLbl);
        headerPanel.add(slaPill, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // FORM CONTENT
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        // Complaint Title
        JLabel titleLbl = new JLabel("Complaint Subject / Title");
        titleLbl.setFont(ThemeFonts.BODY_BOLD);
        titleLbl.setForeground(ThemeColors.TEXT_DARK);
        formPanel.add(titleLbl);
        formPanel.add(Box.createVerticalStrut(4));

        titleField = new ModernTextField("Exposed Electrical Wires in Lab 3");
        titleField.setPlaceholder("e.g. Exposed electrical wires in Lab 3 or projector issue");
        titleField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        com.campusvoice.ui.theme.CssEngine.apply(titleField, "input-modern");
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        formPanel.add(titleField);
        formPanel.add(Box.createVerticalStrut(12));

        // Category & Language
        JPanel catLangPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        catLangPanel.setOpaque(false);
        catLangPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel catPanel = new JPanel(new BorderLayout(0, 4));
        catPanel.setOpaque(false);
        JLabel cLbl = new JLabel("Complaint Category");
        cLbl.setFont(ThemeFonts.BODY_BOLD);
        cLbl.setForeground(ThemeColors.TEXT_DARK);
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(ThemeFonts.BODY_REGULAR);
        categoryCombo.setPreferredSize(new Dimension(200, 38));
        com.campusvoice.ui.theme.CssEngine.apply(categoryCombo, "combo-modern");
        loadCategories();
        catPanel.add(cLbl, BorderLayout.NORTH);
        catPanel.add(categoryCombo, BorderLayout.CENTER);

        JPanel langPanel = new JPanel(new BorderLayout(0, 4));
        langPanel.setOpaque(false);
        JLabel lLbl = new JLabel("Original Language");
        lLbl.setFont(ThemeFonts.BODY_BOLD);
        lLbl.setForeground(ThemeColors.TEXT_DARK);
        languageCombo = new JComboBox<>(new String[]{"English", "Malayalam (മലയാളം)", "Hindi (हिन्दी)", "Tamil (தமிழ்)", "Telugu (తెలుగు)", "Kannada (ಕನ್ನಡ)"});
        languageCombo.setFont(ThemeFonts.BODY_REGULAR);
        languageCombo.setPreferredSize(new Dimension(200, 38));
        com.campusvoice.ui.theme.CssEngine.apply(languageCombo, "combo-modern");
        langPanel.add(lLbl, BorderLayout.NORTH);
        langPanel.add(languageCombo, BorderLayout.CENTER);

        catLangPanel.add(catPanel);
        catLangPanel.add(langPanel);

        formPanel.add(catLangPanel);
        formPanel.add(Box.createVerticalStrut(12));

        // Description
        JPanel descHeaderBox = new JPanel(new BorderLayout());
        descHeaderBox.setOpaque(false);
        descHeaderBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel descLbl = new JLabel("Detailed Complaint Description");
        descLbl.setFont(ThemeFonts.BODY_BOLD);
        descLbl.setForeground(ThemeColors.TEXT_DARK);
        JLabel descHint = new JLabel("Multilingual text supported • AI will analyze keywords");
        descHint.setFont(ThemeFonts.CAPTION);
        descHint.setForeground(ThemeColors.TEXT_MUTED);

        descHeaderBox.add(descLbl, BorderLayout.WEST);
        descHeaderBox.add(descHint, BorderLayout.EAST);

        formPanel.add(descHeaderBox);
        formPanel.add(Box.createVerticalStrut(4));

        descriptionArea = new JTextArea("The electrical wire in the lab is exposed and students could get an electric shock.");
        descriptionArea.setFont(ThemeFonts.BODY_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(ThemeColors.INPUT_BG);
        descriptionArea.setForeground(Color.WHITE);
        descriptionArea.setCaretColor(Color.WHITE);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descriptionArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Describe the problem in detail. Multilingual text supported...");

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(500, 110));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        descScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        descScroll.putClientProperty(FlatClientProperties.SCROLL_PANE_SMOOTH_SCROLLING, true);
        formPanel.add(descScroll);

        formPanel.add(Box.createVerticalStrut(10));

        // Attachment / Evidence Bar
        JPanel attachBar = new JPanel(new BorderLayout());
        attachBar.setOpaque(false);
        attachBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel attachLbl = new JLabel("• Evidence / Photo: None attached (Optional)");
        attachLbl.setFont(ThemeFonts.CAPTION);
        attachLbl.setForeground(ThemeColors.TEXT_MUTED);

        JButton attachBtn = new JButton("Add Attachment");
        attachBtn.setFont(ThemeFonts.CAPTION);
        attachBtn.setBackground(ThemeColors.CARD_SECONDARY);
        attachBtn.setForeground(Color.WHITE);
        attachBtn.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1));
        attachBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        attachBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Evidence File (Image, PDF, or Document)");
            int ret = chooser.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selectedEvidenceFile = chooser.getSelectedFile();
                attachLbl.setText("[Attached] Evidence: " + selectedEvidenceFile.getName());
                attachLbl.setForeground(ThemeColors.CYAN_ACCENT);
            }
        });

        attachBar.add(attachLbl, BorderLayout.WEST);
        attachBar.add(attachBtn, BorderLayout.EAST);
        formPanel.add(attachBar);

        formPanel.add(Box.createVerticalStrut(10));

        // Options: Anonymous check & AI Status
        JPanel optPanel = new JPanel(new BorderLayout());
        optPanel.setOpaque(false);
        optPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        anonymousCheckBox = new JCheckBox("Submit Anonymously (Hide Student Identity & Reg No.)");
        anonymousCheckBox.setFont(ThemeFonts.BODY_REGULAR);
        anonymousCheckBox.setForeground(ThemeColors.TEXT_MUTED);
        anonymousCheckBox.setOpaque(false);

        JPanel aiBadgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        aiBadgeBox.setOpaque(false);

        aiStatusLabel = new JLabel("Priority: Not Analyzed Yet");
        aiStatusLabel.setFont(ThemeFonts.CAPTION);
        aiStatusLabel.setForeground(ThemeColors.TEXT_MUTED);

        priorityBadge = new PriorityBadge("MEDIUM");
        priorityBadge.setVisible(false);

        aiBadgeBox.add(aiStatusLabel);
        aiBadgeBox.add(priorityBadge);

        optPanel.add(anonymousCheckBox, BorderLayout.WEST);
        optPanel.add(aiBadgeBox, BorderLayout.EAST);

        formPanel.add(optPanel);

        add(formPanel, BorderLayout.CENTER);

        // FOOTER BUTTONS
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        footerPanel.setOpaque(false);

        ModernButton analyzeBtn = new ModernButton("AI Pre-Assessment", ThemeColors.PURPLE_ACCENT, ThemeColors.PURPLE_HOVER);
        com.campusvoice.ui.theme.CssEngine.apply(analyzeBtn, "btn-glass");
        analyzeBtn.addActionListener(e -> runAIAnalysis());

        ModernButton submitBtn = new ModernButton("Submit Grievance", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        com.campusvoice.ui.theme.CssEngine.apply(submitBtn, "btn-primary");
        submitBtn.addActionListener(e -> handleSubmit());

        footerPanel.add(analyzeBtn);
        footerPanel.add(submitBtn);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        List<Category> list = categoryDAO.findAll();
        for (Category c : list) {
            categoryCombo.addItem(c);
        }
    }

    private void runAIAnalysis() {
        String desc = descriptionArea.getText().trim();
        Category cat = (Category) categoryCombo.getSelectedItem();
        String catName = cat != null ? cat.getCategoryName() : "General";
        String lang = (String) languageCombo.getSelectedItem();

        PriorityService.AIAnalysisResult result = priorityService.analyzeComplaint(desc, catName, lang);

        com.campusvoice.model.AIAnalysis aiModel = new com.campusvoice.model.AIAnalysis();
        aiModel.setOriginalPriority(result.getPriority());
        aiModel.setFinalPriority(result.getPriority());
        aiModel.setConfidence(result.getConfidence());
        aiModel.setReason(result.getReason());
        aiModel.setRiskFactors(result.getRiskFactors());

        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AIPriorityDialog dialog = new AIPriorityDialog(topFrame, aiModel);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            analyzedPriority = dialog.getFinalPriority();
            analyzedConfidence = aiModel.getConfidence();
            analyzedReason = aiModel.getReason();
            analyzedRiskFactors = aiModel.getRiskFactors();
            isOverridden = aiModel.isHumanOverridden();

            priorityBadge.setPriority(analyzedPriority);
            priorityBadge.setVisible(true);
            aiStatusLabel.setText("Assessed Priority: " + analyzedPriority + " (" + String.format("%.0f%%", analyzedConfidence) + ")");
            aiStatusLabel.setForeground(ThemeColors.TEXT_DARK);
        }
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in title and description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Category cat = (Category) categoryCombo.getSelectedItem();
        String catName = cat != null ? cat.getCategoryName() : "General";
        String lang = (String) languageCombo.getSelectedItem();

        if (analyzedPriority == null) {
            PriorityService.AIAnalysisResult autoRes = priorityService.analyzeComplaint(desc, catName, lang);
            analyzedPriority = autoRes.getPriority();
            analyzedConfidence = autoRes.getConfidence();
            analyzedReason = autoRes.getReason();
            analyzedRiskFactors = autoRes.getRiskFactors();
        }

        // Handle automatic translation for non-English submissions
        String cleanLang = lang != null ? (lang.contains("(") ? lang.substring(0, lang.indexOf('(')).trim() : lang.trim()) : "English";
        String translatedText = desc;
        if (!"English".equalsIgnoreCase(cleanLang)) {
            com.campusvoice.ai.LanguageService.TranslationResult transRes = com.campusvoice.ai.LanguageService.translateToEnglish(desc, cleanLang);
            if (transRes != null && transRes.getTranslatedText() != null && !transRes.getTranslatedText().trim().isEmpty()) {
                translatedText = transRes.getTranslatedText();
            }
        } else {
            com.campusvoice.ai.LanguageService.LanguageDetectionResult detected = com.campusvoice.ai.LanguageService.detectLanguage(desc);
            if (!"en".equalsIgnoreCase(detected.getLanguageCode())) {
                com.campusvoice.ai.LanguageService.TranslationResult transRes = com.campusvoice.ai.LanguageService.translateToEnglish(desc, detected.getLanguageName());
                if (transRes != null && transRes.getTranslatedText() != null && !transRes.getTranslatedText().trim().isEmpty()) {
                    translatedText = transRes.getTranslatedText();
                    cleanLang = detected.getLanguageName();
                }
            }
        }

        User u = SessionManager.getInstance().getCurrentUser();
        Complaint c = new Complaint();
        c.setStudentId(u != null ? u.getId() : null);
        c.setTitle(title);
        c.setDescription(desc);
        c.setCategoryId(cat != null ? cat.getId() : 1);
        c.setAnonymous(anonymousCheckBox.isSelected());
        c.setOriginalLanguage(cleanLang);
        c.setOriginalText(desc);
        c.setTranslatedText(translatedText);
        c.setPriority(analyzedPriority != null ? analyzedPriority : "MEDIUM");
        c.setPriorityConfidence(analyzedConfidence > 0 ? analyzedConfidence : 85.0);
        c.setAiReason(analyzedReason != null ? analyzedReason : "Complaint submitted by student.");
        c.setStatus("SUBMITTED");

        boolean ok = complaintService.createComplaint(c);
        if (ok) {
            if (selectedEvidenceFile != null) {
                if (!saveAttachment(c, selectedEvidenceFile)) {
                    JOptionPane.showMessageDialog(this, "Complaint saved: " + c.getComplaintCode()
                            + "\nEvidence upload failed. Open this complaint in My Complaints and use Add evidence to retry.",
                            "Evidence not uploaded", JOptionPane.WARNING_MESSAGE);
                    parentFrame.navigateTo("MY_COMPLAINTS");
                    return;
                }
                selectedEvidenceFile = null;
            }
            JOptionPane.showMessageDialog(this, "Grievance submitted successfully!\nTracking Code: " + c.getComplaintCode(), "Submitted", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.navigateTo("MY_COMPLAINTS");
        } else {
            JOptionPane.showMessageDialog(this, "Could not save the complaint. Check your database connection and settings.\n"
                    + "Diagnostic log: " + System.getProperty("user.home") + "/.campusvoice/logs/campusvoice.log",
                    "Submission failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean saveAttachment(Complaint complaint, File file) {
        try {
            User u = SessionManager.getInstance().getCurrentUser();
            String mimeType = Files.probeContentType(file.toPath());

            Attachment att = new Attachment();
            att.setComplaintId(complaint.getId());
            att.setFileName(file.getName());
            att.setFileType(mimeType != null ? mimeType : "application/octet-stream");
            att.setFileSize(file.length());
            att.setUploadedBy(u != null ? u.getId() : null);

            new com.campusvoice.service.AttachmentService().upload(att, file.toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

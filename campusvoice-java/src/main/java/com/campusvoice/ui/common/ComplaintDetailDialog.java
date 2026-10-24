package com.campusvoice.ui.common;

import com.campusvoice.dao.AttachmentDAO;
import com.campusvoice.dao.CommentDAO;
import com.campusvoice.dao.impl.AttachmentDAOImpl;
import com.campusvoice.dao.impl.CommentDAOImpl;
import com.campusvoice.model.Attachment;
import com.campusvoice.model.Comment;
import com.campusvoice.model.Complaint;
import com.campusvoice.model.User;
import com.campusvoice.security.SessionManager;
import com.campusvoice.ui.components.ModernButton;
import com.campusvoice.ui.components.ModernTextField;
import com.campusvoice.ui.components.PriorityBadge;
import com.campusvoice.ui.components.RoundedPanel;
import com.campusvoice.ui.components.StatusBadge;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ComplaintDetailDialog extends JDialog {
    private Complaint complaint;
    private CommentDAO commentDAO;
    private AttachmentDAO attachmentDAO;
    private DefaultListModel<String> notesModel;

    public ComplaintDetailDialog(Frame parent, Complaint complaint) {
        super(parent, "Grievance Record - " + complaint.getComplaintCode(), true);
        this.complaint = complaint;
        this.commentDAO = new CommentDAOImpl();
        this.attachmentDAO = new AttachmentDAOImpl();

        setSize(860, 720);
        setMinimumSize(new Dimension(800, 680));
        setLocationRelativeTo(parent);
        setResizable(true);

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_BACKGROUND, ThemeColors.CARD_BG);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_FOREGROUND, Color.WHITE);

        JPanel mainPanel = new RoundedPanel(20, ThemeColors.CARD_BG);
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(22, 25, 22, 25));

        // HEADER (CENTER prevents title from overlapping EAST badges)
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        String fullTitle = complaint.getComplaintCode() + " - " + complaint.getTitle();
        JLabel codeLabel = new JLabel(fullTitle);
        codeLabel.setFont(ThemeFonts.TITLE_MEDIUM);
        codeLabel.setForeground(ThemeColors.TEXT_DARK);
        codeLabel.setToolTipText(fullTitle);

        JLabel subLabel = new JLabel("Institutional Grievance Record • Autonomous Redressal Pipeline");
        subLabel.setFont(ThemeFonts.CAPTION);
        subLabel.setForeground(ThemeColors.TEXT_MUTED);

        titleBox.add(codeLabel);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subLabel);
        headerPanel.add(titleBox, BorderLayout.CENTER);

        JPanel badgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        badgeBox.setOpaque(false);
        badgeBox.add(new PriorityBadge(complaint.getPriority()));
        badgeBox.add(new StatusBadge(complaint.getStatus()));
        headerPanel.add(badgeBox, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // BODY TABS
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeFonts.BODY_BOLD);
        com.campusvoice.ui.theme.CssEngine.apply(tabs, "tabs-modern");

        // TAB 1: DETAILS & AI ANALYSIS
        JPanel detailsTab = new JPanel();
        detailsTab.setLayout(new BoxLayout(detailsTab, BoxLayout.Y_AXIS));
        detailsTab.setOpaque(false);
        detailsTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel metaGrid = new JPanel(new GridLayout(2, 3, 12, 8));
        metaGrid.setOpaque(false);
        metaGrid.add(createFieldBox("Student / Submitter:", complaint.isAnonymous() ? "Protected Anonymous" : (complaint.getStudentName() != null ? complaint.getStudentName() : "Student")));
        metaGrid.add(createFieldBox("Category:", complaint.getCategoryName() != null ? complaint.getCategoryName() : "General"));
        metaGrid.add(createFieldBox("Original Language:", complaint.getOriginalLanguage() != null ? complaint.getOriginalLanguage() : "English"));
        metaGrid.add(createFieldBox("Assigned Officer:", complaint.getAssignedToName() != null ? complaint.getAssignedToName() : "Unassigned"));
        metaGrid.add(createFieldBox("AI Confidence:", String.format("%.1f%%", complaint.getPriorityConfidence())));
        metaGrid.add(createFieldBox("Logged Date:", complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString().substring(0, 16) : "Recent"));

        detailsTab.add(metaGrid);
        detailsTab.add(Box.createVerticalStrut(12));

        JLabel origLbl = new JLabel("Official Grievance Statement:");
        origLbl.setFont(ThemeFonts.BODY_BOLD);
        origLbl.setForeground(ThemeColors.TEXT_DARK);
        detailsTab.add(origLbl);
        detailsTab.add(Box.createVerticalStrut(4));

        JTextArea origArea = new JTextArea(complaint.getDescription());
        origArea.setEditable(false);
        origArea.setLineWrap(true);
        origArea.setWrapStyleWord(true);
        origArea.setFont(ThemeFonts.BODY_REGULAR);
        origArea.setBackground(ThemeColors.INPUT_BG);
        origArea.setForeground(Color.WHITE);
        origArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane spOrig = new JScrollPane(origArea);
        spOrig.setPreferredSize(new Dimension(680, 80));
        spOrig.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        detailsTab.add(spOrig);

        detailsTab.add(Box.createVerticalStrut(10));
        JLabel aiLbl = new JLabel("Native AI Reasoning & Keyphrase Diagnostics:");
        aiLbl.setFont(ThemeFonts.BODY_BOLD);
        aiLbl.setForeground(ThemeColors.TEXT_DARK);
        detailsTab.add(aiLbl);
        detailsTab.add(Box.createVerticalStrut(4));

        JTextArea aiArea = new JTextArea(complaint.getAiReason() != null ? complaint.getAiReason() : "Evaluated by pure Java Logistic Regression Model with automatic keyword safety scan.");
        aiArea.setEditable(false);
        aiArea.setLineWrap(true);
        aiArea.setWrapStyleWord(true);
        aiArea.setFont(ThemeFonts.BODY_REGULAR);
        aiArea.setBackground(ThemeColors.INPUT_BG);
        aiArea.setForeground(Color.WHITE);
        aiArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane spAi = new JScrollPane(aiArea);
        spAi.setPreferredSize(new Dimension(680, 70));
        spAi.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        detailsTab.add(spAi);

        tabs.addTab("Overview & AI Diagnostics", detailsTab);

        // TAB 2: MULTILINGUAL SIDE-BY-SIDE
        JPanel transTab = new JPanel(new GridLayout(1, 2, 16, 0));
        transTab.setOpaque(false);
        transTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String rawLang = complaint.getOriginalLanguage() != null ? complaint.getOriginalLanguage() : "English";
        JPanel leftLang = new JPanel(new BorderLayout(0, 6));
        leftLang.setOpaque(false);
        JLabel lOrig = new JLabel("Original Language (" + rawLang + "):");
        lOrig.setFont(ThemeFonts.BODY_BOLD);
        lOrig.setForeground(ThemeColors.TEXT_DARK);

        String origContent = complaint.getOriginalText() != null && !complaint.getOriginalText().isEmpty()
                ? complaint.getOriginalText()
                : complaint.getDescription();
        JTextArea tOrig = new JTextArea(origContent);
        tOrig.setFont(ThemeFonts.BODY_REGULAR);
        tOrig.setEditable(false);
        tOrig.setLineWrap(true);
        tOrig.setWrapStyleWord(true);
        tOrig.setBackground(ThemeColors.INPUT_BG);
        tOrig.setForeground(Color.WHITE);
        tOrig.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftLang.add(lOrig, BorderLayout.NORTH);
        JScrollPane spLeft = new JScrollPane(tOrig);
        spLeft.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        leftLang.add(spLeft, BorderLayout.CENTER);

        JPanel rightLang = new JPanel(new BorderLayout(0, 6));
        rightLang.setOpaque(false);
        JLabel lTrans = new JLabel("English Translation (Standardized):");
        lTrans.setFont(ThemeFonts.BODY_BOLD);
        lTrans.setForeground(ThemeColors.TEXT_DARK);

        String transContent = complaint.getTranslatedText();
        boolean isNonEnglish = rawLang != null && !rawLang.toLowerCase().startsWith("english");
        if (transContent == null || transContent.trim().isEmpty() || (transContent.equals(origContent) && isNonEnglish)) {
            com.campusvoice.ai.LanguageService.TranslationResult tr = com.campusvoice.ai.LanguageService.translateToEnglish(origContent, rawLang);
            if (tr != null && tr.getTranslatedText() != null && !tr.getTranslatedText().isEmpty()) {
                transContent = tr.getTranslatedText();
            }
        }
        if (transContent == null || transContent.isEmpty()) {
            transContent = complaint.getDescription();
        }

        JTextArea tTrans = new JTextArea(transContent);
        tTrans.setFont(ThemeFonts.BODY_REGULAR);
        tTrans.setEditable(false);
        tTrans.setLineWrap(true);
        tTrans.setWrapStyleWord(true);
        tTrans.setBackground(ThemeColors.INPUT_BG);
        tTrans.setForeground(Color.WHITE);
        tTrans.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightLang.add(lTrans, BorderLayout.NORTH);
        JScrollPane spRight = new JScrollPane(tTrans);
        spRight.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        rightLang.add(spRight, BorderLayout.CENTER);

        transTab.add(leftLang);
        transTab.add(rightLang);
        tabs.addTab("Multilingual Translation", transTab);

        // TAB 3: TIMELINE WORKFLOW
        JPanel timelineTab = new JPanel();
        timelineTab.setLayout(new BoxLayout(timelineTab, BoxLayout.Y_AXIS));
        timelineTab.setOpaque(false);
        timelineTab.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        timelineTab.add(createTimelineStep("Step 1: Grievance Lodged", "Logged with unique identifier " + complaint.getComplaintCode(), true));
        timelineTab.add(Box.createVerticalStrut(10));
        timelineTab.add(createTimelineStep("Step 2: Native AI Assessment", "Evaluated " + complaint.getPriority() + " priority (" + String.format("%.0f%%", complaint.getPriorityConfidence()) + " confidence)", true));
        timelineTab.add(Box.createVerticalStrut(10));

        boolean isAssigned = !"SUBMITTED".equalsIgnoreCase(complaint.getStatus());
        timelineTab.add(createTimelineStep(isAssigned ? "Step 3: Officer Assigned" : "Step 3: Pending Officer Assignment", "Delegated to " + (complaint.getAssignedToName() != null ? complaint.getAssignedToName() : "Department Head"), isAssigned));
        timelineTab.add(Box.createVerticalStrut(10));

        boolean inProgress = "IN PROGRESS".equalsIgnoreCase(complaint.getStatus()) || "RESOLVED".equalsIgnoreCase(complaint.getStatus());
        timelineTab.add(createTimelineStep(inProgress ? "Step 4: Redressal In Progress" : "Step 4: Awaiting Field Action", "Action underway with assigned faculty or technical department.", inProgress));
        timelineTab.add(Box.createVerticalStrut(10));

        boolean isResolved = "RESOLVED".equalsIgnoreCase(complaint.getStatus());
        timelineTab.add(createTimelineStep(isResolved ? "Step 5: Resolved & Closed" : "Step 5: Pending Final Sign-Off", isResolved ? "Official resolution confirmed and closed in database." : "Awaiting student verification or executive override.", isResolved));

        tabs.addTab("Lifecycle Timeline", timelineTab);

        // TAB 4: ACTION NOTES & COMMENTS (Connected to CommentDAO)
        JPanel notesTab = new JPanel(new BorderLayout(0, 10));
        notesTab.setOpaque(false);
        notesTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        notesModel = new DefaultListModel<>();
        loadCommentsIntoModel();

        JList<String> notesList = new JList<>(notesModel);
        notesList.setFont(ThemeFonts.BODY_REGULAR);
        notesList.setBackground(ThemeColors.INPUT_BG);
        notesList.setForeground(Color.WHITE);
        JScrollPane spNotes = new JScrollPane(notesList);
        spNotes.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR));
        notesTab.add(spNotes, BorderLayout.CENTER);

        JPanel addNoteBox = new JPanel(new BorderLayout(8, 0));
        addNoteBox.setOpaque(false);
        ModernTextField noteInput = new ModernTextField();
        noteInput.setToolTipText("Add action remark or official update...");
        ModernButton addNoteBtn = new ModernButton("Post Note", ThemeColors.PRIMARY_BLUE);
        addNoteBtn.addActionListener(e -> {
            String txt = noteInput.getText().trim();
            if (!txt.isEmpty()) {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                String authorId = currentUser != null ? currentUser.getId() : "Staff";
                String authorName = currentUser != null ? currentUser.getFullName() : "Staff";
                Comment newComment = new Comment(complaint.getId() != null ? complaint.getId() : complaint.getComplaintCode(), authorId, txt, false);
                commentDAO.createComment(newComment);
                notesModel.addElement("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "] " + authorName + ": " + txt);
                noteInput.setText("");
            }
        });
        addNoteBox.add(noteInput, BorderLayout.CENTER);
        addNoteBox.add(addNoteBtn, BorderLayout.EAST);
        notesTab.add(addNoteBox, BorderLayout.SOUTH);

        tabs.addTab("Action Notes & History", notesTab);

        // TAB 5: EVIDENCE FILES
        JPanel evidenceTab = new JPanel();
        evidenceTab.setLayout(new BoxLayout(evidenceTab, BoxLayout.Y_AXIS));
        evidenceTab.setOpaque(false);
        evidenceTab.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel evHeader = new JLabel("Attached Evidence Files");
        evHeader.setFont(ThemeFonts.BODY_BOLD);
        evHeader.setForeground(ThemeColors.TEXT_DARK);
        evHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        evidenceTab.add(evHeader);
        evidenceTab.add(Box.createVerticalStrut(8));

        List<Attachment> attachments = attachmentDAO.findByComplaintId(
                complaint.getId() != null ? complaint.getId() : complaint.getComplaintCode());

        if (attachments.isEmpty()) {
            JLabel noFiles = new JLabel("No evidence files were attached to this complaint.");
            noFiles.setFont(ThemeFonts.BODY_REGULAR);
            noFiles.setForeground(ThemeColors.TEXT_MUTED);
            noFiles.setAlignmentX(Component.LEFT_ALIGNMENT);
            evidenceTab.add(noFiles);
        } else {
            for (Attachment att : attachments) {
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, ThemeColors.CYAN_ACCENT),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                long kb = att.getFileSize() / 1024;
                String info = att.getFileName() + "  (" + (kb > 0 ? kb + " KB" : att.getFileSize() + " bytes") + ")  |  " + att.getFileType();
                JLabel fileLbl = new JLabel(info);
                fileLbl.setFont(ThemeFonts.BODY_REGULAR);
                fileLbl.setForeground(ThemeColors.TEXT_DARK);

                ModernButton openBtn = new ModernButton("Open File", ThemeColors.PRIMARY_BLUE);
                openBtn.setPreferredSize(new Dimension(100, 30));
                String storagePath = att.getStoragePath();
                openBtn.addActionListener(ev -> {
                    try {
                        File f = new File(storagePath);
                        if (f.exists() && Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(f);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "File not found at:\n" + storagePath,
                                "File Missing", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                            "Cannot open file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                row.add(fileLbl, BorderLayout.CENTER);
                row.add(openBtn, BorderLayout.EAST);
                evidenceTab.add(row);
                evidenceTab.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane evScroll = new JScrollPane(evidenceTab);
        evScroll.setBorder(null);
        evScroll.getViewport().setOpaque(false);
        evScroll.setOpaque(false);

        // Wrap in a panel so BoxLayout works properly in the scroll
        JPanel evWrapper = new JPanel(new BorderLayout());
        evWrapper.setOpaque(false);
        evWrapper.add(evidenceTab, BorderLayout.NORTH);
        evScroll.setViewportView(evWrapper);

        tabs.addTab("Evidence Files (" + attachments.size() + ")", evScroll);

        mainPanel.add(tabs, BorderLayout.CENTER);

        // CLOSE BUTTON
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        ModernButton closeBtn = new ModernButton("Close Window", ThemeColors.PRIMARY_BLUE, ThemeColors.PRIMARY_HOVER);
        com.campusvoice.ui.theme.CssEngine.apply(closeBtn, "btn-primary");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadCommentsIntoModel() {
        notesModel.clear();
        String dateStr = complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString().substring(0, 16) : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        notesModel.addElement("[" + dateStr + "] System: Complaint successfully logged and classified as " + complaint.getPriority() + ".");
        if (complaint.getAssignedToName() != null && !"Unassigned".equalsIgnoreCase(complaint.getAssignedToName())) {
            notesModel.addElement("[" + dateStr + "] Office: Assigned to " + complaint.getAssignedToName() + " for priority redressal.");
        }

        try {
            List<Comment> comments = commentDAO.findByComplaintId(complaint.getId() != null ? complaint.getId() : complaint.getComplaintCode());
            for (Comment c : comments) {
                String time = c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 16) : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                String author = c.getUserName() != null ? c.getUserName() : "Official";
                notesModel.addElement("[" + time + "] " + author + ": " + c.getCommentText());
            }
        } catch (Exception ignored) {}
    }

    private JPanel createFieldBox(String label, String val) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l1.setForeground(ThemeColors.TEXT_MUTED);
        JLabel l2 = new JLabel(val);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l2.setForeground(ThemeColors.TEXT_DARK);
        p.add(l1);
        p.add(l2);
        return p;
    }

    private JPanel createTimelineStep(String title, String desc, boolean completed) {
        RoundedPanel p = new RoundedPanel(12, completed ? new Color(10, 48, 32, 220) : ThemeColors.CARD_SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                com.campusvoice.ui.theme.Java2DUtils.applyQualityHints(g2);
                com.campusvoice.ui.theme.Java2DUtils.drawGlowingDot(g2, 14, getHeight() / 2, 4,
                        completed ? ThemeColors.PRIORITY_LOW : new Color(148, 163, 184));
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout(10, 2));
        p.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 14));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tLbl.setForeground(completed ? ThemeColors.PRIORITY_LOW : ThemeColors.TEXT_DARK);

        JLabel dLbl = new JLabel(desc);
        dLbl.setFont(ThemeFonts.CAPTION);
        dLbl.setForeground(ThemeColors.TEXT_MUTED);

        p.add(tLbl, BorderLayout.NORTH);
        p.add(dLbl, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return p;
    }
}

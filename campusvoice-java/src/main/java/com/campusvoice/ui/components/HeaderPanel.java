package com.campusvoice.ui.components;

import com.campusvoice.model.User;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Shared dashboard header with a quiet identity and date treatment. */
public class HeaderPanel extends JPanel {
    public HeaderPanel(User user, String subtitle) {
        setLayout(new BorderLayout(24, 0));
        setBackground(ThemeColors.SIDEBAR_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)));

        String name = user != null && user.getFullName() != null ? user.getFullName() : "User";
        JPanel identity = new JPanel(new BorderLayout(14, 0));
        identity.setOpaque(false);
        JLabel avatar = new JLabel(name.isBlank() ? "U" : name.substring(0, 1).toUpperCase(), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(ThemeColors.SIDEBAR_ACTIVE_BG);
        avatar.setForeground(ThemeColors.TEXT_DARK);
        avatar.setFont(ThemeFonts.TITLE_SMALL);
        avatar.setPreferredSize(new Dimension(42, 42));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Welcome, " + name);
        welcome.setFont(ThemeFonts.TITLE_MEDIUM);
        welcome.setForeground(ThemeColors.TEXT_DARK);
        JLabel description = new JLabel(subtitle != null ? subtitle : "Campus complaint management");
        description.setFont(ThemeFonts.BODY_REGULAR);
        description.setForeground(ThemeColors.TEXT_MUTED);
        text.add(welcome);
        text.add(Box.createVerticalStrut(5));
        text.add(description);
        identity.add(avatar, BorderLayout.WEST);
        identity.add(text, BorderLayout.CENTER);

        JPanel metadata = new JPanel();
        metadata.setOpaque(false);
        metadata.setLayout(new BoxLayout(metadata, BoxLayout.Y_AXIS));
        String role = user != null && user.getRoleName() != null ? user.getRoleName().replace('_', ' ') : "USER";
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(ThemeFonts.BODY_BOLD);
        roleLabel.setForeground(ThemeColors.TEXT_DARK);
        roleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setFont(ThemeFonts.CAPTION);
        date.setForeground(ThemeColors.TEXT_MUTED);
        date.setAlignmentX(Component.RIGHT_ALIGNMENT);
        metadata.add(roleLabel);
        metadata.add(Box.createVerticalStrut(5));
        metadata.add(date);
        JLabel databaseMode = new JLabel(com.campusvoice.config.DatabaseConfig.isH2Fallback()
                ? "Local data: this Windows account" : "Shared PostgreSQL database");
        databaseMode.setFont(ThemeFonts.CAPTION);
        databaseMode.setForeground(ThemeColors.TEXT_MUTED);
        databaseMode.setAlignmentX(Component.RIGHT_ALIGNMENT);
        metadata.add(Box.createVerticalStrut(5));
        metadata.add(databaseMode);
        add(identity, BorderLayout.CENTER);
        add(metadata, BorderLayout.EAST);
    }
}

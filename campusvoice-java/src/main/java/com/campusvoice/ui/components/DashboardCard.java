package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Dashboard Metric Card with ambient radial accent glow,
 * specular glass highlights, glowing accent indicator, and interactive elevation.
 */
public class DashboardCard extends RoundedPanel {
    private JLabel titleLabel;
    private JLabel countLabel;
    private JLabel subtextLabel;
    private Color accentColor;
    private boolean hovered = false;

    public DashboardCard(String title, String count, String subtext, Color accentColor) {
        super(16, ThemeColors.CARD_BG, ThemeColors.CARD_SECONDARY);
        this.accentColor = accentColor;
        setShadowEnabled(false);
        setGlassmorphic(false);

        setLayout(new BorderLayout(12, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Title row with colored accent indicator
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dot.setForeground(accentColor);

        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeFonts.BODY_BOLD);
        titleLabel.setForeground(ThemeColors.TEXT_MUTED);

        titleRow.add(titleLabel);

        countLabel = new JLabel(count);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        countLabel.setForeground(ThemeColors.TEXT_DARK);

        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(ThemeFonts.CAPTION);
        subtextLabel.setForeground(ThemeColors.TEXT_MUTED);

        content.add(titleRow);
        content.add(Box.createVerticalStrut(6));
        content.add(countLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtextLabel);

        add(content, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                setGradientColors(ThemeColors.CARD_HOVER, ThemeColors.CARD_SECONDARY);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                setGradientColors(ThemeColors.CARD_BG, ThemeColors.CARD_SECONDARY);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(accentColor);
        g2.fillRoundRect(0, 20, 3, Math.max(0, h - 40), 3, 3);
        g2.dispose();
    }

    public void updateCount(String newCount) {
        countLabel.setText(newCount);
    }

    public void updateSubtext(String subtext) {
        subtextLabel.setText(subtext);
    }
}

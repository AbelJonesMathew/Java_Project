package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Status Badge with glowing neon status indicators,
 * subtle glassmorphism, and crisp typography.
 */
public class StatusBadge extends JLabel {
    private String status;

    public StatusBadge(String status) {
        super(status != null ? status : "SUBMITTED", SwingConstants.CENTER);
        this.status = status != null ? status : "SUBMITTED";
        setFont(ThemeFonts.BADGE_FONT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 6));
        setPreferredSize(new Dimension(108, 24));
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "SUBMITTED";
        setText(this.status);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        Color mainColor = ThemeColors.STATUS_SUBMITTED;
        Color bgTop = new Color(22, 28, 52, 220);
        Color bgBot = new Color(14, 18, 36, 240);

        if ("IN PROGRESS".equals(status)) {
            mainColor = ThemeColors.STATUS_IN_PROGRESS;
            bgTop = new Color(10, 36, 60, 220);
            bgBot = new Color(6, 24, 42, 240);
        } else if ("ASSIGNED".equals(status)) {
            mainColor = ThemeColors.STATUS_ASSIGNED;
            bgTop = new Color(16, 32, 68, 220);
            bgBot = new Color(10, 22, 48, 240);
        } else if ("RESOLVED".equals(status)) {
            mainColor = ThemeColors.STATUS_RESOLVED;
            bgTop = new Color(10, 48, 32, 220);
            bgBot = new Color(6, 32, 22, 240);
        } else if ("ESCALATED".equals(status) || "REJECTED".equals(status)) {
            mainColor = ThemeColors.STATUS_ESCALATED;
            bgTop = new Color(58, 16, 28, 220);
            bgBot = new Color(38, 10, 18, 240);
        }

        setForeground(mainColor);

        // 1. Pill Background Gradient
        GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, h, bgBot);
        g2.setPaint(gp);
        RoundRectangle2D.Float pill = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h);
        g2.fill(pill);

        // 2. Specular Top Highlight
        GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 30), 0, h / 2, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 3, (float) (h / 2.2), h / 2, h / 2));

        // 3. Glowing Indicator Dot
        Java2DUtils.drawGlowingDot(g2, 10, h / 2, 3, mainColor);

        // 4. Subtle Outer Border
        g2.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 90));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(pill);

        g2.dispose();
        super.paintComponent(g);
    }
}

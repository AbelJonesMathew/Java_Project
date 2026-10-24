package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Priority Badge with radial glowing indicator,
 * glassmorphic pill background, and vibrant typography.
 */
public class PriorityBadge extends JLabel {
    private String priority;

    public PriorityBadge(String priority) {
        super(priority != null ? priority.toUpperCase() : "MEDIUM", SwingConstants.CENTER);
        this.priority = priority != null ? priority.toUpperCase() : "MEDIUM";
        setFont(ThemeFonts.BADGE_FONT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 14, 0, 6));
        setPreferredSize(new Dimension(88, 24));
    }

    public void setPriority(String priority) {
        this.priority = priority != null ? priority.toUpperCase() : "MEDIUM";
        setText(this.priority);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        Color text = ThemeColors.PRIORITY_MED;
        Color bgTop = new Color(50, 32, 8, 220);
        Color bgBot = new Color(30, 20, 5, 240);

        if ("HIGH".equals(priority)) {
            text = ThemeColors.PRIORITY_HIGH;
            bgTop = new Color(58, 16, 28, 220);
            bgBot = new Color(38, 10, 18, 240);
        } else if ("LOW".equals(priority)) {
            text = ThemeColors.PRIORITY_LOW;
            bgTop = new Color(10, 48, 32, 220);
            bgBot = new Color(6, 32, 22, 240);
        }

        setForeground(text);

        // 1. Pill Background Gradient
        GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, h, bgBot);
        g2.setPaint(gp);
        RoundRectangle2D.Float pill = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h);
        g2.fill(pill);

        // 2. Specular Top Highlight
        GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 35), 0, h / 2, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 3, (float) (h / 2.2), h / 2, h / 2));

        // 3. Glowing Indicator Dot
        Java2DUtils.drawGlowingDot(g2, 10, h / 2, 3, text);

        // 4. Subtle Border
        g2.setColor(new Color(text.getRed(), text.getGreen(), text.getBlue(), 90));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(pill);

        g2.dispose();
        super.paintComponent(g);
    }
}

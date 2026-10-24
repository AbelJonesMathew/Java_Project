package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D ScrollBar UI with smooth rounded thumb pills,
 * minimalist flat track, and glowing hover states.
 */
public class ModernScrollBarUI extends BasicScrollBarUI {
    private final int THUMB_SIZE = 8;

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(ThemeColors.BACKGROUND);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        boolean isHovered = isThumbRollover();
        boolean dragging = isDragging;

        Color topColor;
        Color botColor;

        if (dragging) {
            topColor = ThemeColors.PRIMARY_HOVER;
            botColor = ThemeColors.PRIMARY_BLUE;
        } else if (isHovered) {
            topColor = new Color(75, 95, 150);
            botColor = new Color(55, 70, 115);
        } else {
            topColor = new Color(45, 58, 95);
            botColor = new Color(30, 40, 72);
        }

        int x = thumbBounds.x + (thumbBounds.width - THUMB_SIZE) / 2;
        int y = thumbBounds.y + 2;
        int w = THUMB_SIZE;
        int h = Math.max(20, thumbBounds.height - 4);

        GradientPaint gp = new GradientPaint(x, y, topColor, x + w, y + h, botColor);
        g2.setPaint(gp);
        RoundRectangle2D.Float thumb = new RoundRectangle2D.Float(x, y, w, h, w, w);
        g2.fill(thumb);

        // Specular highlight
        g2.setColor(new Color(255, 255, 255, isHovered ? 60 : 30));
        g2.setStroke(new BasicStroke(0.8f));
        g2.draw(thumb);

        g2.dispose();
    }
}

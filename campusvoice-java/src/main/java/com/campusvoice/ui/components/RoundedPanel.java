package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D rounded panel featuring soft multi-pass ambient shadows,
 * glassmorphism, specular top-edge highlights, and gradient fills.
 */
public class RoundedPanel extends JPanel {
    private int cornerRadius = 16;
    private Color backgroundColor = ThemeColors.CARD_BG;
    private Color gradientBottom = ThemeColors.GLASS_BG_BOT;
    private Color borderColor = ThemeColors.BORDER_COLOR;
    private boolean drawBorder = true;
    private boolean shadowEnabled = false;
    private int shadowSize = 3;
    private boolean glassmorphic = false;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bg) {
        this.cornerRadius = radius;
        this.backgroundColor = bg;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgTop, Color bgBottom) {
        this.cornerRadius = radius;
        this.backgroundColor = bgTop;
        this.gradientBottom = bgBottom;
        setOpaque(false);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setGradientColors(Color top, Color bottom) {
        this.backgroundColor = top;
        this.gradientBottom = bottom;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    public void setDrawBorder(boolean draw) {
        this.drawBorder = draw;
        repaint();
    }

    public void setShadowEnabled(boolean enabled) {
        this.shadowEnabled = enabled;
        repaint();
    }

    public void setGlassmorphic(boolean glassmorphic) {
        this.glassmorphic = glassmorphic;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int x = shadowEnabled ? shadowSize : 0;
        int y = shadowEnabled ? shadowSize : 0;
        int w = getWidth() - (shadowEnabled ? shadowSize * 2 : 1);
        int h = getHeight() - (shadowEnabled ? shadowSize * 2 : 1);

        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        // 1. Multi-pass ambient drop shadow
        if (shadowEnabled) {
            Java2DUtils.drawCardShadow(g2, x, y, w, h, cornerRadius, shadowSize);
        }

        // 2. Glassmorphic Surface or Gradient Fill
        Color topColor = backgroundColor;
        Color botColor = (gradientBottom != null) ? gradientBottom : new Color(
                Math.max(0, topColor.getRed() - 6),
                Math.max(0, topColor.getGreen() - 6),
                Math.max(0, topColor.getBlue() - 8),
                topColor.getAlpha()
        );

        if (glassmorphic) {
            Java2DUtils.paintGlassSurface(g2, x, y, w, h, cornerRadius, topColor, botColor, drawBorder ? borderColor : null);
        } else {
            GradientPaint gp = new GradientPaint(x, y, topColor, x, y + h, botColor);
            g2.setColor(topColor);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, cornerRadius, cornerRadius));

            if (drawBorder && borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(x, y, w, h, cornerRadius, cornerRadius));
            }
        }

        g2.dispose();
    }
}

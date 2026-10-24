package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Button with dynamic linear gradients,
 * ambient hover glow, specular highlight edge, and responsive pressed states.
 */
public class ModernButton extends JButton {
    private Color normalBg = ThemeColors.PRIMARY_BLUE;
    private Color hoverBg = ThemeColors.PRIMARY_HOVER;
    private Color textColor = Color.WHITE;
    private int radius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text) {
        super(text);
        initButton();
    }

    public ModernButton(String text, Color bg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = computeHoverColor(bg);
        initButton();
    }

    public ModernButton(String text, Color bg, Color hover) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = hover;
        initButton();
    }

    public ModernButton(String text, Color bg, Color hover, Color textCol) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = hover;
        this.textColor = textCol;
        initButton();
    }

    private Color computeHoverColor(Color base) {
        int r = Math.min(255, (int) (base.getRed() * 1.15 + 15));
        int g = Math.min(255, (int) (base.getGreen() * 1.15 + 15));
        int b = Math.min(255, (int) (base.getBlue() * 1.15 + 15));
        return new Color(r, g, b, base.getAlpha());
    }

    private void initButton() {
        setFont(ThemeFonts.BODY_BOLD);
        setForeground(textColor);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 18, 10, 18));
        setRolloverEnabled(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCustomColors(Color normal, Color hover, Color text) {
        this.normalBg = normal;
        this.hoverBg = hover;
        this.textColor = text;
        setForeground(text);
        repaint();
    }

    public void setRadius(int r) {
        this.radius = r;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        Color fill = !isEnabled() ? ThemeColors.CARD_SECONDARY
                : getModel().isPressed() ? normalBg.darker()
                : getModel().isRollover() ? hoverBg : normalBg;
        g2.setColor(fill);
        g2.fillRoundRect(1, 1, w - 3, h - 3, radius, radius);
        if (hasFocus()) {
            g2.setColor(ThemeColors.TEXT_DARK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, radius, radius);
        }
        g2.dispose();

        super.paintComponent(g);
    }
}

package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Sidebar Navigation Button featuring vibrant linear gradients,
 * glowing neon active indicators, and translucent glass hover states.
 */
public class SidebarButton extends JButton {
    private boolean active = false;
    private boolean hovered = false;

    public SidebarButton(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(ThemeColors.TEXT_MUTED);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        putClientProperty("JButton.buttonType", "borderless");
        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.RIGHT);
        setIconTextGap(12);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 16));

        // Ensure strict left alignment and full width in BoxLayout.Y_AXIS
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(265, 44));
        setMinimumSize(new Dimension(220, 44));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                if (!active) setForeground(Color.WHITE);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                if (!active) setForeground(ThemeColors.TEXT_MUTED);
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setForeground(Color.WHITE);
        } else {
            setForeground(ThemeColors.TEXT_MUTED);
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int marginX = 10;
        int w = getWidth() - (marginX * 2);
        int h = getHeight() - 4;
        int y = 2;

        if (active || hovered || hasFocus()) {
            g2.setColor(active ? ThemeColors.SIDEBAR_ACTIVE_BG : ThemeColors.SIDEBAR_HOVER);
            g2.fillRoundRect(marginX, y, w, h, 8, 8);
            if (active) {
                g2.setColor(ThemeColors.CYAN_ACCENT);
                g2.fillRoundRect(marginX, y + 10, 3, h - 20, 3, 3);
            }
            if (hasFocus()) {
                g2.setColor(ThemeColors.CYAN_ACCENT);
                g2.drawRoundRect(marginX, y, w - 1, h - 1, 8, 8);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

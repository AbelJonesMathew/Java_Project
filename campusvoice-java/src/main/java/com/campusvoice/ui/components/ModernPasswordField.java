package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Password Field with interactive neon focus glow,
 * smooth rounded corners, dark glassmorphic styling, and native placeholder support.
 */
public class ModernPasswordField extends JPasswordField {
    private boolean focused = false;
    private int cornerRadius = 12;
    private String placeholder;

    public ModernPasswordField() {
        setFont(ThemeFonts.BODY_REGULAR);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        setPreferredSize(new Dimension(250, 42));

        putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    public ModernPasswordField(String text) {
        this();
        setText(text);
    }

    public ModernPasswordField(String text, String placeholder) {
        this(text);
        setPlaceholder(placeholder);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        repaint();
    }

    public String getPlaceholder() {
        if (placeholder != null) return placeholder;
        Object p = getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT);
        return p != null ? p.toString() : null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        // 1. Interactive Focus Glow Ring
        if (focused) {
            Java2DUtils.drawGlow(g2, 1, 1, w - 2, h - 2, cornerRadius, ThemeColors.PRIMARY_BLUE, 3);
        }

        // 2. Field Background Gradient
        Color topBg = ThemeColors.INPUT_BG;
        Color botBg = new Color(12, 16, 32);
        GradientPaint gp = new GradientPaint(0, 0, topBg, 0, h, botBg);
        g2.setPaint(gp);
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);
        g2.fill(shape);

        // 3. Subtle Border
        Color borderCol = focused ? ThemeColors.PRIMARY_HOVER : ThemeColors.BORDER_COLOR;
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
        g2.draw(shape);

        // 4. Draw Placeholder Text if field is empty and not focused
        String ph = getPlaceholder();
        if (ph != null && !ph.isEmpty() && (getPassword() == null || getPassword().length == 0) && !focused) {
            g2.setColor(new Color(148, 163, 184, 160));
            g2.setFont(getFont());
            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(ph, insets.left, y);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

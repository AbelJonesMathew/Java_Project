package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Text Field with interactive neon focus glow,
 * smooth rounded corners, dark glassmorphic styling, and native placeholder support.
 */
public class ModernTextField extends JTextField {
    private boolean focused = false;
    private int cornerRadius = 12;
    private String placeholder;

    public ModernTextField() {
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

    public ModernTextField(String text) {
        this();
        setText(text);
    }

    public ModernTextField(String text, String placeholder) {
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

        // 2. Field Background Gradient (Frosted Glass Input)
        Color topBg = new Color(17, 24, 44, 210);
        Color botBg = new Color(12, 17, 34, 230);
        GradientPaint gp = new GradientPaint(0, 0, topBg, 0, h, botBg);
        g2.setPaint(gp);
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);
        g2.fill(shape);

        // 3. Specular Upper Glass Reflection Glare
        GradientPaint glassShine = new GradientPaint(
                1, 1, new Color(255, 255, 255, 25),
                1, h / 2f, new Color(255, 255, 255, 0)
        );
        g2.setPaint(glassShine);
        g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, (float) (h / 2.2), cornerRadius - 1, cornerRadius - 1));

        // 4. Directional Specular Glass Border
        if (focused) {
            g2.setColor(ThemeColors.PRIMARY_HOVER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(shape);
        } else {
            GradientPaint glassBorder = new GradientPaint(
                    1, 1, new Color(255, 255, 255, 50),
                    w, h, new Color(255, 255, 255, 12)
            );
            g2.setPaint(glassBorder);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(shape);
        }

        // 5. Draw Placeholder Text if field is empty and not focused
        String ph = getPlaceholder();
        if (ph != null && !ph.isEmpty() && (getText() == null || getText().isEmpty()) && !focused) {
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

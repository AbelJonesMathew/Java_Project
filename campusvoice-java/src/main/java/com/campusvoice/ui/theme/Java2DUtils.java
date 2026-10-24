package com.campusvoice.ui.theme;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * High-performance Java 2D rendering utilities for ultra-modern UI design:
 * anti-aliasing, soft multi-pass shadows, ambient glows, glassmorphism,
 * specular edge highlights, and gradient fills.
 */
public class Java2DUtils {

    /**
     * Applies highest quality rendering hints to the Graphics2D context.
     */
    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /**
     * Renders a soft multi-pass drop shadow beneath a rounded rectangular card.
     */
    public static void drawCardShadow(Graphics2D g2, int x, int y, int width, int height, int arc, int shadowSize) {
        Color baseShadow = new Color(0, 0, 0, 18);
        for (int i = shadowSize; i >= 1; i--) {
            float opacity = (float) (0.015 + (0.025 * (1.0 - (float) i / shadowSize)));
            g2.setColor(new Color(0f, 0f, 0f, Math.min(1.0f, opacity)));
            g2.fill(new RoundRectangle2D.Float(
                    x - i,
                    y - i + 2,
                    width + (i * 2),
                    height + (i * 2),
                    arc + (i * 2),
                    arc + (i * 2)
            ));
        }
    }

    /**
     * Renders an ambient neon or soft glow around a rounded component.
     */
    public static void drawGlow(Graphics2D g2, int x, int y, int width, int height, int arc, Color glowColor, int spread) {
        float r = glowColor.getRed() / 255f;
        float g = glowColor.getGreen() / 255f;
        float b = glowColor.getBlue() / 255f;

        for (int i = spread; i >= 1; i--) {
            float alpha = (float) (0.03 * (1.0 - (float) i / (spread + 1)));
            g2.setColor(new Color(r, g, b, alpha));
            g2.fill(new RoundRectangle2D.Float(
                    x - i,
                    y - i,
                    width + (i * 2),
                    height + (i * 2),
                    arc + (i * 2),
                    arc + (i * 2)
            ));
        }
    }

    /**
     * Paints an ultra-modern celestial Aurora backdrop with multi-stop radial glow orbs
     * in electric blue, violet, and cyan, providing luminous depth for glassmorphism refraction.
     */
    /** Paint a quiet canvas behind dashboard content. */
    public static void paintAuroraBackdrop(Graphics2D g2, int width, int height) {
        g2.setColor(ThemeColors.BACKGROUND);
        g2.fillRect(0, 0, width, height);
    }

    public static void paintGlassSurface(Graphics2D g2, int x, int y, int width, int height, int arc,
                                        Color topBg, Color bottomBg, Color borderColor) {
        if (width <= 0 || height <= 0) return;

        // 1. Translucent Frosted Glass Body Gradient
        Color cTop = (topBg != null)
                ? new Color(topBg.getRed(), topBg.getGreen(), topBg.getBlue(), Math.min(215, topBg.getAlpha() == 255 ? 195 : topBg.getAlpha()))
                : new Color(24, 32, 54, 195);
        Color cBot = (bottomBg != null)
                ? new Color(bottomBg.getRed(), bottomBg.getGreen(), bottomBg.getBlue(), Math.min(235, bottomBg.getAlpha() == 255 ? 220 : bottomBg.getAlpha()))
                : new Color(13, 19, 36, 220);

        GradientPaint bgGradient = new GradientPaint(x, y, cTop, x, y + height, cBot);
        g2.setPaint(bgGradient);
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x, y, width - 1, height - 1, arc, arc);
        g2.fill(rect);

        // 2. Specular Top-Edge Light Glare (Glassmorphic 3D light reflection)
        int glareH = Math.min(34, Math.max(14, height / 3));
        GradientPaint specular = new GradientPaint(
                x, y, new Color(255, 255, 255, 42),
                x, y + glareH, new Color(255, 255, 255, 0)
        );
        g2.setPaint(specular);
        g2.fill(new RoundRectangle2D.Float(x + 1, y + 1, width - 3, glareH, arc - 2, arc - 2));

        // 3. Crisp Inner Top Rim Light Refraction Line
        g2.setColor(new Color(255, 255, 255, 52));
        g2.drawLine(x + (arc / 2), y + 1, x + width - (arc / 2), y + 1);

        // 4. Directional Specular Glass Bevel Border
        GradientPaint glassBorder = new GradientPaint(
                x, y, new Color(255, 255, 255, 65),
                x + width, y + height, new Color(255, 255, 255, 14)
        );
        g2.setPaint(glassBorder);
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, arc, arc));
    }

    /**
     * Renders a glowing status or priority dot with radial bloom.
     */
    public static void drawGlowingDot(Graphics2D g2, int centerX, int centerY, int radius, Color color) {
        // Outer halo
        RadialGradientPaint rgp = new RadialGradientPaint(
                centerX, centerY, radius * 2.2f,
                new float[]{0.0f, 0.45f, 1.0f},
                new Color[]{
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 160),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 60),
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)
                }
        );
        g2.setPaint(rgp);
        g2.fillOval(centerX - (int) (radius * 2.2f), centerY - (int) (radius * 2.2f),
                (int) (radius * 4.4f), (int) (radius * 4.4f));

        // Core dot
        g2.setColor(color);
        g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Specular inner shine
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
    }
}

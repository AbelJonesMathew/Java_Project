package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ultra-modern Java 2D Dynamic Chart Panel featuring glassmorphic backgrounds,
 * anti-aliased gradient bars, donut charts with central metric cutouts,
 * and high-precision data visualizations.
 */
public class DynamicChartPanel extends JPanel {
    public enum ChartType { PIE, BAR, ACCURACY_DONUT }

    private ChartType chartType;
    private String title;
    private Map<String, Integer> data = new LinkedHashMap<>();
    private Map<String, Color> colors = new LinkedHashMap<>();
    private double accuracyPercentage = 92.4;

    public DynamicChartPanel(String title, ChartType chartType) {
        this.title = title;
        this.chartType = chartType;
        setOpaque(false);
        setPreferredSize(new Dimension(340, 250));
    }

    public void setData(Map<String, Integer> newData, Map<String, Color> newColors) {
        this.data = newData;
        this.colors = newColors;
        repaint();
    }

    public void setAccuracyPercentage(double acc) {
        this.accuracyPercentage = acc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Java2DUtils.applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        // 1. Soft Drop Shadow
        Java2DUtils.drawCardShadow(g2, 4, 4, w - 8, h - 8, 16, 4);

        // 2. Glassmorphic Surface
        Java2DUtils.paintGlassSurface(g2, 4, 4, w - 8, h - 8, 16,
                ThemeColors.CARD_BG, ThemeColors.CARD_SECONDARY, ThemeColors.BORDER_COLOR);

        // 3. Title with Accent Indicator
        Java2DUtils.drawGlowingDot(g2, 22, 26, 4, ThemeColors.CYAN_ACCENT);
        g2.setColor(ThemeColors.TEXT_DARK);
        g2.setFont(ThemeFonts.BODY_BOLD);
        g2.drawString(title, 34, 30);

        // 4. Render Specialized Chart Type
        if (chartType == ChartType.PIE) {
            drawDonutChart(g2);
        } else if (chartType == ChartType.BAR) {
            drawBarChart(g2);
        } else if (chartType == ChartType.ACCURACY_DONUT) {
            drawAccuracyDonut(g2);
        }

        g2.dispose();
    }

    private void drawDonutChart(Graphics2D g2) {
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) total = 1;

        int diameter = Math.min(getWidth() / 2 - 10, getHeight() - 75);
        int x = 20;
        int y = 50;

        double startAngle = 90;
        int legendX = x + diameter + 20;
        int legendY = 62;

        // Render outer donut segments
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String key = entry.getKey();
            int val = entry.getValue();
            double arcAngle = ((double) val / total) * 360.0;

            Color baseColor = colors.getOrDefault(key, ThemeColors.PRIMARY_BLUE);
            Color topGrad = baseColor;
            Color botGrad = new Color(
                    Math.max(0, baseColor.getRed() - 40),
                    Math.max(0, baseColor.getGreen() - 40),
                    Math.max(0, baseColor.getBlue() - 30)
            );

            GradientPaint gp = new GradientPaint(x, y, topGrad, x + diameter, y + diameter, botGrad);
            g2.setPaint(gp);
            g2.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, -arcAngle, Arc2D.PIE));

            startAngle -= arcAngle;

            // Modern Legend Row
            if (legendX + 80 < getWidth()) {
                Java2DUtils.drawGlowingDot(g2, legendX + 6, legendY + 5, 4, baseColor);

                g2.setColor(ThemeColors.TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                int pct = (int) Math.round((double) val / total * 100);
                g2.drawString(key + ": " + val + " (" + pct + "%)", legendX + 16, legendY + 9);
                legendY += 24;
            }
        }

        // Inner Cutout to create modern Donut with ambient shadow
        int holeDiameter = (int) (diameter * 0.58);
        int holeX = x + (diameter - holeDiameter) / 2;
        int holeY = y + (diameter - holeDiameter) / 2;

        // Subtle shadow in the hole
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fill(new Ellipse2D.Double(holeX - 1, holeY - 1, holeDiameter + 2, holeDiameter + 2));

        // Center circle fill matching background
        GradientPaint holeGp = new GradientPaint(holeX, holeY, ThemeColors.CARD_SECONDARY, holeX, holeY + holeDiameter, ThemeColors.CARD_BG);
        g2.setPaint(holeGp);
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeDiameter, holeDiameter));

        g2.setColor(ThemeColors.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new Ellipse2D.Double(holeX, holeY, holeDiameter, holeDiameter));

        // Center total metric text
        g2.setColor(ThemeColors.TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String totalStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        int tx = holeX + (holeDiameter - fm.stringWidth(totalStr)) / 2;
        int ty = holeY + (holeDiameter / 2) + 1;
        g2.drawString(totalStr, tx, ty);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(ThemeColors.TEXT_MUTED);
        String labelStr = "TOTAL";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(labelStr, holeX + (holeDiameter - fmSub.stringWidth(labelStr)) / 2, ty + 12);
    }

    private void drawBarChart(Graphics2D g2) {
        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(10);
        if (maxVal == 0) maxVal = 10;

        int chartX = 45;
        int chartY = 55;
        int chartWidth = getWidth() - 65;
        int chartHeight = getHeight() - 105;

        // Dotted Horizontal Gridlines
        float[] dashPattern = {4, 4};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        g2.setColor(new Color(35, 46, 82, 160));

        for (int i = 1; i <= 3; i++) {
            int lineY = chartY + (chartHeight * i / 4);
            g2.drawLine(chartX, lineY, chartX + chartWidth, lineY);
        }

        // Solid Baseline Axis
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(ThemeColors.BORDER_COLOR);
        g2.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);

        int barWidth = Math.max(20, (chartWidth / Math.max(1, data.size())) - 16);
        int curX = chartX + 12;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String key = entry.getKey();
            int val = entry.getValue();
            int barHeight = Math.max(4, (int) ((double) val / maxVal * (chartHeight - 25)));
            int barY = chartY + chartHeight - barHeight;

            Color baseColor = colors.getOrDefault(key, ThemeColors.PRIMARY_BLUE);
            Color topColor = baseColor;
            Color botColor = new Color(
                    Math.max(0, baseColor.getRed() - 40),
                    Math.max(0, baseColor.getGreen() - 40),
                    Math.max(0, baseColor.getBlue() - 30)
            );

            // Bar Gradient Fill with Rounded Top
            GradientPaint barGrad = new GradientPaint(curX, barY, topColor, curX, barY + barHeight, botColor);
            g2.setPaint(barGrad);
            RoundRectangle2D.Float barShape = new RoundRectangle2D.Float(curX, barY, barWidth, barHeight, 8, 8);
            g2.fill(barShape);

            // Specular Highlight line across bar top
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(curX + 2, barY + 1, curX + barWidth - 3, barY + 1);

            // Count label floating above bar
            g2.setColor(ThemeColors.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String valStr = String.valueOf(val);
            FontMetrics fmVal = g2.getFontMetrics();
            g2.drawString(valStr, curX + (barWidth - fmVal.stringWidth(valStr)) / 2, barY - 4);

            // Category label beneath bar
            g2.setColor(ThemeColors.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String label = key.length() > 6 ? key.substring(0, 5) + "." : key;
            FontMetrics fmLbl = g2.getFontMetrics();
            g2.drawString(label, curX + (barWidth - fmLbl.stringWidth(label)) / 2, chartY + chartHeight + 16);

            curX += barWidth + 16;
        }
    }

    private void drawAccuracyDonut(Graphics2D g2) {
        int diameter = Math.min(125, getHeight() - 90);
        int x = (getWidth() - diameter) / 2;
        int y = 55;

        // Background Track with soft shadow
        g2.setColor(new Color(18, 24, 46));
        g2.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(x, y, diameter, diameter, 0, 360, Arc2D.OPEN));

        // Active Glowing Arc
        int angle = (int) Math.round((accuracyPercentage / 100.0) * 360);

        // Neon Glow effect behind arc
        g2.setColor(new Color(52, 211, 153, 50));
        g2.setStroke(new BasicStroke(21, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(x, y, diameter, diameter, 90, -angle, Arc2D.OPEN));

        // Core Gradient Arc
        GradientPaint arcGrad = new GradientPaint(
                x, y, ThemeColors.CYAN_ACCENT,
                x + diameter, y + diameter, ThemeColors.PRIORITY_LOW
        );
        g2.setPaint(arcGrad);
        g2.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(x, y, diameter, diameter, 90, -angle, Arc2D.OPEN));

        // Text inside donut
        g2.setColor(ThemeColors.TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String text = String.format("%.1f%%", accuracyPercentage);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (diameter - fm.stringWidth(text)) / 2;
        int ty = y + (diameter - fm.getHeight()) / 2 + fm.getAscent() - 4;
        g2.drawString(text, tx, ty);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(ThemeColors.CYAN_ACCENT);
        String sub = "AI MODEL ACCURACY";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(sub, x + (diameter - fmSub.stringWidth(sub)) / 2, ty + 15);

        g2.setFont(ThemeFonts.CAPTION);
        g2.setColor(ThemeColors.TEXT_MUTED);
        String footer = "High: 96%  •  Med: 91%  •  Low: 94%";
        FontMetrics fmFoot = g2.getFontMetrics();
        g2.drawString(footer, (getWidth() - fmFoot.stringWidth(footer)) / 2, y + diameter + 26);
    }
}

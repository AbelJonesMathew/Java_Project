package com.campusvoice.ui.splash;

import com.campusvoice.config.AppConfig;
import com.campusvoice.ui.login.LoginFrame;
import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern Java 2D Splash Screen featuring glowing ambient lighting,
 * smooth multi-stop linear gradient progress bar, and elegant typography.
 */
public class SplashScreen extends JFrame {
    private int progress = 0;
    private JLabel statusLabel;
    private JPanel progressPanel;

    public SplashScreen() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(680, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Java2DUtils.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                // 1. Deep cosmic dark gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(7, 10, 20), w, h, new Color(18, 25, 52));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 28, 28);

                // 2. Ambient neon glow circles (Fintech/Cosmic style)
                RadialGradientPaint rgp1 = new RadialGradientPaint(
                        w / 2f, 110, 200,
                        new float[]{0.0f, 0.5f, 1.0f},
                        new Color[]{new Color(99, 102, 241, 65), new Color(168, 85, 247, 25), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(rgp1);
                g2.fillOval(w / 2 - 200, -90, 400, 400);

                RadialGradientPaint rgp2 = new RadialGradientPaint(
                        w - 80, h - 80, 160,
                        new float[]{0.0f, 0.5f, 1.0f},
                        new Color[]{new Color(56, 189, 248, 45), new Color(99, 102, 241, 15), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(rgp2);
                g2.fillOval(w - 240, h - 240, 320, 320);

                // 3. Central Emblem Circle with Specular Ring
                int emblemD = 76;
                int emblemX = (w - emblemD) / 2;
                int emblemY = 38;

                Java2DUtils.drawGlow(g2, emblemX, emblemY, emblemD, emblemD, emblemD, ThemeColors.PRIMARY_BLUE, 6);

                GradientPaint emblemGp = new GradientPaint(emblemX, emblemY, ThemeColors.PRIMARY_BLUE, emblemX + emblemD, emblemY + emblemD, ThemeColors.PURPLE_ACCENT);
                g2.setPaint(emblemGp);
                g2.fillOval(emblemX, emblemY, emblemD, emblemD);

                // Specular top ring
                g2.setColor(new Color(255, 255, 255, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(emblemX, emblemY, emblemD, emblemD);

                // Monogram text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "CV";
                int tx = (w - fm.stringWidth(icon)) / 2;
                int ty = emblemY + (emblemD - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, tx, ty);

                // 4. Subtle Outer Border
                g2.setColor(ThemeColors.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 28, 28);

                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(132, 20, 10, 20));

        JLabel titleLabel = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel(AppConfig.TAGLINE, SwingConstants.CENTER);
        taglineLabel.setFont(ThemeFonts.TITLE_SMALL);
        taglineLabel.setForeground(ThemeColors.CYAN_ACCENT);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTagline = new JLabel(AppConfig.SECONDARY_TAGLINE, SwingConstants.CENTER);
        subTagline.setFont(ThemeFonts.CAPTION);
        subTagline.setForeground(ThemeColors.TEXT_MUTED);
        subTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(taglineLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subTagline);

        // Bottom progress panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 60, 36, 60));

        // Custom Java 2D Progress Bar Panel
        progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Java2DUtils.applyQualityHints(g2);

                int w = getWidth();
                int h = getHeight();

                // Track
                g2.setColor(new Color(20, 26, 48));
                RoundRectangle2D.Float track = new RoundRectangle2D.Float(0, 0, w, h, h, h);
                g2.fill(track);

                g2.setColor(ThemeColors.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(track);

                // Progress fill
                int fillW = (int) ((progress / 100.0) * w);
                if (fillW > 0) {
                    // Tip glow
                    Java2DUtils.drawGlow(g2, 0, 0, fillW, h, h, ThemeColors.CYAN_ACCENT, 3);

                    // Gradient fill
                    GradientPaint fillGp = new GradientPaint(
                            0, 0, ThemeColors.PRIMARY_BLUE,
                            fillW, 0, ThemeColors.CYAN_ACCENT
                    );
                    g2.setPaint(fillGp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));

                    // Specular highlight line
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.drawLine(2, 1, Math.max(2, fillW - 2), 1);
                }

                g2.dispose();
            }
        };
        progressPanel.setOpaque(false);
        progressPanel.setPreferredSize(new Dimension(540, 10));
        progressPanel.setMaximumSize(new Dimension(540, 10));
        progressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Initializing Autonomous Redressal Engine...", SwingConstants.CENTER);
        statusLabel.setFont(ThemeFonts.CAPTION);
        statusLabel.setForeground(ThemeColors.TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(progressPanel);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(statusLabel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void startLoading() {
        setVisible(true);
        Timer timer = new Timer(25, null);
        timer.addActionListener(e -> {
            progress += 2;
            if (progress == 20) {
                statusLabel.setText("Connecting to SJCET Autonomous Node...");
            } else if (progress == 50) {
                statusLabel.setText("Loading AI Multilingual Diagnostics Engine...");
            } else if (progress == 80) {
                statusLabel.setText("Verifying Security Credentials & Session Cache...");
            } else if (progress >= 100) {
                statusLabel.setText("Ready! Welcome to CampusVoice.");
            }
            progressPanel.repaint();

            if (progress >= 100) {
                timer.stop();
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });
        timer.start();
    }
}

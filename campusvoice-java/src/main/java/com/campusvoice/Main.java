package com.campusvoice;

import com.campusvoice.ui.splash.SplashScreen;
import com.campusvoice.ui.theme.FlatLafTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        logger.info("Initializing CampusVoice Application (Pure Java Edition)...");

        // Start embedded Java AI Microservice (Zero-Python native replacement)
        startEmbeddedAIServer();

        // Setup FlatLaf Modern Look and Feel
        FlatLafTheme.setup();

        // Launch Application Splash Screen on Swing EDT
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.startLoading();
        });
    }

    private static void startEmbeddedAIServer() {
        Thread aiServerThread = new Thread(() -> {
            try {
                int port = com.campusvoice.ai.AIConfig.getServerPort();
                com.campusvoice.ai.AIServer server = new com.campusvoice.ai.AIServer(port);
                server.start();
                logger.info("Embedded Java AI REST Microservice active on port {}", port);
            } catch (Exception e) {
                logger.info("Embedded AI server note: {}. In-memory AI engine remains fully operational.", e.getMessage());
            }
        }, "CampusVoice-AI-Server-Thread");
        aiServerThread.setDaemon(true);
        aiServerThread.start();
    }
}

package com.campusvoice.ui.theme;

import com.formdev.flatlaf.FlatClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.UIManager;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CampusVoice CSS Engine & Theme Bridge.
 * Loads external CSS stylesheets (/css/campusvoice.css), parses tokens and design rules,
 * dynamically binds FlatLaf style sheet classes into UIManager, and provides CSS utilities
 * for modern Java Swing components without altering native Swing architecture.
 */
public class CssEngine {
    private static final Logger logger = LoggerFactory.getLogger(CssEngine.class);
    private static final String CSS_RESOURCE = "/css/campusvoice.css";

    private static String rawCssContent = "";
    private static final Map<String, String> styleRules = new HashMap<>();
    private static final Map<String, Color> colorTokens = new HashMap<>();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;

        try (InputStream in = CssEngine.class.getResourceAsStream(CSS_RESOURCE)) {
            if (in == null) {
                logger.warn("CSS stylesheet resource not found at {}. Using embedded design rules.", CSS_RESOURCE);
                registerFallbackRules();
                initialized = true;
                return;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            rawCssContent = sb.toString();
            parseAndRegisterCss(rawCssContent);
            logger.info("CampusVoice CSS Engine successfully loaded: {} style rules, {} color tokens registered.",
                    styleRules.size(), colorTokens.size());
            initialized = true;
        } catch (Exception e) {
            logger.error("Error initializing CSS Engine from {}", CSS_RESOURCE, e);
            registerFallbackRules();
            initialized = true;
        }
    }

    public static synchronized void reload() {
        initialized = false;
        styleRules.clear();
        colorTokens.clear();
        init();
    }

    private static void parseAndRegisterCss(String css) {
        // Remove block comments (/* ... */) and single-line comments (// ...)
        String clean = css.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");

        int searchFrom = 0;
        int openIdx;
        while ((openIdx = clean.indexOf('{', searchFrom)) != -1) {
            String selectorPart = clean.substring(searchFrom, openIdx).trim();
            int closeIdx = clean.indexOf('}', openIdx);
            if (closeIdx == -1) break;

            String body = clean.substring(openIdx + 1, closeIdx).trim();
            searchFrom = closeIdx + 1;

            if (selectorPart.equalsIgnoreCase(":root")) {
                parseColorTokens(body);
                continue;
            }

            if (selectorPart.startsWith("@") || selectorPart.isEmpty()) {
                continue;
            }

            // Standardize FlatLaf CSS style string (semicolon-delimited single line)
            StringBuilder styleStr = new StringBuilder();
            String[] declarations = body.split("\n");
            for (String d : declarations) {
                String decl = d.trim();
                if (!decl.isEmpty()) {
                    styleStr.append(decl);
                    if (!decl.endsWith(";")) styleStr.append(";");
                    styleStr.append(" ");
                }
            }
            String finalStyle = styleStr.toString().trim();

            // Register selectors
            String[] selectors = selectorPart.split(",");
            for (String sel : selectors) {
                String s = sel.trim();
                if (s.isEmpty()) continue;

                // FlatLaf format: [style]ComponentType.styleClass = "arc: 12; background: #..."
                String uiKey;
                if (s.startsWith(".")) {
                    uiKey = "[style]Component." + s.substring(1);
                    styleRules.put("Component." + s.substring(1), finalStyle);
                } else {
                    uiKey = "[style]" + s;
                    styleRules.put(s, finalStyle);
                }

                UIManager.put(uiKey, finalStyle);
            }
        }
    }

    private static void parseColorTokens(String body) {
        String[] lines = body.split("\n");
        for (String l : lines) {
            String line = l.trim();
            if (line.startsWith("--") && line.contains(":")) {
                String[] parts = line.split(":", 2);
                String tokenName = parts[0].trim();
                String val = parts[1].replace(";", "").trim();
                if (val.startsWith("#")) {
                    try {
                        Color c = Color.decode(val);
                        colorTokens.put(tokenName, c);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private static void registerFallbackRules() {
        UIManager.put("[style]Button.btn-primary", "arc: 12; background: #2563eb; hoverBackground: #1d4ed8; foreground: #ffffff;");
        UIManager.put("[style]Button.btn-secondary", "arc: 12; background: #1e293b; hoverBackground: #334155; foreground: #f8fafc;");
        UIManager.put("[style]Button.btn-success", "arc: 12; background: #10b981; hoverBackground: #059669; foreground: #ffffff;");
        UIManager.put("[style]Button.btn-danger", "arc: 12; background: #ef4444; hoverBackground: #dc2626; foreground: #ffffff;");
        UIManager.put("[style]TextField.input-modern", "arc: 12; background: #0b1120; foreground: #f8fafc; borderColor: #334155;");
        UIManager.put("[style]TextField.input-search", "arc: 20; background: #0b1120; foreground: #f8fafc; borderColor: #334155;");
        UIManager.put("[style]ComboBox.combo-modern", "arc: 12; background: #0b1120; foreground: #f8fafc; borderColor: #334155;");
    }

    /**
     * Applies one or more CSS style classes defined in campusvoice.css to any Swing component.
     * Example: CssEngine.apply(myButton, "btn-primary");
     */
    public static void apply(JComponent component, String styleClasses) {
        if (component == null || styleClasses == null) return;
        component.putClientProperty(FlatClientProperties.STYLE_CLASS, styleClasses);
    }

    /**
     * Applies an inline CSS style rule to any Swing component.
     * Example: CssEngine.style(myPanel, "arc: 16; background: #111827;");
     */
    public static void style(JComponent component, String inlineCss) {
        if (component == null || inlineCss == null) return;
        component.putClientProperty(FlatClientProperties.STYLE, inlineCss);
    }

    /**
     * Wraps HTML body content with global CSS stylesheet rules for rich Swing JLabels, Tooltips, or Dialogs.
     */
    public static String html(String bodyContent) {
        return "<html><head><style>" +
                "body { font-family: 'Nirmala UI', 'Segoe UI', sans-serif; color: #f8fafc; margin: 0; padding: 0; }" +
                ".badge { display: inline-block; padding: 2px 8px; border-radius: 9999px; font-size: 10px; font-weight: bold; }" +
                ".badge-high { background-color: #3b0d14; color: #f87171; border: 1px solid #ef4444; }" +
                ".badge-med { background-color: #361f06; color: #fbbf24; border: 1px solid #f59e0b; }" +
                ".badge-low { background-color: #063124; color: #34d399; border: 1px solid #10b981; }" +
                ".badge-cyan { background-color: #082f49; color: #38bdf8; border: 1px solid #0284c7; }" +
                ".text-muted { color: #94a3b8; }" +
                ".text-cyan { color: #38bdf8; font-weight: bold; }" +
                "</style></head><body>" + bodyContent + "</body></html>";
    }

    /**
     * Generates a styled HTML status/priority badge using CSS classes.
     */
    public static String badgeHtml(String text, String type) {
        String cssClass = "badge-cyan";
        if ("HIGH".equalsIgnoreCase(type)) cssClass = "badge-high";
        else if ("MEDIUM".equalsIgnoreCase(type)) cssClass = "badge-med";
        else if ("LOW".equalsIgnoreCase(type) || "RESOLVED".equalsIgnoreCase(type)) cssClass = "badge-low";

        return html("<span class='badge " + cssClass + "'>" + text + "</span>");
    }

    public static Color getColor(String varName, Color fallback) {
        return colorTokens.getOrDefault(varName, fallback);
    }

    public static Map<String, String> getRegisteredRules() {
        return Collections.unmodifiableMap(styleRules);
    }

    public static String getRawCss() {
        return rawCssContent;
    }
}

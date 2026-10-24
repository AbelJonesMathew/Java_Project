package com.campusvoice.ui.theme;

import java.awt.Font;

public class ThemeFonts {
    public static final String FONT_FAMILY = resolveFontFamily();

    public static final Font TITLE_LARGE = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font TITLE_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font TITLE_SMALL = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font BODY_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font BADGE_FONT = new Font(FONT_FAMILY, Font.BOLD, 11);

    /**
     * Resolves the optimal font family supporting crisp Latin typography as well as
     * native South Asian Indic scripts (Malayalam, Hindi, Tamil, Telugu, Kannada).
     * "Nirmala UI" is the official Microsoft Windows Segoe-compatible typeface for Indian scripts.
     */
    public static String resolveFontFamily() {
        String testIndic = "മലയാളം हिन्दी தமிழ் తెలుగు ಕನ್ನಡ";
        String[] candidateFonts = {
            "Nirmala UI",
            "Nirmala Text",
            "Noto Sans Malayalam",
            "Kartika",
            Font.SANS_SERIF,
            Font.DIALOG,
            "Segoe UI"
        };

        try {
            for (String cand : candidateFonts) {
                Font f = new Font(cand, Font.PLAIN, 13);
                if (f.canDisplayUpTo(testIndic) == -1) {
                    return cand;
                }
            }
        } catch (Throwable ignored) {}

        return "Nirmala UI";
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }
}


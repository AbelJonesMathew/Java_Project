package com.campusvoice.ui.theme;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

public class FlatLafTheme {
    private static final Logger logger = LoggerFactory.getLogger(FlatLafTheme.class);

    public static void setup() {
        try {
            // Enable hardware acceleration, animation, and custom window decorations
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            System.setProperty("flatlaf.animation", "true");

            // Setup ultra-modern macOS-style FlatMacDarkLaf with fallback to FlatDarkLaf
            boolean lafSet = false;
            try {
                lafSet = FlatMacDarkLaf.setup();
            } catch (Throwable t) {
                logger.warn("FlatMacDarkLaf setup fallback note: {}", t.getMessage());
            }

            if (!lafSet) {
                FlatDarkLaf.setup();
            }

            // Initialize CSS Engine & Load external /css/campusvoice.css style sheets
            CssEngine.init();

            // Enable Native FlatLaf Custom Window Decorations for all Windows & Dialogs
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);

            // Modern Window TitlePane styling (Integrated with deep dark slate palette)
            UIManager.put("TitlePane.unifiedBackground", true);
            UIManager.put("TitlePane.background", ThemeColors.BACKGROUND);
            UIManager.put("TitlePane.foreground", Color.WHITE);
            UIManager.put("TitlePane.inactiveForeground", ThemeColors.TEXT_MUTED);
            UIManager.put("TitlePane.centerTitle", false);
            UIManager.put("TitlePane.menuBarEmbedded", true);
            UIManager.put("TitlePane.buttonHoverBackground", new Color(30, 41, 59));
            UIManager.put("TitlePane.buttonPressedBackground", new Color(51, 65, 85));
            UIManager.put("TitlePane.closeHoverBackground", new Color(239, 68, 68));
            UIManager.put("TitlePane.closeHoverForeground", Color.WHITE);
            UIManager.put("TitlePane.closePressedBackground", new Color(185, 28, 28));
            UIManager.put("TitlePane.closePressedForeground", Color.WHITE);
            UIManager.put("TitlePane.font", new Font(ThemeFonts.FONT_FAMILY, Font.BOLD, 12));

            // Modern Geometry & Arcs (Sophisticated curved borders)
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("ScrollBar.trackArc", 10);
            UIManager.put("TabbedPane.tabArc", 12);
            UIManager.put("ComboBox.arc", 12);
            UIManager.put("Spinner.arc", 12);
            UIManager.put("PopupMenu.arc", 12);
            UIManager.put("Component.arrowType", "chevron");
            UIManager.put("ComboBox.buttonStyle", "chevron");
            UIManager.put("ComboBox.padding", new Insets(5, 12, 5, 12));

            // Modern Component Focus Rings & Accent Borders
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Component.focusColor", new Color(37, 99, 235, 180));
            UIManager.put("Component.borderColor", ThemeColors.BORDER_COLOR);

            // Fonts - Crisp modern typography with multilingual Indic support
            Font baseFont = ThemeFonts.BODY_REGULAR;
            UIManager.put("defaultFont", baseFont);
            UIManager.put("Label.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("Button.font", ThemeFonts.BODY_BOLD);
            UIManager.put("Table.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("TableHeader.font", ThemeFonts.BODY_BOLD);
            UIManager.put("TextArea.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("TextField.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("PasswordField.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("ComboBox.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("TabbedPane.font", ThemeFonts.BODY_BOLD);
            UIManager.put("List.font", ThemeFonts.BODY_REGULAR);
            UIManager.put("ToolTip.font", ThemeFonts.CAPTION);

            // Global Dark Enterprise Surfaces
            UIManager.put("Panel.background", ThemeColors.BACKGROUND);
            UIManager.put("Panel.foreground", ThemeColors.TEXT_DARK);

            // Buttons
            UIManager.put("Button.background", ThemeColors.CARD_SECONDARY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", ThemeColors.CARD_HOVER);
            UIManager.put("Button.default.background", ThemeColors.PRIMARY_BLUE);
            UIManager.put("Button.default.hoverBackground", ThemeColors.PRIMARY_HOVER);
            UIManager.put("Button.shadowWidth", 2);
            UIManager.put("Button.shadowColor", new Color(0, 0, 0, 70));

            // Tables
            UIManager.put("Table.background", ThemeColors.CARD_BG);
            UIManager.put("Table.foreground", ThemeColors.TEXT_DARK);
            UIManager.put("Table.alternateRowColor", new Color(20, 28, 48));
            UIManager.put("Table.selectionBackground", ThemeColors.PRIMARY_BLUE);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(51, 65, 85, 100));
            UIManager.put("Table.rowHeight", 42);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.selectionArc", 8);
            UIManager.put("Table.selectionInsets", new Insets(1, 2, 1, 2));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.background", ThemeColors.SIDEBAR_BG);
            UIManager.put("TableHeader.foreground", ThemeColors.TEXT_MUTED);
            UIManager.put("TableHeader.separatorColor", ThemeColors.BORDER_COLOR);
            UIManager.put("TableHeader.bottomSeparatorColor", ThemeColors.BORDER_COLOR);
            UIManager.put("TableHeader.height", 38);

            // Text Inputs & Areas
            UIManager.put("TextField.background", ThemeColors.INPUT_BG);
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("TextField.borderColor", ThemeColors.BORDER_COLOR);
            UIManager.put("TextField.placeholderForeground", new Color(148, 163, 184, 150));
            UIManager.put("TextArea.background", ThemeColors.INPUT_BG);
            UIManager.put("TextArea.foreground", Color.WHITE);
            UIManager.put("TextArea.caretForeground", Color.WHITE);
            UIManager.put("TextArea.placeholderForeground", new Color(148, 163, 184, 150));
            UIManager.put("PasswordField.background", ThemeColors.INPUT_BG);
            UIManager.put("PasswordField.foreground", Color.WHITE);
            UIManager.put("PasswordField.caretForeground", Color.WHITE);
            UIManager.put("PasswordField.borderColor", ThemeColors.BORDER_COLOR);
            UIManager.put("PasswordField.placeholderForeground", new Color(148, 163, 184, 150));
            UIManager.put("ComboBox.background", ThemeColors.INPUT_BG);
            UIManager.put("ComboBox.foreground", Color.WHITE);
            UIManager.put("ComboBox.selectionBackground", ThemeColors.PRIMARY_BLUE);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("ComboBox.borderColor", ThemeColors.BORDER_COLOR);

            // ScrollPanes & Scrollbars
            UIManager.put("ScrollPane.background", ThemeColors.BACKGROUND);
            UIManager.put("ScrollPane.smoothScrolling", true);
            UIManager.put("ScrollBar.track", ThemeColors.BACKGROUND);
            UIManager.put("ScrollBar.thumb", new Color(71, 85, 105, 180));
            UIManager.put("ScrollBar.thumbHover", new Color(100, 116, 139, 230));
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("ScrollBar.trackArc", 10);
            UIManager.put("ScrollBar.showButtons", false);

            // TabbedPane & OptionPane
            UIManager.put("TabbedPane.tabType", "underlined");
            UIManager.put("TabbedPane.underlineColor", ThemeColors.PRIMARY_BLUE);
            UIManager.put("TabbedPane.underlineHeight", 3);
            UIManager.put("TabbedPane.background", ThemeColors.BACKGROUND);
            UIManager.put("TabbedPane.foreground", ThemeColors.TEXT_MUTED);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabHeight", 38);

            // Popups & OptionPane
            UIManager.put("Popup.dropShadowPainted", true);
            UIManager.put("PopupMenu.background", new Color(17, 24, 39));
            UIManager.put("PopupMenu.borderColor", ThemeColors.BORDER_COLOR);
            UIManager.put("MenuItem.selectionBackground", ThemeColors.PRIMARY_BLUE);
            UIManager.put("MenuItem.selectionForeground", Color.WHITE);
            UIManager.put("ToolTip.background", new Color(17, 24, 39));
            UIManager.put("ToolTip.foreground", Color.WHITE);
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(ThemeColors.BORDER_COLOR, 1));
            UIManager.put("OptionPane.background", ThemeColors.CARD_BG);
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            UIManager.put("OptionPane.border", BorderFactory.createEmptyBorder(20, 24, 20, 24));

            // Progress Bar
            UIManager.put("ProgressBar.background", new Color(20, 26, 48));
            UIManager.put("ProgressBar.foreground", ThemeColors.PRIMARY_BLUE);
            UIManager.put("ProgressBar.height", 8);

            logger.info("FlatLaf Enterprise macOS Dark Look & Feel successfully configured with native window decorations.");
        } catch (Exception e) {
            logger.error("Failed to initialize FlatLaf dark look and feel", e);
        }
    }
}

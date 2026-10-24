package com.campusvoice.ui.theme;

import com.formdev.flatlaf.FlatClientProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

import static org.junit.jupiter.api.Assertions.*;

public class CssEngineTest {

    @BeforeAll
    public static void setup() {
        CssEngine.reload();
    }

    @Test
    public void testCssEngineInitialized() {
        assertNotNull(CssEngine.getRawCss());
        assertFalse(CssEngine.getRawCss().isEmpty());
    }

    @Test
    public void testFlatLafStyleRulesRegistered() {
        assertNotNull(UIManager.getString("[style]Button.btn-primary"), "Button.btn-primary should be registered in UIManager");
        assertTrue(UIManager.getString("[style]Button.btn-primary").contains("background: #2563eb"));

        assertNotNull(UIManager.getString("[style]TextField.input-modern"), "TextField.input-modern should be registered in UIManager");
        assertTrue(UIManager.getString("[style]TextField.input-modern").contains("arc: 12"));
    }

    @Test
    public void testApplyStyleClass() {
        JButton btn = new JButton("Test");
        CssEngine.apply(btn, "btn-primary");
        assertEquals("btn-primary", btn.getClientProperty(FlatClientProperties.STYLE_CLASS));

        JTextField tf = new JTextField();
        CssEngine.apply(tf, "input-search");
        assertEquals("input-search", tf.getClientProperty(FlatClientProperties.STYLE_CLASS));
    }

    @Test
    public void testHtmlWrapper() {
        String html = CssEngine.html("<div class='test'>Content</div>");
        assertNotNull(html);
        assertTrue(html.contains("<style>"));
        assertTrue(html.contains(".badge-high"));
        assertTrue(html.contains("Content"));
    }

    @Test
    public void testBadgeHtml() {
        String badge = CssEngine.badgeHtml("HIGH PRIORITY", "HIGH");
        assertNotNull(badge);
        assertTrue(badge.contains("badge-high"));
        assertTrue(badge.contains("HIGH PRIORITY"));
    }
}

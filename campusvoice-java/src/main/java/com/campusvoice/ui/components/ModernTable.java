package com.campusvoice.ui.components;

import com.campusvoice.ui.theme.Java2DUtils;
import com.campusvoice.ui.theme.ThemeColors;
import com.campusvoice.ui.theme.ThemeFonts;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

/**
 * Enterprise-grade Java 2D Modern Table with:
 * - Comfortable 42px row height and generous cell padding
 * - Dynamic row hover tracking with subtle translucent highlight
 * - Sophisticated alternating row fills
 * - Automated column rendering (Ticket Code pills, Priority pills, Status pills, Assigned Officer tags)
 * - Professional table header styling with 11px bold uppercase typography
 */
public class ModernTable extends JTable {
    private int hoveredRow = -1;

    public ModernTable(TableModel model) {
        super(model);
        initModernTable();
    }

    private void initModernTable() {
        setRowHeight(42);
        setFont(ThemeFonts.BODY_REGULAR);
        setBackground(ThemeColors.CARD_BG);
        setForeground(ThemeColors.TEXT_DARK);
        setSelectionBackground(new Color(37, 99, 235, 200));
        setSelectionForeground(Color.WHITE);
        setGridColor(new Color(51, 65, 85, 90));
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setFillsViewportHeight(true);
        com.campusvoice.ui.theme.CssEngine.apply(this, "table-modern");

        // Professional Table Header
        JTableHeader header = getTableHeader();
        header.setFont(ThemeFonts.font(Font.BOLD, 11));
        header.setBackground(ThemeColors.SIDEBAR_BG);
        header.setForeground(ThemeColors.TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        com.campusvoice.ui.theme.CssEngine.apply(header, "header-modern");
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(ThemeFonts.font(Font.BOLD, 11));
                lbl.setForeground(ThemeColors.TEXT_MUTED);
                lbl.setBackground(ThemeColors.SIDEBAR_BG);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)
                ));
                lbl.setText(value != null ? value.toString().toUpperCase() : "");
                return lbl;
            }
        });

        // Row hover interaction
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        if (!isRowSelected(row)) {
            if (row == hoveredRow) {
                c.setBackground(new Color(30, 49, 78)); // Glowing subtle blue hover
            } else if (row % 2 == 1) {
                c.setBackground(new Color(25, 34, 49)); // Translucent alternating frosted row
            } else {
                c.setBackground(ThemeColors.CARD_BG); // Frosted glass row
            }
        }
        if (isRowSelected(row)) c.setBackground(getSelectionBackground());
        if (c instanceof JComponent component) component.setOpaque(true);
        return c;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (getColumnModel() != null && column >= 0 && column < getColumnCount()) {
            TableCellRenderer customRenderer = getColumnModel().getColumn(column).getCellRenderer();
            if (customRenderer != null) {
                return customRenderer;
            }
        }

        String colName = getColumnName(column);
        if (colName != null) {
            String colLower = colName.toLowerCase();
            if (colLower.contains("priority")) {
                return (table, value, isSelected, hasFocus, r, c) -> {
                    String p = value != null ? value.toString() : "LOW";
                    JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                    wrap.setOpaque(false);
                    wrap.add(new PriorityBadge(p));
                    return wrap;
                };
            }
            if (colLower.contains("status")) {
                return (table, value, isSelected, hasFocus, r, c) -> {
                    String s = value != null ? value.toString() : "SUBMITTED";
                    JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                    wrap.setOpaque(false);
                    wrap.add(new StatusBadge(s));
                    return wrap;
                };
            }
            if (colLower.contains("code") || colLower.contains("ticket")) {
                return (table, value, isSelected, hasFocus, r, c) -> {
                    String code = value != null ? value.toString() : "";
                    return createCodePillComponent(code, isSelected);
                };
            }
            if (colLower.contains("officer") || colLower.contains("assigned")) {
                return (table, value, isSelected, hasFocus, r, c) -> {
                    String officer = value != null ? value.toString() : "";
                    return createOfficerComponent(officer, isSelected);
                };
            }
        }

        // Standard text renderer with generous padding
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                l.setForeground(isSelected ? Color.WHITE : ThemeColors.TEXT_DARK);
                l.setFont(ThemeFonts.BODY_REGULAR);
                return l;
            }
        };
    }

    private Component createCodePillComponent(String code, boolean isSelected) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Java2DUtils.applyQualityHints(g2);
                int h = 26;
                int y = (getHeight() - h) / 2;
                int w = Math.min(getWidth() - 28, 80);
                g2.setColor(new Color(15, 23, 42));
                g2.fillRoundRect(14, y, w, h, 6, 6);
                g2.setColor(new Color(56, 189, 248, 80));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(14, y, w, h, 6, 6);
                g2.dispose();
            }
        };
        p.setOpaque(false);

        JLabel codeLbl = new JLabel(code);
        codeLbl.setFont(new Font("Consolas", Font.BOLD, 12));
        codeLbl.setForeground(ThemeColors.CYAN_ACCENT);
        codeLbl.setBorder(new EmptyBorder(0, 6, 0, 6));
        p.add(codeLbl);
        return p;
    }

    private Component createOfficerComponent(String officer, boolean isSelected) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        p.setOpaque(false);

        if (officer == null || officer.isBlank() || "Unassigned".equalsIgnoreCase(officer.trim())) {
            RoundedPanel unassignedPill = new RoundedPanel(8, new Color(51, 65, 85, 120));
            unassignedPill.setBorderColor(new Color(100, 116, 139, 100));
            unassignedPill.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 3));
            JLabel uLbl = new JLabel("Unassigned");
            uLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            uLbl.setForeground(ThemeColors.TEXT_MUTED);
            unassignedPill.add(uLbl);
            p.add(unassignedPill);
        } else {
            JLabel offLbl = new JLabel(officer);
            offLbl.setFont(ThemeFonts.BODY_REGULAR);
            offLbl.setForeground(isSelected ? Color.WHITE : ThemeColors.TEXT_DARK);
            p.add(offLbl);
        }
        return p;
    }

    /**
     * Helper to wrap a ModernTable in an enterprise styled JScrollPane.
     */
    public static JScrollPane createModernScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(true);
        sp.setBackground(ThemeColors.CARD_BG);
        sp.getViewport().setBackground(ThemeColors.CARD_BG);
        sp.putClientProperty(FlatClientProperties.SCROLL_PANE_SMOOTH_SCROLLING, true);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 25), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        return sp;
    }
}

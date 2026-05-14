import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class UITheme {

    public static final Color BG_DARK = new Color(245, 247, 250);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_CARD2 = new Color(233, 240, 248);
    public static final Color ACCENT = new Color(42, 96, 150);
    public static final Color ACCENT_GREEN = new Color(40, 140, 80);
    public static final Color ACCENT_RED = new Color(185, 46, 46);
    public static final Color ACCENT_AMBER = new Color(180, 110, 0);
    public static final Color TEXT_PRIMARY = new Color(30, 30, 30);
    public static final Color TEXT_MUTED = new Color(100, 110, 125);
    public static final Color BORDER = new Color(200, 210, 220);
    public static final Color NAV_BG = new Color(42, 96, 150);
    public static final Color NAV_FG = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(240, 244, 248);
    public static final Color SIDEBAR_SEL = new Color(42, 96, 150);
    public static final Color SIDEBAR_TEXT = new Color(30, 50, 80);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16));
    }

    public static Border mattePad(int top, int bottom) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, top, 0, BORDER),
                BorderFactory.createEmptyBorder(bottom, 20, bottom, 20));
    }

    public static Border sectionBorder(int v, int h) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(v, h, v, h));
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        btn.addMouseListener(hover(btn, ACCENT, ACCENT.darker()));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT_RED);
        btn.setForeground(Color.WHITE);
        btn.addMouseListener(hover(btn, ACCENT_RED, ACCENT_RED.darker()));
        return btn;
    }

    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(ACCENT);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btn.addMouseListener(hover(btn, Color.WHITE, BG_CARD2));
        return btn;
    }

    private static MouseAdapter hover(JButton btn, Color normal, Color hovered) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hovered);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(normal);
            }
        };
    }

    public static JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(Color.WHITE);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JPasswordField styledPassword(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(Color.WHITE);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    public static void styleTable(JTable t) {
        t.setBackground(Color.WHITE);
        t.setForeground(TEXT_PRIMARY);
        t.setGridColor(BORDER);
        t.setFont(FONT_BODY);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(42, 96, 150, 40));
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setIntercellSpacing(new Dimension(0, 1));
        // Zebra striping via renderer — handled by styleTableZebra()
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(42, 96, 150)); // Koha blue header
        h.setForeground(Color.WHITE);
        h.setFont(FONT_SMALL);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        h.setReorderingAllowed(false);
    }

    public static void styleTableZebra(JTable t) {
        styleTable(t);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setOpaque(true);
                if (sel) {
                    setBackground(new Color(42, 96, 150, 50));
                    setForeground(TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 249, 253));
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
    }

    public static JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return sp;
    }
}

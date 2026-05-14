import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MyAccountPanel extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final User user;
    private final Library library;
    private DefaultTableModel borrowedModel, historyModel, reservedModel;
    private JLabel fineLabel;

    public MyAccountPanel(User user, Library library) {
        this.user = user; this.library = library;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(buildBorrowedSection(), BorderLayout.CENTER);
        add(buildBottomSection(),   BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildBorrowedSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.add(UITheme.label("📚 Currently Borrowed", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), BorderLayout.WEST);
        fineLabel = UITheme.label("", UITheme.FONT_BODY, UITheme.ACCENT_RED);
        header.add(fineLabel, BorderLayout.EAST);
        borrowedModel = nonEditable("Title", "ISBN", "Due Date", "Days Left", "Status");
        JTable t = new JTable(borrowedModel);
        UITheme.styleTable(t);
        t.getColumnModel().getColumn(0).setPreferredWidth(200); t.getColumnModel().getColumn(0).setMinWidth(100);
        t.getColumnModel().getColumn(1).setPreferredWidth(80);  t.getColumnModel().getColumn(1).setMinWidth(65);
        t.getColumnModel().getColumn(2).setPreferredWidth(110); t.getColumnModel().getColumn(2).setMinWidth(90);
        t.getColumnModel().getColumn(3).setPreferredWidth(70);  t.getColumnModel().getColumn(3).setMinWidth(55);
        t.getColumnModel().getColumn(4).setPreferredWidth(100); t.getColumnModel().getColumn(4).setMinWidth(80);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.getColumnModel().getColumn(4).setCellRenderer(new DueDateRenderer());
        t.getColumnModel().getColumn(2).setCellRenderer(new DueDateRenderer());
        p.add(header, BorderLayout.NORTH);
        p.add(UITheme.styledScroll(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSection(String heading, DefaultTableModel model, int... colWidths) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        p.add(UITheme.label(heading, UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), BorderLayout.NORTH);
        JTable t = new JTable(model);
        UITheme.styleTable(t);
        // FIX: apply column widths so text stays inside cells
        for (int i = 0; i < colWidths.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            t.getColumnModel().getColumn(i).setMinWidth(Math.max(50, colWidths[i] / 2));
        }
        if (colWidths.length > 0) t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        p.add(UITheme.styledScroll(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBottomSection() {
        historyModel  = nonEditable("Title", "ISBN", "Returned On");
        reservedModel = nonEditable("Title", "ISBN", "Queue Position");
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setPreferredSize(new Dimension(0, 220));
        p.add(buildSection("🔔 My Reservations", reservedModel, 200, 80, 110));
        p.add(buildSection("🕓 Borrowing History", historyModel, 200, 80, 110));
        return p;
    }

    private DefaultTableModel nonEditable(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public void refresh() {
        borrowedModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Map.Entry<String, LocalDate> e : user.issuedBooks.entrySet()) {
            LocalDate due = e.getValue();
            Book b = library.getBook(e.getKey());
            String title = b != null ? b.getTitle() : e.getKey();
            long days = due.toEpochDay() - today.toEpochDay();
            String status = days < 0 ? "⚠ OVERDUE" : days == 0 ? "⚠ DUE TODAY" : days <= 2 ? "⚡ DUE SOON" : "✓ On time";
            borrowedModel.addRow(new Object[]{title, e.getKey(), due.format(FMT),
                days < 0 ? days + "d" : "+" + days + "d", status});
        }
        double fine = user.calculateFine();
        fineLabel.setText(fine > 0 ? String.format("⚠ Fine due: ৳ %.0f", fine) : "");

        historyModel.setRowCount(0);
        for (int i = user.borrowingHistory.size() - 1; i >= 0; i--) {
            String[] parts = user.borrowingHistory.get(i).split("\\|");
            if (parts.length == 3) historyModel.addRow(new Object[]{parts[1], parts[0], parts[2]});
        }

        reservedModel.setRowCount(0);
        for (Book b : library.getAllBooks()) {
            int pos = b.getReservedBy().indexOf(user.email);
            if (pos >= 0) reservedModel.addRow(new Object[]{b.getTitle(), b.getIsbn(), "#" + (pos + 1)});
        }
    }

    private static class DueDateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? new Color(88, 166, 255, 50) : UITheme.BG_CARD);
            String s = t.getModel().getValueAt(row, 4) == null ? "" : t.getModel().getValueAt(row, 4).toString();
            if (s.contains("OVERDUE") || s.contains("DUE TODAY")) {
                lbl.setForeground(UITheme.ACCENT_RED); lbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
            } else if (s.contains("DUE SOON")) {
                lbl.setForeground(UITheme.ACCENT_AMBER); lbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
            } else {
                lbl.setForeground(UITheme.ACCENT_GREEN); lbl.setFont(UITheme.FONT_BODY);
            }
            return lbl;
        }
    }
}

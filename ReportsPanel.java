import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.*;

public class ReportsPanel extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final Library library;
    private final HashMap<String, User> userDb;

    public ReportsPanel(Library library, HashMap<String, User> userDb) {
        this.library = library;
        this.userDb = userDb;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);

        tabs.addTab("📋 Overdue Books", buildOverdueTab());
        tabs.addTab("💰 Fine Summary", buildFineTab());
        tabs.addTab("📈 Most Borrowed", buildPopularTab());
        tabs.addTab("👤 Member Activity", buildMemberTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildOverdueTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        DefaultTableModel model = nonEditable("Member Email", "Book Title", "ISBN", "Due Date", "Days Overdue",
                "Fine (৳)");
        LocalDate today = LocalDate.now();
        for (User u : userDb.values()) {
            if (u.isAdmin || u.issuedBooks == null)
                continue;
            for (Map.Entry<String, LocalDate> e : u.issuedBooks.entrySet()) {
                long days = today.toEpochDay() - e.getValue().toEpochDay();
                if (days <= 0)
                    continue;
                Book b = library.getBook(e.getKey());
                model.addRow(new Object[] {
                        u.email,
                        b != null ? b.getTitle() : e.getKey(),
                        e.getKey(),
                        e.getValue().format(FMT),
                        days,
                        String.format("%.0f", days * LibraryConfig.FINE_PER_DAY)
                });
            }
        }

        JTable table = new JTable(model);
        UITheme.styleTableZebra(table);

        JLabel summary = UITheme.label(
                "Total overdue: " + model.getRowCount() + " book(s)", UITheme.FONT_SMALL, UITheme.ACCENT_RED);

        p.add(summary, BorderLayout.NORTH);
        p.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFineTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        DefaultTableModel model = nonEditable("Member", "Email", "Outstanding Fine (৳)", "Total Paid (৳)");
        double totalOut = 0;
        for (User u : userDb.values()) {
            if (u.isAdmin)
                continue;
            double fine = u.calculateFine();
            totalOut += fine;
            if (fine > 0 || u.finePaid > 0) {
                model.addRow(new Object[] {
                        u.getDisplayName(), u.email,
                        String.format("%.0f", fine),
                        String.format("%.0f", u.finePaid)
                });
            }
        }

        JTable table = new JTable(model);
        UITheme.styleTableZebra(table);

        JLabel summary = UITheme.label(
                "Total outstanding fines: ৳" + String.format("%.0f", totalOut),
                UITheme.FONT_SMALL, UITheme.ACCENT_RED);

        p.add(summary, BorderLayout.NORTH);
        p.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPopularTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        Map<String, Long> counts = new HashMap<>();
        for (User u : userDb.values()) {
            if (u.borrowingHistory == null)
                continue;
            for (String entry : u.borrowingHistory) {
                String isbn = entry.split("\\|")[0];
                counts.merge(isbn, 1L, Long::sum);
            }
        }

        DefaultTableModel model = nonEditable("#", "Title", "Author", "ISBN", "Times Borrowed");
        java.util.List<Map.Entry<String, Long>> sorted = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<String, Long> e : sorted) {
            Book b = library.getBook(e.getKey());
            model.addRow(new Object[] {
                    rank++,
                    b != null ? b.getTitle() : e.getKey(),
                    b != null ? b.getAuthor() : "—",
                    e.getKey(),
                    e.getValue()
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTableZebra(table);

        JLabel summary = UITheme.label(
                "Based on borrowing history across all members", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);

        p.add(summary, BorderLayout.NORTH);
        p.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMemberTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        DefaultTableModel model = nonEditable("Member", "Email", "Currently Borrowed", "Total Borrowed (History)",
                "Borrow Limit", "Fine (৳)");
        for (User u : userDb.values()) {
            if (u.isAdmin)
                continue;
            int historyCount = u.borrowingHistory != null ? u.borrowingHistory.size() : 0;
            model.addRow(new Object[] {
                    u.getDisplayName(), u.email,
                    u.issuedBooks.size(),
                    historyCount,
                    u.maxBorrowLimit,
                    String.format("%.0f", u.calculateFine())
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTableZebra(table);

        JLabel summary = UITheme.label(
                "Total members: " + model.getRowCount(), UITheme.FONT_SMALL, UITheme.TEXT_MUTED);

        p.add(summary, BorderLayout.NORTH);
        p.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        return p;
    }

    private DefaultTableModel nonEditable(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }
}

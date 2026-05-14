import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DashboardPanel extends JPanel {
    private final Library library;
    private final User user;
    private final HashMap<String, User> userDb;
    private JLabel totalBooksVal, issuedVal, fineVal, categoryVal;

    public DashboardPanel(Library library, User user, HashMap<String, User> userDb) {
        this.library = library; this.user = user; this.userDb = userDb;
        setBackground(UITheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        setLayout(new FlowLayout(FlowLayout.LEFT, 32, 0));
        totalBooksVal = addStat("📚 Collection", String.valueOf(library.getTotalBooks()));
        issuedVal     = addStat("📥 Borrowed",   borrowedText());
        categoryVal   = addStat("🏷 Top Category", library.getMostPopularCategory());
        fineVal       = addStat("💰 Fine", fineText());
    }

    private String borrowedText() {
        if (user.isAdmin) {
            int total = userDb.values().stream()
                .filter(u -> !u.isAdmin)
                .mapToInt(u -> u.issuedBooks.size())
                .sum();
            return String.valueOf(total);
        }
        return String.valueOf(user.issuedBooks.size());
    }

    private double fineValue() {
        if (user.isAdmin) {
            return userDb.values().stream()
                .filter(u -> !u.isAdmin)
                .mapToDouble(User::calculateFine)
                .sum();
        }
        return user.calculateFine();
    }

    private String fineText() {
        double fine = fineValue();
        return fine > 0 ? String.format("৳ %.0f", fine) : "No dues";
    }

    private JLabel addStat(String label, String value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(UITheme.BG_CARD);
        JLabel lbl = UITheme.label(label, UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        JLabel val = UITheme.label(value, UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(lbl); cell.add(Box.createVerticalStrut(2)); cell.add(val);
        add(cell); return val;
    }

    public void refresh() {
        totalBooksVal.setText(String.valueOf(library.getTotalBooks()));
        issuedVal.setText(borrowedText());
        categoryVal.setText(library.getMostPopularCategory());
        double fine = fineValue();
        fineVal.setText(fine > 0 ? String.format("৳ %.0f", fine) : "No dues");
        fineVal.setForeground(fine > 0 ? UITheme.ACCENT_RED : UITheme.TEXT_PRIMARY);
    }
}

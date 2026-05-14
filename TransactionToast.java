import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransactionToast extends JDialog {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public TransactionToast(Frame owner, String action, Book book, User user) {
        super(owner, "Transaction", false);
        setSize(340, 260); setLocationRelativeTo(owner);
        setResizable(false); setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(UITheme.ACCENT, 1));

        boolean isBorrow = action.equalsIgnoreCase("borrowed");
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel icon = UITheme.label(isBorrow ? "✅ BORROWED" : "↩ RETURNED",
            UITheme.FONT_HEADING, isBorrow ? UITheme.ACCENT_GREEN : UITheme.ACCENT);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sep = UITheme.label("─────────────────────────", UITheme.FONT_SMALL, UITheme.BORDER);
        sep.setAlignmentX(CENTER_ALIGNMENT);

        JLabel close2 = UITheme.label("─────────────────────────", UITheme.FONT_SMALL, UITheme.BORDER);
        close2.setAlignmentX(CENTER_ALIGNMENT);

        String dueStr = isBorrow
            ? "Due:   " + LocalDate.now().plusDays(7).format(FMT)
            : "Date:  " + LocalDate.now().format(FMT);

        JButton closeBtn = UITheme.primaryButton("Done");
        closeBtn.setAlignmentX(CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());

        for (Component c : new Component[]{icon, Box.createVerticalStrut(6), sep,
                Box.createVerticalStrut(10),
                UITheme.label("Book:  " + trunc(book.getTitle(), 22), UITheme.FONT_BODY, UITheme.TEXT_PRIMARY),
                Box.createVerticalStrut(4),
                UITheme.label("User:  " + trunc(user.email, 22), UITheme.FONT_BODY, UITheme.TEXT_PRIMARY),
                Box.createVerticalStrut(4),
                UITheme.label(dueStr, UITheme.FONT_BODY, isBorrow ? UITheme.ACCENT_AMBER : UITheme.TEXT_MUTED),
                Box.createVerticalStrut(4),
                UITheme.label("Stock: " + book.getAvailableCopies() + " / " + book.getTotalCopies(), UITheme.FONT_BODY, UITheme.TEXT_MUTED),
                Box.createVerticalStrut(10), close2, Box.createVerticalStrut(14), closeBtn}) {
            p.add(c);
        }
        add(p);
    }

    private String trunc(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}

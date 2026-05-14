import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SettingsPanel extends JPanel {
    private final HashMap<String, User> userDb;
    private final Runnable saveAll;

    private JTextField loanDaysField, fineRateField, borrowLimitField;
    private JLabel statusLabel;

    public SettingsPanel(HashMap<String, User> userDb, Runnable saveAll) {
        this.userDb = userDb;
        this.saveAll = saveAll;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        add(buildGlobalSettings(), BorderLayout.NORTH);
        add(buildMemberSettings(), BorderLayout.CENTER);

        statusLabel = UITheme.label("", UITheme.FONT_SMALL, UITheme.ACCENT_GREEN);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildGlobalSettings() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(UITheme.cardBorder());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        loanDaysField = UITheme.styledField(8);
        loanDaysField.setText(String.valueOf(LibraryConfig.LOAN_DAYS));
        fineRateField = UITheme.styledField(8);
        fineRateField.setText(String.valueOf((int) LibraryConfig.FINE_PER_DAY));
        borrowLimitField = UITheme.styledField(8);
        borrowLimitField.setText(String.valueOf(LibraryConfig.DEFAULT_BORROW_LIMIT));

        addRow(card, g, 0, "📅  Loan Period (days):", loanDaysField);
        addRow(card, g, 1, "💰  Fine per Overdue Day (৳):", fineRateField);
        addRow(card, g, 2, "📚  Default Borrow Limit (books):", borrowLimitField);

        g.gridy = 3;
        g.gridx = 0;
        g.gridwidth = 2;
        JButton saveBtn = UITheme.primaryButton("Save Global Settings");
        saveBtn.addActionListener(e -> saveGlobalSettings());
        card.add(saveBtn, g);

        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setBackground(UITheme.BG_DARK);
        wrap.add(UITheme.label("⚙  Global Library Settings", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY),
                BorderLayout.NORTH);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String labelText, JComponent field) {
        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 0;
        p.add(UITheme.label(labelText, UITheme.FONT_BODY, UITheme.TEXT_PRIMARY), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(field, g);
    }

    private void saveGlobalSettings() {
        try {
            int days = Integer.parseInt(loanDaysField.getText().trim());
            int fine = Integer.parseInt(fineRateField.getText().trim());
            int limit = Integer.parseInt(borrowLimitField.getText().trim());
            if (days < 1 || fine < 0 || limit < 1) {
                status("Values must be positive.", false);
                return;
            }
            LibraryConfig.LOAN_DAYS = days;
            LibraryConfig.FINE_PER_DAY = fine;
            LibraryConfig.DEFAULT_BORROW_LIMIT = limit;
            LibraryConfig.save();
            saveAll.run();
            status("Settings saved.", true);
        } catch (NumberFormatException ex) {
            status("Please enter valid numbers.", false);
        }
    }

    private JPanel buildMemberSettings() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(UITheme.BG_DARK);
        outer.add(UITheme.label("👤  Per-Member Settings", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY),
                BorderLayout.NORTH);

        String[] cols = { "Username", "Email", "Borrow Limit", "Outstanding Fine (৳)", "Action" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        refresh(model);

        JTable table = new JTable(model);
        UITheme.styleTableZebra(table);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setBackground(UITheme.BG_CARD);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton setBorrowBtn = UITheme.ghostButton("Set Borrow Limit");
        JButton payFineBtn = UITheme.ghostButton("Mark Fine Paid");

        setBorrowBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                status("Select a member first.", false);
                return;
            }
            String email = (String) model.getValueAt(row, 1);
            User u = userDb.get(email);
            if (u == null)
                return;
            String input = JOptionPane.showInputDialog(this,
                    "Set borrow limit for " + u.getDisplayName() + ":", u.maxBorrowLimit);
            if (input == null)
                return;
            try {
                int lim = Integer.parseInt(input.trim());
                if (lim < 1) {
                    status("Limit must be at least 1.", false);
                    return;
                }
                u.maxBorrowLimit = lim;
                saveAll.run();
                refresh(model);
                status("Borrow limit updated for " + u.getDisplayName(), true);
            } catch (NumberFormatException ex) {
                status("Enter a valid number.", false);
            }
        });

        payFineBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                status("Select a member first.", false);
                return;
            }
            String email = (String) model.getValueAt(row, 1);
            User u = userDb.get(email);
            if (u == null)
                return;
            double fine = u.calculateFine();
            if (fine <= 0) {
                status(u.getDisplayName() + " has no outstanding fine.", false);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Mark ৳%.0f fine as paid for %s?", fine, u.getDisplayName()),
                    "Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                u.payFine(fine);
                // Reset issuedBooks due dates so fine clears (shift past due dates to today)
                // Actually we just record payment — fine still accrues on unreturned books
                saveAll.run();
                refresh(model);
                status(String.format("৳%.0f marked as paid for %s.", fine, u.getDisplayName()), true);
            }
        });

        bar.add(setBorrowBtn);
        bar.add(payFineBtn);
        bar.add(UITheme.label("  Select a member first", UITheme.FONT_SMALL, UITheme.TEXT_MUTED));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(UITheme.cardBorder());
        card.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        card.add(bar, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private void refresh(DefaultTableModel model) {
        model.setRowCount(0);
        for (User u : userDb.values()) {
            if (u.isAdmin)
                continue;
            model.addRow(new Object[] {
                    u.getDisplayName(), u.email, u.maxBorrowLimit,
                    String.format("%.0f", u.calculateFine()), ""
            });
        }
    }

    private void status(String msg, boolean ok) {
        statusLabel.setForeground(ok ? UITheme.ACCENT_GREEN : UITheme.ACCENT_RED);
        statusLabel.setText(msg);
    }
}

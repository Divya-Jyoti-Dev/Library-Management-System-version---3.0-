import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class AdminRemindersPanel extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final int REMIND_DAYS = 3;

    private final HashMap<String, User> userDb;
    private final Library library;
    private final User adminUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    public AdminRemindersPanel(HashMap<String, User> userDb, Library library, User adminUser) {
        this.userDb = userDb;
        this.library = library;
        this.adminUser = adminUser;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(UITheme.BG_CARD2);
        toolbar.setBorder(UITheme.sectionBorder(0, 6));
        JButton autoBtn = UITheme.primaryButton("⚡ Auto-Remind Near Due / Overdue");
        JButton manualBtn = UITheme.ghostButton("✉ Send Manual Reminder");
        JButton refreshBtn = UITheme.ghostButton("🔄 Refresh");
        statusLabel = UITheme.label("", UITheme.FONT_SMALL, UITheme.ACCENT_GREEN);
        autoBtn.addActionListener(e -> sendAutoReminders());
        manualBtn.addActionListener(e -> sendManualReminder());
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(autoBtn);
        toolbar.add(manualBtn);
        toolbar.add(refreshBtn);
        toolbar.add(statusLabel);

        JPanel northWrap = new JPanel(new BorderLayout());
        northWrap.setBackground(UITheme.BG_DARK);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        header.add(UITheme.label("🔔 Reminders & Messages", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY),
                BorderLayout.WEST);
        northWrap.add(header, BorderLayout.NORTH);
        northWrap.add(toolbar, BorderLayout.CENTER);
        add(northWrap, BorderLayout.NORTH);

        String[] cols = { "User", "Book Title", "ISBN", "Due Date", "Days Left", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        center.add(UITheme.styledScroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JLabel note = UITheme.label(
                "ℹ  Auto-Remind sends a message to users whose book is due within " + REMIND_DAYS
                        + " days or is already overdue.",
                UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        note.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(note, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (User u : userDb.values()) {
            if (u.isAdmin || u.issuedBooks == null || u.issuedBooks.isEmpty())
                continue;
            for (Map.Entry<String, LocalDate> e : u.issuedBooks.entrySet()) {
                Book b = library.getBook(e.getKey());
                long days = e.getValue().toEpochDay() - today.toEpochDay();
                String status = days < 0 ? "OVERDUE (" + Math.abs(days) + "d)"
                        : days == 0 ? "DUE TODAY" : days <= REMIND_DAYS ? "DUE SOON" : "OK";
                tableModel.addRow(new Object[] { u.email, b != null ? b.getTitle() : e.getKey(),
                        e.getKey(), e.getValue().format(FMT), days, status });
            }
        }
        if (tableModel.getRowCount() == 0)
            statusLabel.setText("No books currently borrowed.");
    }

    private void sendAutoReminders() {
        LocalDate today = LocalDate.now();
        int sent = 0;
        for (User u : userDb.values()) {
            if (u.isAdmin || u.issuedBooks == null)
                continue;
            if (u.inbox == null)
                u.inbox = new ArrayList<>();
            for (Map.Entry<String, LocalDate> e : u.issuedBooks.entrySet()) {
                long days = e.getValue().toEpochDay() - today.toEpochDay();
                if (days > REMIND_DAYS)
                    continue;
                Book b = library.getBook(e.getKey());
                String title = b != null ? b.getTitle() : e.getKey();
                u.inbox.add(new Message("SYSTEM", u.email,
                        ReminderHelper.subject(title, days),
                        ReminderHelper.body(u.email, title, e.getValue(), days, "Regards,\nLibraryOS Admin"),
                        Message.Type.AUTO_DUE_REMINDER));
                sent++;
            }
        }
        refresh();
        statusLabel.setForeground(sent > 0 ? UITheme.ACCENT_GREEN : UITheme.TEXT_MUTED);
        statusLabel.setText(sent > 0 ? "✓ " + sent + " auto-reminder(s) sent." : "No near-due or overdue books found.");
    }

    private void sendManualReminder() {
        int row = table.getSelectedRow();
        String prefillEmail = row != -1 ? (String) tableModel.getValueAt(row, 0) : "";
        String prefillBook = row != -1 ? (String) tableModel.getValueAt(row, 1) : "";

        java.util.List<String> emails = new ArrayList<>();
        for (User u : userDb.values())
            if (!u.isAdmin)
                emails.add(u.email);
        if (emails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No regular users found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> toBox = new JComboBox<>(emails.toArray(new String[0]));
        toBox.setBackground(UITheme.BG_CARD2);
        toBox.setForeground(UITheme.TEXT_PRIMARY);
        toBox.setFont(UITheme.FONT_BODY);
        if (!prefillEmail.isEmpty())
            toBox.setSelectedItem(prefillEmail);

        JTextField subField = UITheme.styledField(40);
        subField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        if (!prefillBook.isEmpty())
            subField.setText("Return Reminder: \"" + prefillBook + "\"");

        JTextArea bodyArea = new JTextArea(6, 40);
        bodyArea.setBackground(UITheme.BG_CARD2);
        bodyArea.setForeground(UITheme.TEXT_PRIMARY);
        bodyArea.setFont(UITheme.FONT_BODY);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        if (!prefillBook.isEmpty())
            bodyArea.setText("Dear reader,\n\nThis is a reminder to please return \"" + prefillBook
                    + "\" on time.\n\nThank you,\nLibraryOS Admin");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (Component c : new Component[] {
                lbl("To (User Email):"), toBox, Box.createVerticalStrut(10),
                lbl("Subject:"), subField, Box.createVerticalStrut(10),
                lbl("Message:"), new JScrollPane(bodyArea) }) {
            if (c instanceof JComponent jc)
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(c);
            if (c instanceof JLabel)
                form.add(Box.createVerticalStrut(4));
        }

        int res = JOptionPane.showConfirmDialog(this, form, "Send Manual Reminder", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        String toEmail = (String) toBox.getSelectedItem();
        String subject = subField.getText().trim();
        String body = bodyArea.getText().trim();
        if (subject.isEmpty() || body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject and message cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        User target = userDb.get(toEmail);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (target.inbox == null)
            target.inbox = new ArrayList<>();
        target.inbox.add(new Message(adminUser.email, toEmail, subject, body, Message.Type.MANUAL_REMINDER));
        statusLabel.setForeground(UITheme.ACCENT_GREEN);
        statusLabel.setText("✓ Message sent to " + toEmail);
    }

    private JLabel lbl(String text) {
        return UITheme.label(text, UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            String s = val == null ? "" : val.toString();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(UITheme.FONT_SMALL);
            if (s.startsWith("OVERDUE"))
                lbl.setForeground(UITheme.ACCENT_RED);
            else if (s.equals("DUE TODAY"))
                lbl.setForeground(UITheme.ACCENT_AMBER);
            else if (s.equals("DUE SOON"))
                lbl.setForeground(UITheme.ACCENT_AMBER);
            else
                lbl.setForeground(UITheme.ACCENT_GREEN);
            lbl.setBackground(sel ? new Color(88, 166, 255, 50) : UITheme.BG_CARD);
            return lbl;
        }
    }
}

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MessagesPanel extends JPanel {
    private final User user;
    private DefaultTableModel tableModel;
    private JTextArea bodyArea;
    private JLabel unreadBadge;
    private JTable table;
    private Runnable onReadCallback;

    public MessagesPanel(User user) {
        this(user, null);
    }

    public MessagesPanel(User user, Runnable onReadCallback) {
        this.user = user;
        this.onReadCallback = onReadCallback;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        unreadBadge = UITheme.label("", UITheme.FONT_SMALL, UITheme.ACCENT_RED);
        header.add(UITheme.label("📬 My Messages", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), BorderLayout.WEST);
        header.add(unreadBadge, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = { "", "Subject", "From", "Time" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(24);
        table.getColumnModel().getColumn(0).setMinWidth(24);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(1).setMinWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setMinWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setMinWidth(90);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                showSelected(table);
        });

        bodyArea = new JTextArea();
        bodyArea.setEditable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBackground(UITheme.BG_CARD);
        bodyArea.setForeground(UITheme.TEXT_PRIMARY);
        bodyArea.setFont(UITheme.FONT_BODY);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UITheme.styledScroll(table), UITheme.styledScroll(bodyArea));
        split.setBackground(UITheme.BG_DARK);
        split.setDividerLocation(420);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private void showSelected(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1 || user.inbox == null)
            return;
        int idx = user.inbox.size() - 1 - row;
        if (idx < 0 || idx >= user.inbox.size())
            return;
        Message m = user.inbox.get(idx);
        m.markRead();
        tableModel.setValueAt(false, row, 0);
        bodyArea.setText("From: " + m.getFromEmail() + "\nDate: " + m.getFormattedTime() +
                "\nSubject: " + m.getSubject() + "\n\n" + m.getBody());
        updateBadge();
        if (onReadCallback != null)
            onReadCallback.run();
    }

    public void setOnReadCallback(Runnable cb) {
        this.onReadCallback = cb;
    }

    private void updateBadge() {
        int unread = user.unreadCount();
        unreadBadge.setText(unread > 0 ? unread + " unread" : "");
    }

    public void refresh() {
        if (user.inbox == null)
            user.inbox = new java.util.ArrayList<>();
        int selectedRow = table != null ? table.getSelectedRow() : -1;
        tableModel.setRowCount(0);
        List<Message> msgs = user.inbox;
        for (int i = msgs.size() - 1; i >= 0; i--) {
            Message m = msgs.get(i);
            tableModel.addRow(new Object[] { !m.isRead(), m.getSubject(), m.getFromEmail(), m.getFormattedTime() });
        }
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount() && table != null) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }
        updateBadge();
    }

    public int getUnreadCount() {
        return user.unreadCount();
    }
}

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class AuthFrame extends JFrame {
    private final HashMap<String, User> userDb;
    private final User[] loggedInUser;
    private final Runnable onSuccess;

    private static final Color DARK_BG     = new Color(18, 20, 28);
    private static final Color DARK_CARD   = new Color(28, 32, 44);
    private static final Color DARK_FIELD  = new Color(38, 43, 58);
    private static final Color DARK_BORDER = new Color(55, 62, 82);
    private static final Color DARK_TEXT   = new Color(220, 225, 235);
    private static final Color DARK_MUTED  = new Color(120, 130, 150);
    private static final Color ACCENT      = new Color(32, 164, 147);
    private static final Color ACCENT_DARK = new Color(24, 130, 116);
    private static final Color ACCENT_RED  = new Color(220, 70, 70);
    private static final Color ACCENT_GREEN= new Color(50, 190, 120);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    private JTextField emailField, usernameField;
    private JPasswordField passField;
    private JLabel statusLabel, usernameLbl;
    private boolean showingLogin = true;
    private JButton loginTab, regTab, actionBtn;

    public AuthFrame(HashMap<String, User> userDb, User[] loggedInUserBox, Runnable onSuccess) {
        this.userDb = userDb; this.loggedInUser = loggedInUserBox; this.onSuccess = onSuccess;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); setResizable(false);
        getContentPane().setBackground(DARK_BG);
        setLayout(new BorderLayout());
        if (userDb.isEmpty()) {
            buildAdminSetup();
        } else {
            buildAuthPanel();
        }
        setVisible(true);
    }

    private void buildAdminSetup() {
        setTitle("Library Management System — Admin Setup");
        setSize(420, 560);
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(DARK_BG);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(DARK_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BORDER, 1),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        card.setPreferredSize(new Dimension(360, 490));
        JLabel logo = darkLabel("📚 Library Management System", FONT_TITLE, ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        JLabel heading = darkLabel("Create Admin Account", FONT_HEADING, DARK_TEXT);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = darkLabel("Set up your administrator credentials", FONT_SMALL, DARK_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField aUsernameField = darkField();
        JTextField aEmailField    = darkField();
        JPasswordField aPassField    = darkPass();
        JPasswordField aConfirmField = darkPass();
        JButton createBtn = accentBtn("Create & Sign In");
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JLabel statusLbl = darkLabel("", FONT_SMALL, ACCENT_RED);
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        Runnable doSetup = () -> {
            String uname = aUsernameField.getText().trim();
            String email = aEmailField.getText().trim();
            String pass  = new String(aPassField.getPassword()).trim();
            String conf  = new String(aConfirmField.getPassword()).trim();
            if (uname.isEmpty() || email.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
                statusLbl.setText("Please fill in all fields."); return;
            }
            if (!email.contains("@")) { statusLbl.setText("Enter a valid email."); return; }
            if (pass.length() < 6)    { statusLbl.setText("Password must be at least 6 characters."); return; }
            if (!pass.equals(conf))   { statusLbl.setText("Passwords do not match."); return; }
            User admin = new User(email, pass, uname, true);
            userDb.put(email, admin);
            loggedInUser[0] = admin;
            dispose(); onSuccess.run();
        };
        aConfirmField.addActionListener(e -> doSetup.run());
        createBtn.addActionListener(e -> doSetup.run());
        for (Component c : new Component[]{
                Box.createVerticalStrut(8), logo, Box.createVerticalStrut(6),
                heading, Box.createVerticalStrut(4), sub, Box.createVerticalStrut(18),
                lbl("Username"), Box.createVerticalStrut(4), aUsernameField, Box.createVerticalStrut(10),
                lbl("Email"),    Box.createVerticalStrut(4), aEmailField,    Box.createVerticalStrut(10),
                lbl("Password"), Box.createVerticalStrut(4), aPassField,     Box.createVerticalStrut(10),
                lbl("Confirm Password"), Box.createVerticalStrut(4), aConfirmField, Box.createVerticalStrut(16),
                createBtn, Box.createVerticalStrut(8), statusLbl}) {
            card.add(c);
        }
        outer.add(card);
        add(outer, BorderLayout.CENTER);
    }

    private void buildAuthPanel() {
        setTitle("Library System — Sign In");
        setSize(420, 520);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildCenter() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(DARK_BG);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(DARK_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BORDER, 1),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        card.setPreferredSize(new Dimension(360, 450));
        JLabel logo = darkLabel("📚 Library Management System", FONT_TITLE, ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        JLabel subtitle = darkLabel("Your smart library companion", FONT_SMALL, DARK_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel tabs = new JPanel(new GridLayout(1, 2));
        tabs.setBackground(new Color(22, 26, 38));
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        tabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginTab  = tabBtn("Sign In");
        regTab    = tabBtn("Register");
        actionBtn = accentBtn("Sign In");
        highlight(loginTab, true); highlight(regTab, false);
        tabs.add(loginTab); tabs.add(regTab);
        emailField    = darkField();
        usernameField = darkField();
        passField     = darkPass();
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        statusLabel = darkLabel("", FONT_SMALL, ACCENT_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel emailLbl = lbl("Email");
        usernameLbl     = lbl("Username (for registration)");
        JLabel passLbl  = lbl("Password");
        usernameLbl.setVisible(false);
        usernameField.setVisible(false);
        loginTab.addActionListener(e -> {
            showingLogin = true;
            highlight(loginTab, true); highlight(regTab, false);
            actionBtn.setText("Sign In");
            usernameLbl.setVisible(false); usernameField.setVisible(false);
            statusLabel.setForeground(ACCENT_RED); statusLabel.setText("");
        });
        regTab.addActionListener(e -> {
            showingLogin = false;
            highlight(loginTab, false); highlight(regTab, true);
            actionBtn.setText("Register");
            usernameLbl.setVisible(true); usernameField.setVisible(true);
            statusLabel.setForeground(ACCENT_RED); statusLabel.setText("");
        });
        actionBtn.addActionListener(e -> handleSubmit());
        passField.addActionListener(e -> handleSubmit());
        for (Component c : new Component[]{
                Box.createVerticalStrut(10), logo, Box.createVerticalStrut(4),
                subtitle, Box.createVerticalStrut(16), tabs, Box.createVerticalStrut(16),
                usernameLbl, Box.createVerticalStrut(4), usernameField, Box.createVerticalStrut(12),
                emailLbl, Box.createVerticalStrut(4), emailField, Box.createVerticalStrut(12),
                passLbl, Box.createVerticalStrut(4), passField, Box.createVerticalStrut(16),
                actionBtn, Box.createVerticalStrut(10), statusLabel}) {
            card.add(c);
        }
        outer.add(card);
        return outer;
    }

    private JLabel lbl(String text) {
        JLabel l = darkLabel(text, FONT_SMALL, DARK_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }

    private JLabel darkLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }

    private JTextField darkField() {
        JTextField tf = new JTextField(20); tf.setFont(FONT_BODY);
        tf.setBackground(DARK_FIELD); tf.setForeground(DARK_TEXT); tf.setCaretColor(DARK_TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return tf;
    }

    private JPasswordField darkPass() {
        JPasswordField pf = new JPasswordField(20); pf.setFont(FONT_BODY);
        pf.setBackground(DARK_FIELD); pf.setForeground(DARK_TEXT); pf.setCaretColor(DARK_TEXT);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return pf;
    }

    private JButton accentBtn(String text) {
        JButton b = new JButton(text); b.setFont(FONT_BODY);
        b.setBackground(ACCENT); b.setForeground(Color.WHITE);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(ACCENT_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(ACCENT); }
        });
        return b;
    }

    private JButton tabBtn(String text) {
        JButton b = new JButton(text); b.setFont(FONT_BODY);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return b;
    }

    private void highlight(JButton btn, boolean active) {
        btn.setBackground(active ? ACCENT : new Color(22, 26, 38));
        btn.setForeground(active ? Color.WHITE : DARK_MUTED);
    }

    private void handleSubmit() {
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword()).trim();
        if (email.isEmpty() || pass.isEmpty()) { statusLabel.setText("Please fill in all fields."); return; }
        if (pass.length() < 6) { statusLabel.setText("Password must be at least 6 characters."); return; }
        if (showingLogin) {
            User u = userDb.get(email);
            // Support both hashed passwords (new) and plain-text legacy passwords
            if (u != null && (u.checkPassword(pass) || u.password.equals(pass))) {
                // Migrate plain-text password to hashed on first login
                if (!u.password.matches("[0-9a-f]{64}")) {
                    u.password = User.hashPassword(pass);
                }
                loggedInUser[0] = u; dispose(); onSuccess.run();
            } else {
                statusLabel.setText("Invalid email or password.");
            }
        } else {
            String uname = usernameField.getText().trim();
            if (uname.isEmpty()) { statusLabel.setText("Please enter a username."); return; }
            if (uname.length() < 3) { statusLabel.setText("Username must be at least 3 characters."); return; }
            if (!email.contains("@")) { statusLabel.setText("Enter a valid email."); return; }
            if (userDb.containsKey(email)) { statusLabel.setText("Email already registered."); return; }
            boolean takenName = userDb.values().stream().anyMatch(u -> uname.equalsIgnoreCase(u.username));
            if (takenName) { statusLabel.setText("Username already taken."); return; }
            User newUser = new User(email, pass, uname, false);
            newUser.maxBorrowLimit = LibraryConfig.DEFAULT_BORROW_LIMIT;
            userDb.put(email, newUser);
            showingLogin = true;
            highlight(loginTab, true); highlight(regTab, false);
            actionBtn.setText("Sign In");
            usernameLbl.setVisible(false); usernameField.setVisible(false);
            emailField.setText(email); passField.setText("");
            statusLabel.setForeground(ACCENT_GREEN);
            statusLabel.setText("Registered! Please sign in.");
        }
    }
}

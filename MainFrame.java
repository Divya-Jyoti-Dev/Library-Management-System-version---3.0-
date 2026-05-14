import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainFrame extends JFrame {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final Library library;
    private final User currentUser;
    private final HashMap<String, User> userDb;
    private final Runnable saveAll, logout;

    private BookTableModel tableModel;
    private JTable bookTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private DashboardPanel dashboard;
    private MyAccountPanel accountPanel;
    private MessagesPanel messagesPanel;
    private AdminRemindersPanel remindersPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    private JPanel contentArea;
    private CardLayout contentCards;
    private JLabel msgBadge;
    private java.util.List<JButton> navTabs = new ArrayList<>();
    private JButton activeTab = null;

    private static final String CARD_CATALOG = "catalog";
    private static final String CARD_CATEGORIES = "categories";
    private static final String CARD_USERS = "users";
    private static final String CARD_REMINDERS = "reminders";
    private static final String CARD_REPORTS = "reports";
    private static final String CARD_SETTINGS = "settings";
    private static final String CARD_ACCOUNT = "account";
    private static final String CARD_MESSAGES = "messages";
    private static final String CARD_DASHBOARD = "dashboard";

    private static final Color LIB_TEAL = new Color(32, 164, 147);
    private static final Color LIB_TEAL_DARK = new Color(24, 130, 116);
    private static final Color LIB_TEAL_LIGHT = new Color(230, 248, 246);
    private static final Color LIB_PAGE_BG = new Color(245, 247, 249);
    private static final Color LIB_CARD_BG = Color.WHITE;
    private static final Color LIB_BORDER = new Color(220, 228, 235);
    private static final Color LIB_TEXT = new Color(40, 50, 65);
    private static final Color LIB_MUTED = new Color(120, 130, 145);
    private static final Color LIB_GREEN = new Color(40, 167, 69);
    private static final Color LIB_RED = new Color(220, 53, 69);
    private static final Color LIB_AMBER = new Color(255, 152, 0);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public MainFrame(Library library, User currentUser, HashMap<String, User> userDb,
            Runnable saveAll, Runnable logout) {
        this.library = library;
        this.currentUser = currentUser;
        this.userDb = userDb;
        this.saveAll = saveAll;
        this.logout = logout;

        setTitle("Library Management System");
        setSize(1200, 740);
        setMinimumSize(new Dimension(940, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LIB_PAGE_BG);
        setLayout(new BorderLayout(0, 0));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveAll.run();
                System.exit(0);
            }
        });

        JPanel topSection = new JPanel(new BorderLayout(0, 0));
        topSection.add(buildHeader(), BorderLayout.NORTH);
        topSection.add(buildNavBar(), BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        refreshTable(library.getAllBooks());
        sendAutoRemindersOnStartup();
        updateMsgBadge();
        showCard(CARD_DASHBOARD);

        SwingUtilities.invokeLater(this::showReservationPopups);

        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LIB_BORDER),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Color.WHITE);
        JLabel iconBox = new JLabel("  \uD83D\uDCDA  ");
        iconBox.setOpaque(true);
        iconBox.setBackground(LIB_TEAL);
        iconBox.setForeground(Color.WHITE);
        iconBox.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconBox.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        JPanel logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setBackground(Color.WHITE);
        JLabel siteName = new JLabel("  Library Management System");
        siteName.setFont(FONT_TITLE);
        siteName.setForeground(LIB_TEAL);
        JLabel tagLine = new JLabel("  Online Library Management System");
        tagLine.setFont(FONT_SMALL);
        tagLine.setForeground(LIB_MUTED);
        logoText.add(siteName);
        logoText.add(tagLine);
        left.add(iconBox);
        left.add(logoText);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);
        String initials = currentUser.getDisplayName().substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(LIB_TEAL);
        avatar.setPreferredSize(new Dimension(32, 32));
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(currentUser.getDisplayName());
        nameLabel.setFont(FONT_SMALL);
        nameLabel.setForeground(LIB_TEXT);
        JLabel roleLabel = new JLabel(currentUser.isAdmin ? "Administrator" : "Member");
        roleLabel.setFont(FONT_SMALL);
        roleLabel.setForeground(LIB_MUTED);
        userInfo.add(nameLabel);
        userInfo.add(roleLabel);

        JButton saveBtn = outlineBtn("Save");
        JButton logoutBtn = primaryBtn("Sign Out");
        saveBtn.addActionListener(e -> {
            saveAll.run();
            showInfo("Saved successfully.");
        });
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Sign out?", "Sign Out",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                saveAll.run();
                dispose();
                logout.run();
            }
        });
        right.add(avatar);
        right.add(userInfo);
        right.add(Box.createHorizontalStrut(8));
        right.add(saveBtn);
        right.add(logoutBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nav.setBackground(LIB_TEAL);
        nav.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        addNavTab(nav, "Home", CARD_DASHBOARD);
        addNavTab(nav, "Catalog", CARD_CATALOG);
        if (currentUser.isAdmin) {
            addNavTab(nav, "Categories", CARD_CATEGORIES);
            addNavTab(nav, "Members", CARD_USERS);
            addNavTab(nav, "Reminders", CARD_REMINDERS);
            addNavTab(nav, "Reports", CARD_REPORTS);
            addNavTab(nav, "Settings", CARD_SETTINGS);
        } else {
            addNavTab(nav, "My Account", CARD_ACCOUNT);

            JButton msgTab = makeNavTab("Messages");
            msgBadge = new JLabel();
            msgBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            msgBadge.setForeground(Color.WHITE);
            msgBadge.setOpaque(true);
            msgBadge.setBackground(LIB_RED);
            msgBadge.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
            msgBadge.setVisible(false);
            msgTab.addActionListener(e -> setActiveTab(msgTab, CARD_MESSAGES));
            navTabs.add(msgTab);
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrap.setBackground(LIB_TEAL);
            wrap.add(msgTab);
            wrap.add(msgBadge);
            nav.add(wrap);
        }
        return nav;
    }

    private void addNavTab(JPanel nav, String label, String card) {
        JButton tab = makeNavTab(label);
        tab.addActionListener(e -> setActiveTab(tab, card));
        navTabs.add(tab);
        nav.add(tab);
    }

    private JButton makeNavTab(String label) {
        JButton tab = new JButton(label);
        tab.setFont(FONT_BODY);
        tab.setForeground(new Color(220, 245, 242));
        tab.setBackground(LIB_TEAL);
        tab.setOpaque(true);
        tab.setBorderPainted(false);
        tab.setFocusPainted(false);
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        tab.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (tab != activeTab)
                    tab.setBackground(LIB_TEAL_DARK);
            }

            public void mouseExited(MouseEvent e) {
                if (tab != activeTab)
                    tab.setBackground(LIB_TEAL);
            }
        });
        return tab;
    }

    private void setActiveTab(JButton tab, String card) {
        for (JButton t : navTabs) {
            t.setBackground(LIB_TEAL);
            t.setForeground(new Color(220, 245, 242));
            t.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }
        tab.setBackground(LIB_TEAL_DARK);
        tab.setForeground(Color.WHITE);
        tab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(10, 20, 7, 20)));
        activeTab = tab;
        showCard(card);
    }

    private void activateTab(String card) {
        String keyword = switch (card) {
            case CARD_CATALOG -> "Catalog";
            case CARD_CATEGORIES -> "Categories";
            case CARD_USERS -> "Members";
            case CARD_REMINDERS -> "Reminders";
            case CARD_REPORTS -> "Reports";
            case CARD_SETTINGS -> "Settings";
            case CARD_ACCOUNT -> "Account";
            case CARD_MESSAGES -> "Messages";
            default -> "Home";
        };
        for (JButton t : navTabs) {
            if (t.getText().contains(keyword)) {
                setActiveTab(t, card);
                return;
            }
        }
        showCard(card);
    }

    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(LIB_PAGE_BG);
        contentArea = new JPanel(contentCards = new CardLayout());
        contentArea.setBackground(LIB_PAGE_BG);

        dashboard = new DashboardPanel(library, currentUser, userDb);
        tableModel = new BookTableModel();
        bookTable = buildBookTable();
        accountPanel = currentUser.isAdmin ? null : new MyAccountPanel(currentUser, library);
        messagesPanel = currentUser.isAdmin ? null : new MessagesPanel(currentUser, this::updateMsgBadge);
        remindersPanel = currentUser.isAdmin ? new AdminRemindersPanel(userDb, library, currentUser) : null;
        reportsPanel = currentUser.isAdmin ? new ReportsPanel(library, userDb) : null;
        settingsPanel = currentUser.isAdmin ? new SettingsPanel(userDb, saveAll) : null;

        contentArea.add(buildDashboardCard(), CARD_DASHBOARD);
        contentArea.add(buildCatalogCard(), CARD_CATALOG);
        if (currentUser.isAdmin) {
            contentArea.add(buildCategoriesPanel(), CARD_CATEGORIES);
            contentArea.add(buildUsersPanel(), CARD_USERS);
            contentArea.add(remindersPanel, CARD_REMINDERS);
            contentArea.add(reportsPanel, CARD_REPORTS);
            contentArea.add(settingsPanel, CARD_SETTINGS);
        } else {
            contentArea.add(accountPanel, CARD_ACCOUNT);
            contentArea.add(messagesPanel, CARD_MESSAGES);
        }
        wrapper.add(contentArea, BorderLayout.CENTER);
        return wrapper;
    }

    private void showCard(String card) {
        contentCards.show(contentArea, card);
    }

    private JPanel pageTitle(String title, String sub) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(LIB_PAGE_BG);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 17));
        t.setForeground(LIB_TEXT);
        JLabel s = new JLabel(sub);
        s.setFont(FONT_SMALL);
        s.setForeground(LIB_MUTED);
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(LIB_PAGE_BG);
        col.add(t);
        col.add(Box.createVerticalStrut(2));
        col.add(s);
        row.add(col, BorderLayout.WEST);
        return row;
    }

    private JPanel buildDashboardCard() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(LIB_PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        page.add(pageTitle("Dashboard", "Welcome back, " + currentUser.getDisplayName()), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(LIB_PAGE_BG);
        center.add(dashboard, BorderLayout.NORTH);
        JPanel qRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qRow.setBackground(LIB_PAGE_BG);
        JButton goCatalog = primaryBtn("Browse Catalog");
        goCatalog.addActionListener(e -> activateTab(CARD_CATALOG));
        qRow.add(goCatalog);
        if (!currentUser.isAdmin) {
            JButton goAcc = outlineBtn("My Account");
            goAcc.addActionListener(e -> activateTab(CARD_ACCOUNT));
            qRow.add(goAcc);
        }
        center.add(qRow, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildCatalogCard() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(LIB_PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(LIB_PAGE_BG);
        titleRow.add(pageTitle("Book Catalog", "Browse and manage library books"), BorderLayout.WEST);
        if (currentUser.isAdmin) {
            JButton addBtn = primaryBtn("+ Add Book");
            addBtn.addActionListener(e -> showAddBookDialog());
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            btnWrap.setBackground(LIB_PAGE_BG);
            btnWrap.add(addBtn);
            titleRow.add(btnWrap, BorderLayout.EAST);
        }
        page.add(titleRow, BorderLayout.NORTH);

        JPanel mainCard = new JPanel(new BorderLayout(0, 0));
        mainCard.setBackground(LIB_CARD_BG);
        mainCard.setBorder(BorderFactory.createLineBorder(LIB_BORDER, 1));
        mainCard.add(buildSearchBar(), BorderLayout.NORTH);
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(Color.WHITE);
        tableWrap.add(styledScroll(bookTable), BorderLayout.CENTER);
        mainCard.add(tableWrap, BorderLayout.CENTER);
        mainCard.add(buildActionBar(), BorderLayout.SOUTH);
        page.add(mainCard, BorderLayout.CENTER);
        return page;
    }

    private JTable buildBookTable() {
        JTable t = new JTable(tableModel);
        styleTable(t);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        int[][] colW = { { 36, 36 }, { 220, 120 }, { 150, 90 }, { 80, 70 }, { 120, 80 }, { 65, 55 }, { 110, 90 } };
        for (int i = 0; i < colW.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(colW[i][0]);
            t.getColumnModel().getColumn(i).setMinWidth(colW[i][1]);
        }
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    showBookDetail(getSelectedBook());
            }
        });
        return t;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LIB_BORDER),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        searchField = styledField(26);
        searchField.addActionListener(e -> doSearch());
        JButton searchBtn = primaryBtn("Search");
        searchBtn.addActionListener(e -> doSearch());
        JButton clearBtn = outlineBtn("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            refreshTable(library.getAllBooks());
        });
        categoryFilter = new JComboBox<>();
        categoryFilter.setBackground(Color.WHITE);
        categoryFilter.setForeground(LIB_TEXT);
        categoryFilter.setFont(FONT_BODY);
        rebuildCategoryFilter();
        categoryFilter.addActionListener(e -> doSearch());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(new Color(248, 250, 252));
        left.add(muted("Search:"));
        left.add(searchField);
        left.add(searchBtn);
        left.add(clearBtn);
        left.add(Box.createHorizontalStrut(12));
        left.add(muted("Category:"));
        left.add(categoryFilter);
        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LIB_BORDER));
        if (!currentUser.isAdmin) {
            JButton borrowBtn = primaryBtn("Borrow");
            borrowBtn.addActionListener(e -> doBorrow());
            JButton returnBtn = outlineBtn("Return");
            returnBtn.addActionListener(e -> doReturn());
            JButton renewBtn = outlineBtn("Renew");
            renewBtn.addActionListener(e -> doRenew());
            JButton detailBtn = outlineBtn("Details");
            detailBtn.addActionListener(e -> showBookDetail(getSelectedBook()));
            bar.add(borrowBtn);
            bar.add(returnBtn);
            bar.add(renewBtn);
            bar.add(detailBtn);
        }
        if (currentUser.isAdmin) {
            JButton editBtn = outlineBtn("Edit");
            editBtn.addActionListener(e -> showEditBookDialog());
            JButton deleteBtn = dangerBtn("Delete");
            deleteBtn.addActionListener(e -> doDeleteBook());
            JButton detailBtn = outlineBtn("Details");
            detailBtn.addActionListener(e -> showBookDetail(getSelectedBook()));
            bar.add(editBtn);
            bar.add(deleteBtn);
            bar.add(detailBtn);
        }
        bar.add(muted("  Double-click a row for details"));
        return bar;
    }

    private void showBookDetail(Book b) {
        if (b == null) {
            showInfo("Please select a book first.");
            return;
        }
        JDialog dlg = new JDialog(this, "Book Details", true);
        dlg.setSize(520, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(LIB_CARD_BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIB_TEAL);
        top.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel titleLbl = new JLabel(b.getTitle());
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        JLabel authorLbl = new JLabel("by " + b.getAuthor());
        authorLbl.setFont(FONT_SMALL);
        authorLbl.setForeground(new Color(200, 240, 235));
        JPanel topText = new JPanel();
        topText.setLayout(new BoxLayout(topText, BoxLayout.Y_AXIS));
        topText.setBackground(LIB_TEAL);
        topText.add(titleLbl);
        topText.add(authorLbl);
        top.add(topText, BorderLayout.CENTER);
        dlg.add(top, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(0, 2, 8, 8));
        info.setBackground(LIB_CARD_BG);
        info.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        addDetailRow(info, "ISBN:", b.getIsbn());
        addDetailRow(info, "Category:", b.getCategory());
        addDetailRow(info, "Available:", b.getAvailableCopies() + " / " + b.getTotalCopies() + " copies");
        addDetailRow(info, "Status:", b.getStatusTag());
        addDetailRow(info, "Reservation Queue:",
                b.getReservedBy().isEmpty() ? "None" : b.getReservedBy().size() + " waiting");
        addDetailRow(info, "Loan Period:", LibraryConfig.LOAN_DAYS + " days");

        if (currentUser.isAdmin && !b.getReservedBy().isEmpty()) {
            JLabel ql = new JLabel("Queue:");
            ql.setFont(FONT_HEADING);
            ql.setForeground(LIB_TEXT);
            JLabel qv = new JLabel(String.join(", ", b.getReservedBy()));
            qv.setFont(FONT_SMALL);
            qv.setForeground(LIB_MUTED);
            info.add(ql);
            info.add(qv);
        }

        dlg.add(info, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(LIB_CARD_BG);
        south.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LIB_BORDER));
        JButton closeBtn = primaryBtn("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        south.add(closeBtn);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addDetailRow(JPanel p, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(LIB_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(FONT_BODY);
        val.setForeground(LIB_TEXT);
        p.add(lbl);
        p.add(val);
    }

    private JPanel buildCategoriesPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(LIB_PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        page.add(pageTitle("Book Categories", "Books grouped by subject or genre"), BorderLayout.NORTH);
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setBackground(LIB_PAGE_BG);
        buildCategoriesContent(wrap);
        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private void buildCategoriesContent(JPanel outer) {
        outer.removeAll();
        Map<String, java.util.List<Book>> catMap = new LinkedHashMap<>();
        for (Book b : library.getAllBooks())
            catMap.computeIfAbsent(b.getCategory(), k -> new ArrayList<>()).add(b);

        String[] sumCols = { "Category", "Total Books", "Available", "Borrowed" };
        DefaultTableModel sumModel = nonEditable(sumCols);
        for (Map.Entry<String, java.util.List<Book>> e : catMap.entrySet()) {
            int avail = e.getValue().stream().mapToInt(Book::getAvailableCopies).sum();
            int total = e.getValue().stream().mapToInt(Book::getTotalCopies).sum();
            sumModel.addRow(new Object[] { e.getKey(), e.getValue().size(), avail, total - avail });
        }
        JTable sumTable = new JTable(sumModel);
        styleTable(sumTable);
        sumTable.setPreferredScrollableViewportSize(new Dimension(700, 110));

        JPanel sumCard = new JPanel(new BorderLayout(0, 8));
        sumCard.setBackground(LIB_CARD_BG);
        sumCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIB_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel sumTitle = new JLabel("Category Summary");
        sumTitle.setFont(FONT_HEADING);
        sumTitle.setForeground(LIB_TEXT);
        sumCard.add(sumTitle, BorderLayout.NORTH);
        sumCard.add(styledScroll(sumTable), BorderLayout.CENTER);
        outer.add(sumCard, BorderLayout.NORTH);

        JPanel detailWrap = new JPanel();
        detailWrap.setLayout(new BoxLayout(detailWrap, BoxLayout.Y_AXIS));
        detailWrap.setBackground(LIB_PAGE_BG);
        for (Map.Entry<String, java.util.List<Book>> e : catMap.entrySet()) {
            JPanel catCard = new JPanel(new BorderLayout(0, 8));
            catCard.setBackground(LIB_CARD_BG);
            catCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIB_BORDER, 1),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));
            catCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
            catCard.setAlignmentX(LEFT_ALIGNMENT);
            JLabel catLabel = new JLabel(e.getKey() + "  (" + e.getValue().size() + " books)");
            catLabel.setFont(FONT_HEADING);
            catLabel.setForeground(LIB_TEAL);
            catCard.add(catLabel, BorderLayout.NORTH);
            DefaultTableModel dm = nonEditable("Title", "Author", "ISBN", "Available / Total", "Status");
            for (Book b : e.getValue())
                dm.addRow(new Object[] { b.getTitle(), b.getAuthor(), b.getIsbn(),
                        b.getAvailableCopies() + " / " + b.getTotalCopies(), b.getStatusTag() });
            JTable dt = new JTable(dm);
            styleTable(dt);
            dt.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
            dt.setPreferredScrollableViewportSize(new Dimension(700, Math.min(60 + dm.getRowCount() * 30, 150)));
            catCard.add(styledScroll(dt), BorderLayout.CENTER);
            detailWrap.add(catCard);
            detailWrap.add(Box.createVerticalStrut(10));
        }
        JScrollPane ds = new JScrollPane(detailWrap);
        ds.setBorder(null);
        ds.getViewport().setBackground(LIB_PAGE_BG);
        outer.add(ds, BorderLayout.CENTER);
        outer.revalidate();
        outer.repaint();
    }

    private JPanel buildUsersPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(LIB_PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        page.add(pageTitle("Library Members", "All registered users and their borrowing status"), BorderLayout.NORTH);

        DefaultTableModel model = nonEditable("Username", "Email", "Borrowed", "Limit", "Fine (৳)");
        for (User u : userDb.values()) {
            if (u.isAdmin)
                continue;
            model.addRow(new Object[] { u.getDisplayName(), u.email, u.issuedBooks.size(), u.maxBorrowLimit,
                    String.format("%.0f", u.calculateFine()) });
        }
        JTable t = new JTable(model);
        styleTable(t);

        JPanel mainCard = new JPanel(new BorderLayout());
        mainCard.setBackground(LIB_CARD_BG);
        mainCard.setBorder(BorderFactory.createLineBorder(LIB_BORDER, 1));
        mainCard.add(styledScroll(t), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        south.setBackground(new Color(248, 250, 252));
        south.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LIB_BORDER));
        JButton viewBtn = outlineBtn("View Borrowed Books");
        viewBtn.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row == -1) {
                showInfo("Please select a member first.");
                return;
            }
            User u = userDb.get((String) model.getValueAt(row, 1));
            if (u != null)
                showUserDetails(u);
        });
        south.add(viewBtn);
        south.add(muted("  Select a member first"));
        mainCard.add(south, BorderLayout.SOUTH);
        page.add(mainCard, BorderLayout.CENTER);
        return page;
    }

    private void showUserDetails(User u) {
        JDialog dlg = new JDialog(this, "Borrowed Books — " + u.email, true);
        dlg.setSize(660, 400);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setBackground(LIB_TEAL);
        JLabel title = new JLabel("  Books borrowed by: " + u.email);
        title.setFont(FONT_BODY);
        title.setForeground(Color.WHITE);
        titleBar.add(title);
        dlg.add(titleBar, BorderLayout.NORTH);
        LocalDate today = LocalDate.now();
        DefaultTableModel dm = nonEditable("Title", "ISBN", "Due Date", "Days Left", "Status");
        for (Map.Entry<String, LocalDate> e : u.issuedBooks.entrySet()) {
            LocalDate due = e.getValue();
            Book b = library.getBook(e.getKey());
            long days = due.toEpochDay() - today.toEpochDay();
            String status = days < 0 ? "OVERDUE" : days == 0 ? "DUE TODAY" : days <= 3 ? "DUE SOON" : "On time";
            dm.addRow(new Object[] { b != null ? b.getTitle() : e.getKey(), e.getKey(), due.format(FMT),
                    days < 0 ? days + "d" : "+" + days + "d", status });
        }
        if (dm.getRowCount() == 0) {
            JLabel empty = new JLabel("  No books currently borrowed.", SwingConstants.CENTER);
            empty.setFont(FONT_BODY);
            empty.setForeground(LIB_MUTED);
            dlg.add(empty, BorderLayout.CENTER);
        } else {
            JTable dt = new JTable(dm);
            styleTable(dt);
            dlg.add(styledScroll(dt), BorderLayout.CENTER);
        }
        double fine = u.calculateFine();
        JLabel fineLabel = new JLabel(
                fine > 0 ? "  Outstanding fine: \u09F3" + String.format("%.0f", fine) : "  No outstanding fine.");
        fineLabel.setFont(FONT_BODY);
        fineLabel.setForeground(fine > 0 ? LIB_RED : LIB_GREEN);
        fineLabel.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));
        dlg.add(fineLabel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addReservationMessage(User u, Book b, int position) {
        if (u.inbox == null)
            u.inbox = new ArrayList<>();
        u.inbox.add(new Message("SYSTEM", u.email, "Reservation Confirmed: \"" + b.getTitle() + "\"",
                "You have been added to the waitlist for \"" + b.getTitle() + "\".\nQueue position: #" + position,
                Message.Type.AUTO_DUE_REMINDER));
    }

    private void offerReservation(Book b, int pos, String title, String msg) {
        if (JOptionPane.showConfirmDialog(this,
                "<html>" + msg + "<br><br>Would you like to reserve it?<br>Queue position: <b>#" + pos + "</b></html>",
                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            library.reserveBook(b.getIsbn(), currentUser.email);
            addReservationMessage(currentUser, b, pos);
            saveAll.run();
            refreshAll();
            showInfo("Reserved! You are #" + pos + " in queue.");
        }
    }

    private void doBorrow() {
        Book b = getSelectedBook();
        if (b == null) {
            showInfo("Please select a book first.");
            return;
        }
        if (!currentUser.canBorrowMore()) {
            showInfo("You have reached your borrow limit (" + currentUser.maxBorrowLimit
                    + " books). Please return a book first.");
            return;
        }
        if (currentUser.issuedBooks.containsKey(b.getIsbn())) {
            showInfo("You already have this book.");
            return;
        }
        java.util.List<String> queue = b.getReservedBy();
        boolean userIsFirst = !queue.isEmpty() && queue.get(0).equals(currentUser.email);
        boolean inQueue = queue.contains(currentUser.email);
        if (inQueue && !userIsFirst) {
            showInfo("You are #" + (queue.indexOf(currentUser.email) + 1) + " in queue.");
            return;
        }
        if (!userIsFirst && !queue.isEmpty()) {
            int cp = Math.min(queue.size(), b.getAvailableCopies());
            if (b.getAvailableCopies() == 0 || cp >= b.getAvailableCopies()) {
                offerReservation(b, queue.size() + 1, "Join Waitlist?",
                        "<b>\"" + b.getTitle() + "\"</b> is reserved by others.");
                return;
            }
        }
        if (library.issueBook(b.getIsbn())) {
            library.cancelReservation(b.getIsbn(), currentUser.email);
            currentUser.issueBook(b.getIsbn(), b.getTitle());
            saveAll.run();
            refreshAll();
            new TransactionToast(this, "borrowed", b, currentUser).setVisible(true);
        } else {
            offerReservation(b, queue.size() + 1, "Book Unavailable",
                    "<b>\"" + b.getTitle() + "\"</b> is out of stock.");
        }
    }

    private void doReturn() {
        if (currentUser.issuedBooks.isEmpty()) {
            showInfo("You have no borrowed book to return.");
            return;
        }

        String isbn;
        if (currentUser.issuedBooks.size() == 1) {
            isbn = currentUser.issuedBooks.keySet().iterator().next();
        } else {
            Book sel = getSelectedBook();
            if (sel == null || !currentUser.issuedBooks.containsKey(sel.getIsbn())) {
                showInfo("You have multiple books borrowed. Select one from the table to return.");
                return;
            }
            isbn = sel.getIsbn();
        }
        Book b = library.getBook(isbn);
        if (b == null) {
            showInfo("Book not found.");
            return;
        }
        library.returnBook(b.getIsbn());
        currentUser.returnBook(b.getIsbn(), b.getTitle());
        String nextEmail = library.notifyFirstReservation(b.getIsbn());
        if (nextEmail != null) {
            User waiting = userDb.get(nextEmail);
            if (waiting != null) {
                if (waiting.inbox == null)
                    waiting.inbox = new ArrayList<>();
                waiting.inbox.add(new Message("SYSTEM", nextEmail, "Book Available: \"" + b.getTitle() + "\"",
                        "A copy of \"" + b.getTitle() + "\" is now available. You are #1 in queue.",
                        Message.Type.AUTO_DUE_REMINDER));
            }
        }
        saveAll.run();
        refreshAll();
        updateMsgBadge();
        new TransactionToast(this, "returned", b, currentUser).setVisible(true);
    }

    private void doRenew() {
        Book b = getSelectedBook();
        if (b == null) {
            showInfo("Please select a book first.");
            return;
        }
        if (!currentUser.issuedBooks.containsKey(b.getIsbn())) {
            showInfo("You have not borrowed this book.");
            return;
        }
        if (!b.getReservedBy().isEmpty()) {
            showInfo("Cannot renew — other members are waiting for \"" + b.getTitle() + "\".");
            return;
        }
        LocalDate newDue = LocalDate.now().plusDays(LibraryConfig.LOAN_DAYS);
        currentUser.issuedBooks.put(b.getIsbn(), newDue);
        saveAll.run();
        refreshAll();
        showInfo("Renewed! New due date: " + newDue.format(FMT));
    }

    private void showReservationPopups() {
        if (currentUser.isAdmin || currentUser.inbox == null)
            return;
        // Find unread reservation-available messages
        long available = currentUser.inbox.stream()
                .filter(m -> !m.isRead() && m.getSubject().startsWith("Book Available:"))
                .count();
        if (available > 0) {
            JOptionPane.showMessageDialog(this,
                    "<html><b>" + available + " book(s) you reserved are now available!</b><br>" +
                            "Go to <b>Messages</b> or <b>Catalog</b> to borrow them before someone else does.</html>",
                    "📚 Reservation Available!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAddBookDialog() {
        BookDialog dlg = new BookDialog(this, null);
        dlg.setVisible(true);
        Book r = dlg.getResult();
        if (r != null) {
            library.addBook(r);
            refreshAll();
            showInfo("Book added: " + r.getTitle());
        }
    }

    private void showEditBookDialog() {
        Book b = getSelectedBook();
        if (b == null) {
            showInfo("Please select a book first.");
            return;
        }
        BookDialog dlg = new BookDialog(this, b);
        dlg.setVisible(true);
        Book r = dlg.getResult();
        if (r != null) {
            library.updateBook(dlg.getOldIsbn(), r.getTitle(), r.getAuthor(), r.getIsbn(), r.getTotalCopies(),
                    r.getCategory());
            refreshAll();
        }
    }

    private void doDeleteBook() {
        Book b = getSelectedBook();
        if (b == null) {
            showInfo("Please select a book first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete \"" + b.getTitle() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            library.removeBook(b.getIsbn());
            refreshAll();
        }
    }

    private void sendAutoRemindersOnStartup() {
        if (!currentUser.isAdmin)
            return;
        LocalDate today = LocalDate.now();
        for (User u : userDb.values()) {
            if (u.isAdmin || u.issuedBooks == null)
                continue;
            if (u.inbox == null)
                u.inbox = new ArrayList<>();
            for (Map.Entry<String, LocalDate> e : u.issuedBooks.entrySet()) {
                long daysLeft = e.getValue().toEpochDay() - today.toEpochDay();
                if (daysLeft > 3)
                    continue;
                String isbn = e.getKey();
                String todayStr = today.format(FMT);
                boolean sent = u.inbox.stream().anyMatch(m -> m.getType() == Message.Type.AUTO_DUE_REMINDER
                        && m.getSubject().contains(isbn) && m.getFormattedTime().startsWith(todayStr));
                if (sent)
                    continue;
                Book b = library.getBook(isbn);
                String title = b != null ? b.getTitle() : isbn;
                u.inbox.add(new Message(
                        "SYSTEM", u.email, ReminderHelper.subject(title, daysLeft), ReminderHelper.body(u.email, title,
                                e.getValue(), daysLeft, "Regards,\nLibrary Management System"),
                        Message.Type.AUTO_DUE_REMINDER));
            }
        }
        saveAll.run();
    }

    private void rebuildCategoryFilter() {
        if (categoryFilter == null)
            return;
        String sel = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        library.getAllBooks().stream().map(Book::getCategory).distinct().sorted().forEach(categoryFilter::addItem);
        if (sel != null)
            categoryFilter.setSelectedItem(sel);
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        String cat = categoryFilter != null ? (String) categoryFilter.getSelectedItem() : null;
        boolean allCats = cat == null || cat.equals("All Categories");
        java.util.List<Book> results;
        if (q.isEmpty()) {
            results = new ArrayList<>(library.getAllBooks());
        } else {
            results = library.searchByTitle(q);
            if (results.isEmpty())
                results = library.searchByAuthor(q);
            Book ex = library.getBook(q);
            if (ex != null && !results.contains(ex))
                results.add(0, ex);
        }
        if (!allCats)
            results.removeIf(b -> !b.getCategory().equals(cat));
        if (results.isEmpty() && (!q.isEmpty() || !allCats))
            showInfo("No books found.");
        refreshTable(results);
    }

    private DefaultTableModel nonEditable(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private Book getSelectedBook() {
        int row = bookTable.getSelectedRow();
        return row == -1 ? null : tableModel.getBookAt(bookTable.convertRowIndexToModel(row));
    }

    private void refreshTable(java.util.Collection<Book> books) {
        java.util.List<Book> s = new ArrayList<>(books);
        s.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        tableModel.setData(s);
    }

    private void updateMsgBadge() {
        if (messagesPanel == null || msgBadge == null)
            return;
        int n = messagesPanel.getUnreadCount();
        msgBadge.setText(" " + n + " ");
        msgBadge.setVisible(n > 0);
    }

    private void refreshAll() {
        refreshTable(library.getAllBooks());
        rebuildCategoryFilter();
        dashboard.refresh();
        if (accountPanel != null)
            accountPanel.refresh();
        if (messagesPanel != null) {
            messagesPanel.refresh();
            updateMsgBadge();
        }
        if (remindersPanel != null)
            remindersPanel.refresh();
        saveAll.run();
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Library Management System", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(LIB_TEAL);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(LIB_TEAL_DARK);
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(LIB_TEAL);
            }
        });
        return b;
    }

    private JButton outlineBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(Color.WHITE);
        b.setForeground(LIB_TEAL);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIB_TEAL, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(LIB_TEAL_LIGHT);
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(Color.WHITE);
            }
        });
        return b;
    }

    private JButton dangerBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(LIB_RED);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(LIB_RED.darker());
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(LIB_RED);
            }
        });
        return b;
    }

    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setBackground(Color.WHITE);
        tf.setForeground(LIB_TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LIB_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(LIB_MUTED);
        return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(Color.WHITE);
        t.setForeground(LIB_TEXT);
        t.setGridColor(LIB_BORDER);
        t.setFont(FONT_BODY);
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(32, 164, 147, 40));
        t.setSelectionForeground(LIB_TEXT);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                    int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setOpaque(true);
                setBackground(
                        sel ? new Color(32, 164, 147, 45) : row % 2 == 0 ? Color.WHITE : new Color(247, 251, 250));
                setForeground(LIB_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        JTableHeader h = t.getTableHeader();
        h.setBackground(LIB_TEAL);
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, LIB_TEAL_DARK));
        h.setReorderingAllowed(false);
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(null);
        return sp;
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            String s = val == null ? "" : val.toString();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(FONT_SMALL);
            lbl.setBackground(
                    sel ? new Color(32, 164, 147, 45) : row % 2 == 0 ? Color.WHITE : new Color(247, 251, 250));
            switch (s) {
                case "AVAILABLE" -> lbl.setForeground(LIB_GREEN);
                case "LOW STOCK" -> lbl.setForeground(LIB_AMBER);
                case "OUT OF STOCK" -> lbl.setForeground(LIB_RED);
                default -> lbl.setForeground(LIB_MUTED);
            }
            return lbl;
        }
    }
}

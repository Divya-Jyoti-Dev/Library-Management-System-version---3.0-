import javax.swing.*;
import java.awt.*;

public class BookDialog extends JDialog {
    private static final String[] CATEGORIES = {
        "General","Fiction","Non-Fiction","Science Fiction","Fantasy",
        "Mystery","Thriller","Horror","Romance","Biography",
        "History","Science","Technology","Religion","Poetry",
        "Children","Academic","Comics","Self-Help","Islamic"
    };

    private JTextField titleField, authorField, isbnField, copiesField;
    private JComboBox<String> categoryBox;
    private Book result;
    private final Book existing;

    public BookDialog(Frame owner, Book existing) {
        super(owner, existing == null ? "Add Book" : "Edit Book", true);
        this.existing = existing;
        setSize(440, 480); setLocationRelativeTo(owner); setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(28, 24, 10, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0); g.weightx = 1;

        titleField  = UITheme.styledField(20);
        authorField = UITheme.styledField(20);
        isbnField   = UITheme.styledField(20);
        copiesField = UITheme.styledField(20);
        categoryBox = new JComboBox<>(CATEGORIES);
        categoryBox.setBackground(UITheme.BG_CARD2);
        categoryBox.setForeground(UITheme.TEXT_PRIMARY);
        categoryBox.setFont(UITheme.FONT_BODY);
        categoryBox.setEditable(true);

        if (existing != null) {
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            isbnField.setText(existing.getIsbn());
            copiesField.setText(String.valueOf(existing.getTotalCopies()));
            categoryBox.setSelectedItem(existing.getCategory());
        }

        addRow(p, g, 0, "Title",    titleField);
        addRow(p, g, 1, "Author",   authorField);
        addRow(p, g, 2, "ISBN",     isbnField);
        addRow(p, g, 3, "Copies",   copiesField);
        addRow(p, g, 4, "Category", categoryBox);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2; g.gridx = 0;
        p.add(UITheme.label(label, UITheme.FONT_SMALL, UITheme.TEXT_MUTED), g);
        g.gridy = row * 2 + 1;
        field.setPreferredSize(new Dimension(0, 34));
        p.add(field, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton cancel = UITheme.ghostButton("Cancel");
        JButton save   = UITheme.primaryButton(existing == null ? "Add Book" : "Save Changes");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> handleSave());
        p.add(cancel); p.add(save);
        return p;
    }

    private void handleSave() {
        String title  = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn   = isbnField.getText().trim();
        Object catObj = categoryBox.getSelectedItem();
        String cat    = catObj != null ? catObj.toString().trim() : "General";
        if (cat.isEmpty()) cat = "General";

        int copies;
        try { copies = Integer.parseInt(copiesField.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Copies must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Author and ISBN are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        result = new Book(title, author, isbn, copies, cat);
        dispose();
    }

    public Book getResult()    { return result; }
    public String getOldIsbn() { return existing != null ? existing.getIsbn() : null; }
}

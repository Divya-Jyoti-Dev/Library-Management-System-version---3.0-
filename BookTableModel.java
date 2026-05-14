import javax.swing.table.AbstractTableModel;
import java.util.*;

public class BookTableModel extends AbstractTableModel {
    private static final String[] COLS = {"#","Title","Author","ISBN","Category","Copies","Status"};
    private List<Book> data = new ArrayList<>();

    public void setData(Collection<Book> books) { data = new ArrayList<>(books); fireTableDataChanged(); }
    public Book getBookAt(int row)              { return data.get(row); }
    @Override public int getRowCount()          { return data.size(); }
    @Override public int getColumnCount()       { return COLS.length; }
    @Override public String getColumnName(int c){ return COLS[c]; }

    @Override
    public Object getValueAt(int row, int col) {
        Book b = data.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> b.getTitle();
            case 2 -> b.getAuthor();
            case 3 -> b.getIsbn();
            case 4 -> b.getCategory();
            case 5 -> b.getAvailableCopies() + " / " + b.getTotalCopies();
            case 6 -> b.getStatusTag();
            default -> "";
        };
    }
}

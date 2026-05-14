import javax.swing.*;
import java.io.*;
import java.util.HashMap;

public class LibraryManagementSystem {
    private static final String LIBRARY_FILE = "library.dat";
    private static final String USERS_FILE   = "users.dat";

    private static HashMap<String, User> userDb  = new HashMap<>();
    private static Library library = new Library();

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        LibraryConfig.load();
        loadUsers();
        library.loadFromFile(LIBRARY_FILE);
        if (library.getTotalBooks() == 0) seedBooks();
        SwingUtilities.invokeLater(LibraryManagementSystem::showAuth);
    }

    private static void seedBooks() {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("books.csv"))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                String[] parts = line.split(",", 5);
                if (parts.length < 5) continue;
                String title    = parts[0].trim();
                String author   = parts[1].trim();
                String isbn     = parts[2].trim();
                int    copies   = Integer.parseInt(parts[3].trim());
                String category = parts[4].trim();
                library.addBook(new Book(title, author, isbn, copies, category));
            }
            System.out.println("Books loaded from books.csv");
        } catch (java.io.FileNotFoundException e) {
            System.err.println("books.csv not found — no seed books loaded.");
        } catch (Exception e) {
            System.err.println("Error reading books.csv: " + e.getMessage());
        }
    }

    private static void showAuth() {
        User[] box = new User[1];
        new AuthFrame(userDb, box, () ->
            SwingUtilities.invokeLater(() ->
                new MainFrame(library, box[0], userDb, LibraryManagementSystem::saveAll,
                    () -> SwingUtilities.invokeLater(LibraryManagementSystem::showAuth))));
    }

    static void saveAll() { library.saveToFile(LIBRARY_FILE); saveUsers(); LibraryConfig.save(); }

    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            userDb = (HashMap<String, User>) in.readObject();
        } catch (FileNotFoundException ignored) {
            userDb = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage()); userDb = new HashMap<>();
        }
    }

    private static void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            out.writeObject(userDb);
        } catch (IOException e) { System.err.println("Error saving users: " + e.getMessage()); }
    }
}

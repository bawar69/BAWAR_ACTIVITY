import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

abstract class LibraryItem {
    String category;
    protected String id;
    protected String title;
    protected String author;
    protected boolean isBorrowed;

    public LibraryItem() {
        category = "None";
        id = "N/A";
        title = "Untitled";
        author = "Unknown";
        isBorrowed = false;
    }

    public LibraryItem(String category, String id, String title, String author) {
        this.category = category;
        this.id = id;
        this.title = title;
        this.author = author;
        this.isBorrowed = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public synchronized String borrowBook() {
        if (isBorrowed) {
            return title + " is already borrowed.";
        } else {
            isBorrowed = true;
            return "You borrowed: " + title;
        }
    }

    public synchronized String returnBook() {
        if (!isBorrowed) {
            return title + " is already in the library.";
        } else {
            isBorrowed = false;
            return "You returned: " + title;
        }
    }

    public abstract String getDetails();
}

class Book extends LibraryItem {

    public Book() {
        super();
    }

    public Book(String category, String id, String title, String author) {
        super(category, id, title, author);
    }

    @Override
    public synchronized String getDetails() {
        String status = isBorrowed ? "Borrowed" : "Available";

        return id + " | " + title + " | " + author +
               " | " + category + " | " + status;
    }
}

public class LibraryProgram extends JFrame {

    ArrayList<Book> books = new ArrayList<>();

    JLabel titleLabel;
    JLabel statusLabel;
    JTextField bookIdField;
    JTextArea catalogArea;

    JButton borrowButton;
    JButton returnButton;
    JButton catalogButton;

    public LibraryProgram() {

        books.add(new Book("Fiction", "101",
                "Noli Me Tangere", "Jose Rizal"));

        books.add(new Book("Fantasy", "102",
                "Harry Potter", "J.K. Rowling"));

        books.add(new Book("Fiction", "103",
                "The King in yellow", "Robert Chambers"));

        setTitle("Library System");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        titleLabel = new JLabel("LIBRARY SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel();

        inputPanel.add(new JLabel("Enter Book ID:"));

        bookIdField = new JTextField(15);
        inputPanel.add(bookIdField);

        centerPanel.add(inputPanel, BorderLayout.NORTH);

        catalogArea = new JTextArea(12, 45);
        catalogArea.setEditable(false);
        catalogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(catalogArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        borrowButton = new JButton("Borrow");
        returnButton = new JButton("Return");
        catalogButton = new JButton("Catalog");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(borrowButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(catalogButton);

        statusLabel = new JLabel("Status: Ready", SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        borrowButton.addActionListener(e -> {
            String searchId = bookIdField.getText().trim();

            if (searchId.isEmpty()) {
                statusLabel.setText("Please enter a Book ID.");
                return;
            }

            Book selectedBook = findBook(searchId);

            if (selectedBook == null) {
                statusLabel.setText("Book not found.");
                return;
            }

            processTask(selectedBook, "borrow");
        });

        returnButton.addActionListener(e -> {
            String searchId = bookIdField.getText().trim();

            if (searchId.isEmpty()) {
                statusLabel.setText("Please enter a Book ID.");
                return;
            }

            Book selectedBook = findBook(searchId);

            if (selectedBook == null) {
                statusLabel.setText("Book not found.");
                return;
            }

            processTask(selectedBook, "return");
        });

        catalogButton.addActionListener(e -> {
            displayCatalog();
            statusLabel.setText("Catalog displayed.");
        });

        displayCatalog();

        setVisible(true);
    }

    public Book findBook(String searchId) {
        for (Book book : books) {
            if (book.getId().equals(searchId)) {
                return book;
            }
        }
        return null;
    }

    public void displayCatalog() {
        StringBuilder catalog = new StringBuilder();

        catalog.append("===== LIBRARY CATALOG =====\n\n");
        catalog.append("ID | TITLE | AUTHOR | CATEGORY | STATUS\n");
        catalog.append("------------------------------------------\n");

        for (Book book : books) {
            catalog.append(book.getDetails()).append("\n");
        }

        catalogArea.setText(catalog.toString());
    }

    public void processTask(Book book, String operation) {
        borrowButton.setEnabled(false);
        returnButton.setEnabled(false);
        catalogButton.setEnabled(false);

        statusLabel.setText("Status: Processing...");

        TaskProcessor task = new TaskProcessor(
                book, operation, statusLabel,
                borrowButton, returnButton,
                catalogButton, this
        );

        Thread thread = new Thread(task);
        thread.start();
    }

    static class TaskProcessor implements Runnable {

        Book book;
        String operation;

        JLabel statusLabel;
        JButton borrowButton;
        JButton returnButton;
        JButton catalogButton;
        LibraryProgram main;

        public TaskProcessor(Book book, String operation,
                JLabel statusLabel,
                JButton borrowButton,
                JButton returnButton,
                JButton catalogButton,
                LibraryProgram main) {

            this.book = book;
            this.operation = operation;
            this.statusLabel = statusLabel;
            this.borrowButton = borrowButton;
            this.returnButton = returnButton;
            this.catalogButton = catalogButton;
            this.main = main;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    final int progress = i * 20;

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(
                                "Status: Processing... " + progress + "%"
                        );
                    });
                }

                String result;
                if (operation.equals("borrow")) {
                    result = book.borrowBook();
                } else {
                    result = book.returnBook();
                }

                final String message = result;

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(message);
                    main.displayCatalog();

                    borrowButton.setEnabled(true);
                    returnButton.setEnabled(true);
                    catalogButton.setEnabled(true);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Task interrupted!");

                    borrowButton.setEnabled(true);
                    returnButton.setEnabled(true);
                    catalogButton.setEnabled(true);
                });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryProgram();
        });
    }
}
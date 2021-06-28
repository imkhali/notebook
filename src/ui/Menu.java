package ui;

import Model.Note;
import Model.Notebook;

import java.io.FilterInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.*;
import java.util.stream.Collectors;

public class Menu {
    private final String SEPARATOR = System.lineSeparator();
    private Notebook notebook;
    private Connection conn;

    public Menu() {
        setupConnection();
        try {
            notebook = new Notebook(conn);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Unable to initialize NotebookWithDB " + e);
        }
        run();
    }

    public static void main(String[] args) {
        new Menu();
    }

    /**
     * setup connection with database on disk
     */
    public void setupConnection() {
        System.out.println("NotesDBJDBC.<init> starting...");
        System.out.println("Loading Driver Class");

        try {
            // Load the mysql driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Enable logging
            DriverManager.setLogWriter(new PrintWriter(System.err));

            System.out.println("Getting Connection");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost/mynotebook?"
                            + "user=myuser&password=Solom2020");

            // Optionally, get sql warnings
            SQLWarning warn = conn.getWarnings();
            while (warn != null) {
                System.out.println("SQLState: " + warn.getSQLState());
                System.out.println("Message: " + warn.getMessage());
                System.out.println("Vendor: " + warn.getErrorCode());
                System.out.println();
                warn = warn.getNextWarning();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("FAILED: " + e);
            throw new IllegalStateException(e);
        } catch (SQLException e) {
            System.out.println("Database access failed " + e);
        }
    }

    /**
     * run the console interface to the user, prompt the user to
     * choose what to do and invoke actions correspondingly
     */
    private void run() {
        // a wrapper around System.in, so we don't close it if we want to close close scanner in runtime
        Scanner scanner = new Scanner(new FilterInputStream(System.in) {
            public void close() {
            }
        });
        try (scanner) {
            while (true) {
                displayMenu();
                if (scanner.hasNextInt()) {
                    int choice = scanner.nextInt();
                    switch (choice) {
                        case 1 -> showNotes();
                        case 2 -> showNotes(searchNotes());
                        case 3 -> addNote();
                        case 4 -> modifyNote();
                        case 5 -> removeNote();
                        case 6 -> quit();
                        default -> System.out.println(choice + " is not corresponding to a list item");
                    }
                } else {
                    System.out.println("you have specified invalid choice");
                }
            }
        }
    }

    /**
     * display the list items that the user can choose from
     */
    private void displayMenu() {
        System.out.print(SEPARATOR + "Notebook Menu"
                + SEPARATOR
                + SEPARATOR + "\t1. Show all Notes"
                + SEPARATOR + "\t2. Search Notes"
                + SEPARATOR + "\t3. Add Note"
                + SEPARATOR + "\t4. Modify Note"
                + SEPARATOR + "\t5. remove Note"
                + SEPARATOR + "\t6. Quit"
                + SEPARATOR);
    }

    // TODO: check later if needed
    private void displaySubMenu() {
        System.out.print(SEPARATOR + "Notebook Menu"
                + SEPARATOR
                + SEPARATOR + "\t1. Return to Main Menu"
                + SEPARATOR + "\t2. Quit"
                + SEPARATOR);
    }

    /**
     * display the given notes on the console
     *
     * @param notes list of notes to be displayed
     */
    private void showNotes(List<Note> notes) {
        if (notes == null)
            return;

        if (notes.size() > 0) {
            for (Note note : notes)
                System.out.println(note);
        } else {
            System.out.println("No notes to show!");
        }
    }

    /**
     * display all notebook's notes
     */
    private void showNotes() {
        showNotes(notebook.getNotes());
    }

    private void addNote() {
        String memo = promptForMemo();
        Set<String> tags = promptForTags();

        if (memo != null & tags != null) {
            try {
                notebook.addNote(memo, tags);
                System.out.println("note was successfully added!");
            } catch (SQLException e) {
                System.out.println("Error adding note: " + e.getMessage());
            }
        }
    }

    /**
     * remove note by id, if id matches a note id, otherwise do nothing
     */
    private void removeNote() {
        try {
            int noteID = promptForID();
            if (notebook.removeNote(noteID)) {
                System.out.println("note deleted successfully");
            } else {
                System.out.println("note not found!");
            }
        } catch (SQLException | InputMismatchException e) {
            System.out.println("Error deleting note: " + e.getMessage());
        }
    }

    private void modifyNote() {
        try {
            int noteID = promptForID(); // why this not asking me to handle mismatch exception???
            String memo = promptForMemo();
            Set<String> tags = promptForTags();

            if (!notebook.containsNoteWithID(noteID)) {
                System.out.println("note with id: " + noteID + " not in notebook");
                return;
            }
            if (memo != null)
                notebook.updateNoteMemo(noteID, memo);
            if (tags != null)
                notebook.updateNoteTags(noteID, tags);
            System.out.println("note updated successfully");
        } catch (SQLException | InputMismatchException e) {
            System.out.println("Error updating note! " + e.getMessage());
        }
    }

    private List<Note> searchNotes() {
        String filter;
        System.out.print("Search for: ");

        Scanner scanner = new Scanner(new FilterInputStream(System.in) {
            public void close() {
            }
        });

        try (scanner) {
            if (scanner.hasNextLine()) {
                filter = scanner.nextLine().strip();
                return notebook.search(filter);
            }
            return null;
        }
    }

    /**
     * prompt the user for integer that represent a note ID
     *
     * @return noteID that represent a note ID
     * @throws InputMismatchException if the user input a non-integer value
     */
    private int promptForID() throws InputMismatchException {
        int noteID;
        // ensures we don't close System.in
        Scanner scanner = new Scanner(new FilterInputStream(System.in) {
            public void close() {
            }
        });

        System.out.print("Please enter an ID: ");
        if (!scanner.hasNextInt())
            throw new InputMismatchException("Wrong input: expected an integer!");
        noteID = scanner.nextInt();
        scanner.nextLine(); // clears the buffer
        return noteID;
    }

    /**
     * prompt the user for a text (one or more lines) that represent a note Memo
     *
     * @return String that represents a note memo
     */
    public String promptForMemo() {
        String curLine;
        StringBuilder newMemo = new StringBuilder();
        // ensures we don't close System.in
        Scanner scanner = new Scanner(new FilterInputStream(System.in) {
            public void close() {
            }
        });

        System.out.println("Please enter a Memo: ");
//        if (!scanner.hasNextLine())
//            throw new InputMismatchException("A valid input was not given!");
        while (scanner.hasNextLine()) {
            curLine = scanner.nextLine();
            if (curLine == null || curLine.isBlank()) break;
            newMemo.append(curLine).append(SEPARATOR);
        }
        return newMemo.toString().strip();
    }

    /**
     * prompt the user for a comma separated set of note keywords
     *
     * @return set of strings that represent note keywords
     */
    public Set<String> promptForTags() {
        Set<String> newTags;
        String curLine;
        StringBuilder newTagsString = new StringBuilder();

        // ensures we don't close System.in
        Scanner scanner = new Scanner(new FilterInputStream(System.in) {
            public void close() {
            }
        });

        System.out.println("Please enter tags separated by comma: ");
//        if (!scanner.hasNextLine())
//            throw new InputMismatchException("A valid input was not given!");
        while (scanner.hasNextLine()) {
            curLine = scanner.nextLine();
            if (curLine == null || curLine.isBlank()) break;
            newTagsString.append(curLine).append(System.lineSeparator());
        }
        newTags = Arrays.stream(newTagsString.toString().split(","))
                .map(String::strip).collect(Collectors.toSet());
        return newTags;
    }

    // close connection and quit
    private void quit() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Thank you for using your notebook today");
        System.exit(0);
    }
}

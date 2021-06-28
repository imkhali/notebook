package Model;

import org.jetbrains.annotations.Contract;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
// TODO: think about a refactoring, observer that get updates of notebook changes (in-memory)
//       and then updates the database
public class Notebook {
    private static final int BASE_INDEX = 10001;
    private final List<Note> notes;

    private static final int NUM_COLS_TABLE = 4;

    final static String SQL_INSERT_NOTE =
            "insert into mynotebook.notes " +
                    " values (?, ?, ?, ?)";
    private final PreparedStatement addNoteStmt;
    private final PreparedStatement deleteNoteStmt;
    private final PreparedStatement setMemoStmt;
    private final PreparedStatement setTagsStmt;

    public Notebook(Connection conn) throws SQLException {
        this.notes = new ArrayList<>();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from mynotebook.notes");

        /* populate notes in-memory from database each time the program starts */
        while (rs.next()) {
            int id = rs.getInt("id");
            LocalDate dateCreated = rs.getDate("dateCreated").toLocalDate();
            String memo = rs.getString("memo");
            Set<String> tags = Arrays.stream(rs.getString("tags").split(",\\s?")).collect(Collectors.toSet());

            // populating in-memory notes from database
            Note note = new Note(id, dateCreated, memo, tags);
            notes.add(note);
        }
        rs.close();
        stmt.close();

        // setup the preparedStatements now so we don't have to
        // create them each time needed.
        addNoteStmt = conn.prepareStatement(SQL_INSERT_NOTE);
        deleteNoteStmt = conn.prepareStatement(
                "delete from mynotebook.notes where id = ?");
        setMemoStmt = conn.prepareStatement(
                "update mynotebook.notes set memo = ? where id = ?");
        setTagsStmt = conn.prepareStatement(
                "update mynotebook.notes set tags = ? where id = ?");
    }

    /* Getters and Setters */
    public List<Note> getNotes() {
        return notes;
    }


    /**
     * Add a new note to notes both in-memory and database on disk (keep both synchronized)
     * if note is already in notes, do nothing (No duplicates)
     * @param memo memo of the new note
     * @param tags tags of the new note
     * @throws SQLException if failed to add to database
     */
    @Contract(mutates = "this")
    public synchronized void addNote(String memo, Set<String> tags) throws SQLException {
        int noteID = getNextID();
        Note note = new Note(noteID, memo, tags);

        if (notes.contains(note)) {
            System.out.println("note with id " + noteID + " is already in notes");
            return;
        }

        // add to DB,
        int i = 1; // it seems adding works only by numeric index starts at 1
        addNoteStmt.setInt(i++, note.getID());
        addNoteStmt.setDate(i++, java.sql.Date.valueOf(note.getDateCreated()));
        addNoteStmt.setString(i++, note.getMemo());
        addNoteStmt.setString(i++, String.join(", ", note.getTags()));
        --i;

        if (i != 4) {
            System.out.println("Warning: not enough fields set! i = " + i);
        }

        // store in DB
        addNoteStmt.executeUpdate();
        // add in in-memory list
        notes.add(note);
    }

    /**
     * gives an ID to a new note to be added to notes
     * @return {@code Model.Notebook.BASE_INDEX} if first note, otherwise one plus number of notes in notebook
     */
    private int getNextID() {
        /* this approach has an issue, if we remove a note, we end up duplicating ids, immutability is a heaven */
        // return notes.size() + BASE_INDEX;
        return notes.size() == 0 ? BASE_INDEX : notes.get(notes.size() - 1).getID() + 1; // this makes IDs unique but with gaps
    }

    /**
     * remove note of ID {@code noteID} from notes both in-memory and database on disk
     * @param noteID integer that represent a note ID
     * @return {@code true} if note is successfully deleted from notes
     * @throws SQLException in case unable to remove note in database,
     *                      in which case keep in-memory storage unchanged too
     */
    public synchronized boolean removeNote(int noteID) throws SQLException {
        Note noteToDelete = getNoteByID(noteID);

        if (!notes.contains(noteToDelete)) {
            throw new SQLException("Note with id " + noteID + " not in in-memory DB");
        }

        deleteNoteStmt.setInt(1, noteID);
        int n = deleteNoteStmt.executeUpdate();
        if (n != 1) { // not just one row?
            /* CANT HAPPEN */
            throw new SQLException("ERROR: deleted " + n + " rows!!");
        }

        // IFF deleted from the DB, remove also from in-memory list
        return notes.remove(noteToDelete);
    }

    /**
     * sets the memo of note with id {@code noteID} to {@code newMemo}
     * @param noteID integer that represent a note ID
     * @param newMemo new memo of the note
     * @throws SQLException in case unable to update note's memo in database,
     *                      in which case keep in-memory storage unchanged too
     */
    @Contract(mutates = "this")
    public synchronized void updateNoteMemo(int noteID, String newMemo)
            throws SQLException {
        Note noteToUpdate = getNoteByID(noteID);

        // confirm note in notes
        if (!notes.contains(noteToUpdate)) {
            throw new SQLException("note with id: " + noteID + " not in in-memory DB");
        }

        // Change it in DB first, if this fails, the info in
        // the in-memory copy won't be changed either
        setMemoStmt.setString(1, newMemo);
        setMemoStmt.setInt(2, noteID);

        // change in in-memory
        noteToUpdate.setMemo(newMemo);
    }

    /**
     * sets the memo of note with id {@code noteID} to {@code newTags}
     * @param noteID integer that represent a note ID
     * @param newTags new tags of the note
     * @throws SQLException in case unable to update note's tags in database,
     *                      in which case keep in-memory storage unchanged too
     */
    @Contract(mutates = "this")
    public synchronized void updateNoteTags(int noteID, Set<String> newTags)
            throws SQLException {
        Note noteToUpdate = getNoteByID(noteID);

        // confirm note in notes
        if (!notes.contains(noteToUpdate)) {
            throw new SQLException("note with id: " + noteID + " not in in-memory DB");
        }

        // Change it in DB first, if this fails, the info in
        // the in-memory copy won't be changed either
        setTagsStmt.setString(1, String.join(", ", newTags));
        setTagsStmt.setInt(2, noteID);

        // change in in-memory
        noteToUpdate.setTags(newTags);
    }

    /**
     * retun list of notes that match the string {@code filter} in note's memo or tags
     * @param filter a string to match against
     * @return list of notes, could be empty if none matches
     */
    @Contract("_ -> !null")
    public List<Note> search(String filter) {
        List<Note> results = new ArrayList<>();
        for (Note note : notes)
            if (note.match(filter))
                results.add(note);
        return results;
    }

    /**
     * return as an object the note with ID {@code noteID}
     * @param noteID integer that represent a note ID
     * @return Note or null if no such note
     */
    private Note getNoteByID(int noteID) {
        for (Note note : notes)
            if (note.getID() == noteID)
                return note;
        return null;
    }

    /**
     * return true if notes contain note with ID {@code noteID}
     * @param noteID integer that represent a note ID
     * @return Note or null if no such note
     */
    public boolean containsNoteWithID(int noteID) {
        return notes.contains(getNoteByID(noteID));
    }

    /**
     * return true if notes contain note with ID {@code noteID}
     * @param note a Note to check if in notes or not
     * @return Note or null if no such note
     */
    public boolean contains(Note note) {
        return notes.contains(note);
    }

    /**
     * gives the number of notes in {@code notes}, should be same to number of notes in database
     * @return integer that represent number of notes
     */
    public int size() {
        return notes.size();
    }

}


package test;

import Model.Note;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
// TODO: complete this, below code is for first version of Notebook (without database storage)
public class NotebookTest {
//    private Notebook testNotebook;
//    private Note testNote;
//    @Before
//    public void setup() {
//        testNotebook = new Notebook();
//        testNote = new Note(1, "a  test note", new HashSet<>(Arrays.asList("test", "note")));
//    }
//
//    @Test
//    public void testAddNoteNotThere() {
//        checkEmptyDoesntContain(testNote);
//        testNotebook.addNote(testNote);
//        checkContainsOnce(testNote);
//    }
//
//    @Test
//    public void testAddNoteThere() {
//        checkEmptyDoesntContain(testNote);
//        testNotebook.addNote(testNote);
//        checkContainsOnce(testNote);
//        testNotebook.addNote(testNote);
//        checkContainsOnce(testNote);
//    }
//
//    @Test
//    public void testRemoveNote() {
//        checkEmptyDoesntContain(testNote);
//        testNotebook.addNote(testNote);
//        checkContainsOnce(testNote);
//        testNotebook.removeNote(testNote);
//        checkEmptyDoesntContain(testNote);
//    }
//
//    @Test
//    public void testUpdateNote() {
//        String oldMemo = testNote.getMemo();
//        Set<String> oldTags = testNote.getTags();
//
//        String updatedMemo = "updated note";
//        Set<String> updatedTags = new HashSet<>(Arrays.asList("updated", "note"));
//
//        testNotebook.addNote(testNote);
//        assertEquals(oldMemo, testNote.getMemo());
//        assertEquals(oldTags, testNote.getTags());
//
//        testNotebook.updateNote(testNote, updatedMemo, updatedTags);
//        assertEquals(updatedMemo, testNote.getMemo());
//        assertEquals(updatedTags, testNote.getTags());
//    }
//
//    @Test
//    public void testSearch() {
//        List<Note> searchResults;
//
//        testNotebook.addNote(testNote);
//        Note testNote2 = new Note(2, "another note", new HashSet<>(Arrays.asList("note2", "other")));
//        testNotebook.addNote(testNote2);
//
//        searchResults = testNotebook.search("Not in note");
//        assertEquals(0, searchResults.size());
//
//        searchResults = testNotebook.search("note");
//        assertEquals(2, searchResults.size());
//
//        searchResults = testNotebook.search("other");
//        assertEquals(1, searchResults.size());
//
//    }
//
//    @Test
//    public void testGetNoteByID() {
//        Note returnedNote;
//        testNotebook.addNote(testNote);
//
//        returnedNote = testNotebook.getNoteByID(testNote.getID());
//        assertEquals(testNote, returnedNote);
//    }
//
//    private void checkContainsOnce(Note note) {
//        assertEquals(1, testNotebook.size());
//        assertTrue(testNotebook.contains(note));
//    }
//
//    private void checkEmptyDoesntContain(Note note) {
//        assertEquals(0, testNotebook.size());
//        assertFalse(testNotebook.contains(note));
//    }
}

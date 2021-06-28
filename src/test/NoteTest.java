package test;

import Model.Note;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;


public class NoteTest {
    private Note testNote;

    @Before
    public void setup() {
        testNote = new Note(1,"this is a test note", new HashSet<>(Arrays.asList("first", "test", "note")));
    }

    @Test
    public void testConstructor() {
        LocalDate dateTestNote = LocalDate.now();
        // assertEquals(testNote.getDateCreated(), dateTestNote);
        assertEquals(testNote.getMemo(), "this is a test note");
        assertEquals(testNote.getTags(), new HashSet<>(Arrays.asList("first", "test", "note")));
    }

    @Test
    public void testMatchMemo() {
        assertFalse(testNote.match("Not there"));
        assertFalse(testNote.match("test notes"));
        assertFalse(testNote.match("Test note", true));
        assertTrue(testNote.match("Test note", false));
        assertTrue(testNote.match("test note"));
    }

    @Test
    public void testMatchTags() {
        assertFalse(testNote.match("second"));
        assertTrue(testNote.match("note"));
    }
}

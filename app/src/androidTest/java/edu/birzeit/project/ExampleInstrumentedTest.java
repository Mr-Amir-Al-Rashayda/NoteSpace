package edu.birzeit.project;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.models.Note;
import edu.birzeit.project.utils.SessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void databaseAuthenticatesAndIsolatesNotesByOwner() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("edu.birzeit.project", appContext.getPackageName());

        DatabaseHelper database = DatabaseHelper.getInstance(appContext);
        String suffix = String.valueOf(System.nanoTime());
        String firstEmail = "first" + suffix + "@example.com";
        String secondEmail = "second" + suffix + "@example.com";
        assertTrue(database.createUser(firstEmail, "Alice", "Stone", "Secure1"));
        assertTrue(database.createUser(secondEmail, "Brian", "Clark", "Secure1"));
        assertTrue(database.authenticate(firstEmail, "Secure1"));
        assertFalse(database.authenticate(firstEmail, "Wrong1"));

        long now = System.currentTimeMillis();
        Note firstNote = new Note(0, firstEmail, "Private title", "Searchable body",
                now, now, false, "Study");
        Note secondNote = new Note(0, secondEmail, "Other user", "Hidden body",
                now + 1, now + 1, true, "Work");
        assertTrue(database.insertNote(firstNote) > 0);
        assertTrue(database.insertNote(secondNote) > 0);

        List<Note> firstNotes = database.getNotes(firstEmail, false, "searchable", "Study",
                SessionManager.SORT_DATE);
        assertEquals(1, firstNotes.size());
        assertEquals(firstNote.getId(), firstNotes.get(0).getId());
        assertNull(database.getNote(firstNote.getId(), secondEmail));
        assertFalse(database.setFavorite(firstNote.getId(), secondEmail, true));
        assertFalse(database.deleteNote(firstNote.getId(), secondEmail));
        assertTrue(database.deleteNote(firstNote.getId(), firstEmail));
        assertTrue(database.deleteNote(secondNote.getId(), secondEmail));
    }
}

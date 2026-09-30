package edu.birzeit.project.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.birzeit.project.R;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.models.Note;
import edu.birzeit.project.utils.SessionManager;

public class AddEditNoteActivity extends AppCompatActivity {
    public static final String EXTRA_NOTE_ID = "note_id";
    private static final long NEW_NOTE = -1L;

    private TextInputLayout titleLayout, contentLayout;
    private TextInputEditText titleInput, contentInput;
    private MaterialAutoCompleteTextView tagInput;
    private DatabaseHelper database;
    private String ownerEmail;
    private Note existingNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);
        ownerEmail = sessionManager.getCurrentUserEmail();
        if (ownerEmail == null) { finish(); return; }
        setContentView(R.layout.activity_add_edit_note);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titleLayout = findViewById(R.id.titleLayout);
        contentLayout = findViewById(R.id.contentLayout);
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        tagInput = findViewById(R.id.tagInput);
        database = DatabaseHelper.getInstance(this);
        tagInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                database.getTags(ownerEmail)));

        long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, NEW_NOTE);
        if (noteId != NEW_NOTE) {
            existingNote = database.getNote(noteId, ownerEmail);
            if (existingNote == null) {
                Snackbar.make(findViewById(R.id.noteEditorRoot), R.string.note_not_found, Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override public void onDismissed(Snackbar transientBottomBar, int event) { finish(); }
                        }).show();
                return;
            }
            toolbar.setTitle(R.string.edit_note);
            titleInput.setText(existingNote.getTitle());
            contentInput.setText(existingNote.getContent());
            tagInput.setText(existingNote.getTag());
        } else toolbar.setTitle(R.string.add_note);
        findViewById(R.id.saveNoteButton).setOnClickListener(view -> saveNote());
    }

    private void saveNote() {
        titleLayout.setError(null);
        contentLayout.setError(null);
        String title = textOf(titleInput).trim();
        String content = textOf(contentInput).trim();
        String tag = textOf(tagInput).trim();
        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.error_title_required));
            titleInput.requestFocus();
            return;
        }
        if (content.isEmpty()) {
            contentLayout.setError(getString(R.string.error_content_required));
            contentInput.requestFocus();
            return;
        }
        long now = System.currentTimeMillis();
        boolean successful;
        if (existingNote == null) {
            Note note = new Note(0, ownerEmail, title, content, now, now, false, tag);
            successful = database.insertNote(note) != -1;
        } else {
            existingNote.setTitle(title);
            existingNote.setContent(content);
            existingNote.setTag(tag);
            existingNote.setUpdatedAt(now);
            successful = database.updateNote(existingNote, ownerEmail);
        }
        if (successful) {
            setResult(RESULT_OK);
            Snackbar.make(findViewById(R.id.noteEditorRoot), existingNote == null
                            ? R.string.note_added : R.string.note_updated, Snackbar.LENGTH_SHORT)
                    .addCallback(new Snackbar.Callback() {
                        @Override public void onDismissed(Snackbar transientBottomBar, int event) { finish(); }
                    }).show();
        } else Snackbar.make(findViewById(R.id.noteEditorRoot), R.string.error_save_note, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private String textOf(android.widget.EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}

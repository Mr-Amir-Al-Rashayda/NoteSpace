package edu.birzeit.project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.birzeit.project.models.Note;
import edu.birzeit.project.models.User;
import edu.birzeit.project.utils.PasswordUtils;
import edu.birzeit.project.utils.SessionManager;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String USER_EMAIL = "email";
    private static final String USER_FIRST_NAME = "first_name";
    private static final String USER_LAST_NAME = "last_name";
    private static final String USER_PASSWORD = "password_hash";

    private static final String TABLE_NOTES = "notes";
    private static final String NOTE_ID = "id";
    private static final String NOTE_OWNER = "owner_email";
    private static final String NOTE_TITLE = "title";
    private static final String NOTE_CONTENT = "content";
    private static final String NOTE_CREATED = "created_at";
    private static final String NOTE_UPDATED = "updated_at";
    private static final String NOTE_FAVORITE = "is_favorite";
    private static final String NOTE_TAG = "tag";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + USER_EMAIL + " TEXT PRIMARY KEY COLLATE NOCASE, "
                + USER_FIRST_NAME + " TEXT NOT NULL, "
                + USER_LAST_NAME + " TEXT NOT NULL, "
                + USER_PASSWORD + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_NOTES + " ("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_OWNER + " TEXT NOT NULL COLLATE NOCASE, "
                + NOTE_TITLE + " TEXT NOT NULL, "
                + NOTE_CONTENT + " TEXT NOT NULL, "
                + NOTE_CREATED + " INTEGER NOT NULL, "
                + NOTE_UPDATED + " INTEGER NOT NULL, "
                + NOTE_FAVORITE + " INTEGER NOT NULL DEFAULT 0, "
                + NOTE_TAG + " TEXT NOT NULL DEFAULT '', "
                + "FOREIGN KEY(" + NOTE_OWNER + ") REFERENCES " + TABLE_USERS
                + "(" + USER_EMAIL + ") ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_notes_owner ON " + TABLE_NOTES + "(" + NOTE_OWNER + ")");
        db.execSQL("CREATE INDEX idx_notes_owner_favorite ON " + TABLE_NOTES
                + "(" + NOTE_OWNER + ", " + NOTE_FAVORITE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the initial schema. Future migrations belong here without dropping user data.
    }

    public boolean userExists(String email) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_USERS, new String[]{USER_EMAIL},
                USER_EMAIL + " = ?", new String[]{normalizeEmail(email)}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    public boolean createUser(String email, String firstName, String lastName, String password) {
        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, normalizeEmail(email));
        values.put(USER_FIRST_NAME, firstName.trim());
        values.put(USER_LAST_NAME, lastName.trim());
        values.put(USER_PASSWORD, PasswordUtils.hash(password));
        return getWritableDatabase().insert(TABLE_USERS, null, values) != -1;
    }

    public boolean authenticate(String email, String password) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_USERS, new String[]{USER_PASSWORD},
                USER_EMAIL + " = ?", new String[]{normalizeEmail(email)}, null, null, null, "1")) {
            return cursor.moveToFirst() && PasswordUtils.verify(password, cursor.getString(0));
        }
    }

    public User getUser(String email) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_USERS,
                new String[]{USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME}, USER_EMAIL + " = ?",
                new String[]{normalizeEmail(email)}, null, null, null, "1")) {
            if (!cursor.moveToFirst()) return null;
            return new User(cursor.getString(0), cursor.getString(1), cursor.getString(2));
        }
    }

    public boolean updateUser(String email, String firstName, String lastName, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(USER_FIRST_NAME, firstName.trim());
        values.put(USER_LAST_NAME, lastName.trim());
        if (newPassword != null && !newPassword.isEmpty()) {
            values.put(USER_PASSWORD, PasswordUtils.hash(newPassword));
        }
        return getWritableDatabase().update(TABLE_USERS, values, USER_EMAIL + " = ?",
                new String[]{normalizeEmail(email)}) == 1;
    }

    public long insertNote(Note note) {
        ContentValues values = noteValues(note, true);
        long id = getWritableDatabase().insert(TABLE_NOTES, null, values);
        if (id != -1) note.setId(id);
        return id;
    }

    public boolean updateNote(Note note, String ownerEmail) {
        ContentValues values = noteValues(note, false);
        return getWritableDatabase().update(TABLE_NOTES, values,
                NOTE_ID + " = ? AND " + NOTE_OWNER + " = ?",
                new String[]{String.valueOf(note.getId()), normalizeEmail(ownerEmail)}) == 1;
    }

    public boolean setFavorite(long noteId, String ownerEmail, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put(NOTE_FAVORITE, favorite ? 1 : 0);
        return getWritableDatabase().update(TABLE_NOTES, values,
                NOTE_ID + " = ? AND " + NOTE_OWNER + " = ?",
                new String[]{String.valueOf(noteId), normalizeEmail(ownerEmail)}) == 1;
    }

    public boolean deleteNote(long noteId, String ownerEmail) {
        return getWritableDatabase().delete(TABLE_NOTES,
                NOTE_ID + " = ? AND " + NOTE_OWNER + " = ?",
                new String[]{String.valueOf(noteId), normalizeEmail(ownerEmail)}) == 1;
    }

    public Note getNote(long noteId, String ownerEmail) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_NOTES, null,
                NOTE_ID + " = ? AND " + NOTE_OWNER + " = ?",
                new String[]{String.valueOf(noteId), normalizeEmail(ownerEmail)}, null, null, null, "1")) {
            return cursor.moveToFirst() ? readNote(cursor) : null;
        }
    }

    public List<Note> getNotes(String ownerEmail, boolean favoritesOnly, String search,
                               String tag, String sortType) {
        List<String> clauses = new ArrayList<>();
        List<String> arguments = new ArrayList<>();
        clauses.add(NOTE_OWNER + " = ?");
        arguments.add(normalizeEmail(ownerEmail));
        if (favoritesOnly) clauses.add(NOTE_FAVORITE + " = 1");
        if (search != null && !search.trim().isEmpty()) {
            clauses.add("(LOWER(" + NOTE_TITLE + ") LIKE ? OR LOWER(" + NOTE_CONTENT + ") LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            arguments.add(pattern);
            arguments.add(pattern);
        }
        if (tag != null && !tag.trim().isEmpty()) {
            clauses.add(NOTE_TAG + " = ? COLLATE NOCASE");
            arguments.add(tag.trim());
        }
        String order = SessionManager.SORT_ALPHABETICAL.equals(sortType)
                ? NOTE_TITLE + " COLLATE NOCASE ASC, " + NOTE_CREATED + " DESC"
                : NOTE_CREATED + " DESC, " + NOTE_ID + " DESC";
        String selection = android.text.TextUtils.join(" AND ", clauses);
        List<Note> notes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_NOTES, null, selection,
                arguments.toArray(new String[0]), null, null, order)) {
            while (cursor.moveToNext()) notes.add(readNote(cursor));
        }
        return notes;
    }

    public List<String> getTags(String ownerEmail) {
        List<String> tags = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(true, TABLE_NOTES,
                new String[]{NOTE_TAG}, NOTE_OWNER + " = ? AND TRIM(" + NOTE_TAG + ") != ''",
                new String[]{normalizeEmail(ownerEmail)}, null, null, NOTE_TAG + " COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) tags.add(cursor.getString(0));
        }
        return tags;
    }

    private ContentValues noteValues(Note note, boolean includeOwnerAndCreated) {
        ContentValues values = new ContentValues();
        if (includeOwnerAndCreated) {
            values.put(NOTE_OWNER, normalizeEmail(note.getOwnerEmail()));
            values.put(NOTE_CREATED, note.getCreatedAt());
        }
        values.put(NOTE_TITLE, note.getTitle().trim());
        values.put(NOTE_CONTENT, note.getContent().trim());
        values.put(NOTE_UPDATED, note.getUpdatedAt());
        values.put(NOTE_FAVORITE, note.isFavorite() ? 1 : 0);
        values.put(NOTE_TAG, note.getTag() == null ? "" : note.getTag().trim());
        return values;
    }

    private Note readNote(Cursor cursor) {
        return new Note(cursor.getLong(cursor.getColumnIndexOrThrow(NOTE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(NOTE_OWNER)),
                cursor.getString(cursor.getColumnIndexOrThrow(NOTE_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(NOTE_CONTENT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(NOTE_CREATED)),
                cursor.getLong(cursor.getColumnIndexOrThrow(NOTE_UPDATED)),
                cursor.getInt(cursor.getColumnIndexOrThrow(NOTE_FAVORITE)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(NOTE_TAG)));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

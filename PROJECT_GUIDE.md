# NoteSpace Project Guide

## 1. Architecture

The app is a small Java/XML Android application. Activities own full-screen flows, fragments own the main app destinations, `NoteAdapter` renders note cards, `DatabaseHelper` owns SQLite operations, and utility classes contain session, validation, password, date, and email logic. The design deliberately avoids a remote backend and complex framework layers.

## 2. Main activities

- `SignInActivity`: launcher, credential validation, Remember Me, and session start.
- `SignUpActivity`: registration form and all required validation.
- `MainActivity`: toolbar, search, navigation drawer, FAB, fragment container, drawer header, logout, and back navigation.
- `AddEditNoteActivity`: one owner-safe editor used for both adding and editing notes.

## 3. Main fragments

- `AllNotesFragment`: all notes plus dynamic tag chips.
- `FavoritesFragment`: favorite notes only.
- `SortedNotesFragment`: date or alphabetical sorting.
- `ProfileFragment`: profile and optional password updates.
- `NoteDetailFragment`: full note content and edit/delete/email/favorite actions.
- `BaseNotesFragment`: shared RecyclerView, query, empty-state, CRUD action, and favorite logic for the three list screens.

## 4. Database tables

`DatabaseHelper` creates `users` and `notes` in `notes.db`.

`users`: `email` primary key (case-insensitive), `first_name`, `last_name`, and `password_hash`.

`notes`: autoincrement `id`, `owner_email`, `title`, `content`, `created_at`, `updated_at`, `is_favorite`, and `tag`. `owner_email` is a foreign key to `users.email` with cascade delete. Owner and favorite indexes keep common list queries fast.

## 5. Authentication

Registration normalizes emails to lowercase and stores a PBKDF2-HMAC-SHA256 salted password hash through `PasswordUtils`. Sign-in fetches the hash for the normalized email and verifies it without storing or displaying the plain password. Successful authentication starts a session and clears the authentication activities from the back stack.

## 6. Remember Me

After a successful login, `SessionManager.updateRememberedEmail()` stores the email only when the checkbox is checked. Unchecking it removes the remembered email. No password is placed in SharedPreferences. Logout leaves the remembered email setting unchanged.

## 7. Current session

`KEY_CURRENT_USER_EMAIL` is separate from Remember Me. It is set only after valid authentication and removed on logout. Remembering an email never authenticates a user.

## 8. Multi-user isolation

Every note has exactly one `owner_email`. All list, detail, edit, favorite, and delete database operations require the current email. Single-note updates use `id = ? AND owner_email = ?`, so a guessed note ID cannot access another user's data.

## 9. CRUD

The FAB launches `AddEditNoteActivity`. A new note receives the session email and current timestamp. Editing first loads the note using ID plus owner, preserves creation time and ownership, and changes the update time. Delete actions use a Material confirmation dialog and an owner-scoped delete query. List fragments reload from SQLite in `onResume()`.

## 10. Favorites

The heart calls `DatabaseHelper.setFavorite(noteId, ownerEmail, value)`. Favorites uses the same shared list code with an added `is_favorite = 1` clause. Lists reload from SQLite, keeping All, Favorites, Sorted, and Detail consistent.

## 11. Search

The toolbar `SearchView` sends text changes to the current `SearchableNotes` fragment. `DatabaseHelper.getNotes()` performs a case-insensitive `LIKE` search over both title and content while retaining the owner constraint. Clearing the query restores the full current list.

## 12. Sorting

The Sorted screen uses a `MaterialButtonToggleGroup`. `DATE` orders newest first; `ALPHABETICAL` orders title with case-insensitive collation. `KEY_SORT_TYPE` persists the choice.

## 13. SharedPreferences

All preferences are in `note_app_preferences`:

- `current_user_email`: authenticated owner for the active session.
- `remembered_email`: email to prefill on Sign In.
- `remember_me`: whether prefilling is enabled.
- `sort_type`: `DATE` or `ALPHABETICAL` for the Sorted screen.

## 14. Tags and filtering

The note editor accepts a short free-form tag, so users can reuse an existing label or create one by typing it. `DatabaseHelper.getTags()` returns the current user's distinct tags. All Notes rebuilds a single-selection horizontal ChipGroup containing All plus those tags.

## 15. Email sharing

`NoteShareUtils` creates an `ACTION_SENDTO` intent with a `mailto:` URI, subject `Note: <title>`, and body containing note content plus optional tag and date. It catches the no-compatible-app case and shows a Snackbar. No Internet permission or server is needed.

## 16. Favorite animation

`BaseNotesFragment.pulse()` uses `ViewPropertyAnimator`: the heart scales from 1.0 to 1.28 in 130 ms and returns to 1.0 in 150 ms. It runs after a successful database update.

## 17. Profile editing

`ProfileFragment` keeps email read-only and validates names using the registration rule. Password fields are optional; if used, the new password must satisfy the same rule and match confirmation. A successful update refreshes both the profile avatar and drawer header immediately.

## 18. Common UI modification points

- Palette and theme: `res/values/colors.xml` and `res/values/themes.xml`.
- Spacing and corner sizes: `res/values/dimens.xml` and `res/values/styles.xml`.
- User text: `res/values/strings.xml`.
- Note cards: `res/layout/item_note.xml`.
- Main lists/empty state: `res/layout/fragment_notes.xml`.
- Drawer destinations/header: `res/menu/drawer_menu.xml` and `res/layout/nav_header.xml`.

## 19. Most important files for discussion

1. `database/DatabaseHelper.java`
2. `utils/SessionManager.java`
3. `utils/PasswordUtils.java`
4. `utils/ValidationUtils.java`
5. `activities/SignInActivity.java`
6. `activities/SignUpActivity.java`
7. `MainActivity.java`
8. `activities/AddEditNoteActivity.java`
9. `fragments/BaseNotesFragment.java`
10. `fragments/NoteDetailFragment.java`

Also review `ProfileFragment`, `NoteAdapter`, and the `Note`/`User` model classes if time permits.

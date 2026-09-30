# NoteSpace

NoteSpace is a multi-user Android note-taking application built with Java, XML, AndroidX, Material Design, and SQLite. It provides secure local accounts, owner-isolated notes, favorites, full-text search, persistent sorting preferences, tags, profile management, and email sharing in a clean navigation-drawer interface.

> Academic project for ENCS5150 - Advanced Computer Systems Engineering Laboratory at Birzeit University.

## Highlights

- Local sign-up and sign-in with strict input validation
- Salted PBKDF2-HMAC-SHA256 password hashing
- Per-user note isolation enforced in every database operation
- Create, read, update, and delete note workflows
- Favorite notes with animated visual feedback
- Search across note titles and content
- Sort notes by creation date or title
- Persisted Remember Me and sorting preferences
- Tag-based note categorization and filtering
- Editable user profile and optional password update
- Email sharing through Android intents
- Responsive Material 3 interface using activities, fragments, and RecyclerView

## Technology

| Area | Implementation |
| --- | --- |
| Language | Java 11 |
| UI | XML, Material 3, AndroidX, RecyclerView |
| Storage | SQLite through `SQLiteOpenHelper` |
| Preferences | Android `SharedPreferences` |
| Architecture | Activities, fragments, adapter, models, database, and utility layers |
| Minimum SDK | Android 9 / API 28 |
| Target SDK | API 36 |
| Build | Gradle Wrapper 9.5.0 and Android Gradle Plugin 9.3.2 |

## Architecture

```text
app/src/main/
├── java/edu/birzeit/project/
│   ├── activities/     Authentication and note editor screens
│   ├── adapters/       RecyclerView note presentation
│   ├── database/       SQLite schema and owner-scoped operations
│   ├── fragments/      Notes, favorites, sorting, details, and profile
│   ├── models/         Note and user data models
│   └── utils/          Validation, sessions, password hashing, dates, and sharing
└── res/
    ├── layout/         Material interface layouts
    ├── menu/           Drawer and action menus
    ├── anim/           Fragment transitions
    └── values/         Theme, colors, dimensions, styles, and strings
```

The app stores users and notes in a private SQLite database. Every note query and mutation includes the authenticated owner's normalized email, preventing one account from accessing another account's notes. Passwords are never stored in plain text, and application data is excluded from Android backup and device transfer.

For a detailed code walkthrough, see [PROJECT_GUIDE.md](PROJECT_GUIDE.md).

## Getting Started

### Requirements

- Android Studio with Android SDK 36.1 installed
- JDK 21 (the Gradle daemon toolchain is declared by the project)
- An Android device or emulator running API 28 or newer

### Run in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Allow Gradle to synchronize the project dependencies.
4. Select an emulator or connected Android device.
5. Run the `app` configuration.

### Build from the command line

On macOS or Linux:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
gradlew.bat assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Quality Checks

Run the JVM unit tests and Android lint checks with:

```bash
./gradlew test lintDebug
```

Run the database instrumentation tests on a connected device or emulator with:

```bash
./gradlew connectedDebugAndroidTest
```

The instrumentation test verifies authentication and confirms that notes cannot be read, favorited, or deleted by another account.

## Main User Flow

1. Create an account with a valid email, name, and password.
2. Sign in and optionally remember the email address.
3. Add notes with a title, content, and optional tag.
4. Search, sort, filter, favorite, edit, share, or delete notes.
5. Update profile details or password from the navigation drawer.
6. Log out to clear the authenticated session.

## Privacy and Security

- Passwords use a unique random salt and PBKDF2-HMAC-SHA256 derivation.
- Remember Me stores only the email address, never the password.
- Database reads and writes are parameterized and scoped to the current user.
- Activities containing private application content are not exported.
- The database and session preferences are excluded from Android backup.

## Project Status

The project compiles successfully, passes its JVM unit tests, produces a valid debug APK, and reports no Android lint errors.

## Acknowledgments

Developed as part of the ENCS5150 Android project at Birzeit University. The implementation applies course topics including intents, SQLite, animations, SharedPreferences, singleton access, and fragments using current AndroidX and Material components.

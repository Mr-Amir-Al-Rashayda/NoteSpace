package edu.birzeit.project.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.material.snackbar.Snackbar;

import edu.birzeit.project.R;
import edu.birzeit.project.models.Note;

public final class NoteShareUtils {
    private NoteShareUtils() { }

    public static void sendByEmail(Context context, Note note, android.view.View feedbackView) {
        StringBuilder body = new StringBuilder(note.getContent());
        if (note.getTag() != null && !note.getTag().trim().isEmpty()) {
            body.append("\n\n").append(context.getString(R.string.shared_note_tag, note.getTag()));
        }
        body.append("\n").append(context.getString(R.string.shared_note_date,
                DateUtils.format(note.getCreatedAt())));
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject, note.getTitle()));
        intent.putExtra(Intent.EXTRA_TEXT, body.toString());
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_note)));
        } catch (ActivityNotFoundException exception) {
            Snackbar.make(feedbackView, R.string.no_email_app, Snackbar.LENGTH_LONG).show();
        }
    }
}

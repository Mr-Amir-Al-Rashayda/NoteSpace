package edu.birzeit.project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import edu.birzeit.project.MainActivity;
import edu.birzeit.project.R;
import edu.birzeit.project.activities.AddEditNoteActivity;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.models.Note;
import edu.birzeit.project.utils.DateUtils;
import edu.birzeit.project.utils.NoteShareUtils;
import edu.birzeit.project.utils.SessionManager;

public class NoteDetailFragment extends Fragment {
    private static final String ARG_NOTE_ID = "note_id";
    private long noteId;
    private String ownerEmail;
    private DatabaseHelper database;
    private Note note;
    private TextView title, content, date;
    private Chip tag;
    private ImageButton favoriteButton;

    public static NoteDetailFragment newInstance(long noteId) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_NOTE_ID, noteId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);
        noteId = getArguments() == null ? -1 : getArguments().getLong(ARG_NOTE_ID, -1);
        ownerEmail = new SessionManager(requireContext()).getCurrentUserEmail();
        database = DatabaseHelper.getInstance(requireContext());
        title = view.findViewById(R.id.detailTitle);
        content = view.findViewById(R.id.detailContent);
        date = view.findViewById(R.id.detailDate);
        tag = view.findViewById(R.id.detailTag);
        favoriteButton = view.findViewById(R.id.detailFavoriteButton);
        view.findViewById(R.id.detailEditButton).setOnClickListener(v -> editNote());
        view.findViewById(R.id.detailDeleteButton).setOnClickListener(v -> confirmDelete());
        view.findViewById(R.id.detailSendButton).setOnClickListener(v -> {
            if (note != null) NoteShareUtils.sendByEmail(requireContext(), note, v);
        });
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        refreshNote();
        return view;
    }

    @Override public void onResume() { super.onResume(); refreshNote(); }

    public void refreshNote() {
        if (database == null || ownerEmail == null || noteId < 0 || getView() == null) return;
        note = database.getNote(noteId, ownerEmail);
        if (note == null) {
            Snackbar.make(requireView(), R.string.note_not_found, Snackbar.LENGTH_LONG)
                    .addCallback(new Snackbar.Callback() {
                        @Override public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    }).show();
            return;
        }
        title.setText(note.getTitle());
        content.setText(note.getContent());
        date.setText(getString(R.string.created_on, DateUtils.format(note.getCreatedAt())));
        if (note.getUpdatedAt() > note.getCreatedAt()) {
            date.append("\n" + getString(R.string.updated_on, DateUtils.format(note.getUpdatedAt())));
        }
        if (note.getTag().trim().isEmpty()) tag.setVisibility(View.GONE);
        else { tag.setVisibility(View.VISIBLE); tag.setText(note.getTag()); }
        favoriteButton.setImageResource(note.isFavorite()
                ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        favoriteButton.setContentDescription(getString(note.isFavorite()
                ? R.string.remove_from_favorites : R.string.add_to_favorites));
    }

    private void toggleFavorite() {
        if (note == null) return;
        boolean favorite = !note.isFavorite();
        if (database.setFavorite(note.getId(), ownerEmail, favorite)) {
            note.setFavorite(favorite);
            favoriteButton.setImageResource(favorite
                    ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            BaseNotesFragment.pulse(favoriteButton);
        } else Snackbar.make(requireView(), R.string.error_favorite, Snackbar.LENGTH_LONG).show();
    }

    private void editNote() {
        if (note == null) return;
        Intent intent = new Intent(requireContext(), AddEditNoteActivity.class);
        intent.putExtra(AddEditNoteActivity.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    private void confirmDelete() {
        if (note == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_note_question)
                .setMessage(R.string.delete_note_warning)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (database.deleteNote(note.getId(), ownerEmail)) {
                        Snackbar.make(requireView(), R.string.note_deleted, Snackbar.LENGTH_SHORT)
                                .addCallback(new Snackbar.Callback() {
                                    @Override public void onDismissed(Snackbar transientBottomBar, int event) {
                                        if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
                                    }
                                }).show();
                    } else Snackbar.make(requireView(), R.string.error_delete_note, Snackbar.LENGTH_LONG).show();
                }).show();
    }
}

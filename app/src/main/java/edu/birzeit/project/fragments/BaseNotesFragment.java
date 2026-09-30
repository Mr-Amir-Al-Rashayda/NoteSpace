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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import edu.birzeit.project.MainActivity;
import edu.birzeit.project.R;
import edu.birzeit.project.activities.AddEditNoteActivity;
import edu.birzeit.project.adapters.NoteAdapter;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.models.Note;
import edu.birzeit.project.utils.NoteShareUtils;
import edu.birzeit.project.utils.SessionManager;

public abstract class BaseNotesFragment extends Fragment implements SearchableNotes, NoteAdapter.NoteActionListener {
    private static final String STATE_TAG = "selected_tag";

    protected DatabaseHelper database;
    protected String ownerEmail;
    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private ChipGroup tagChipGroup;
    private View tagFilterContainer;
    private MaterialButtonToggleGroup sortToggleGroup;
    private String query = "";
    private String selectedTag = "";

    protected abstract boolean favoritesOnly();
    protected abstract boolean showsTagFilters();
    protected abstract boolean showsSortControls();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        database = DatabaseHelper.getInstance(requireContext());
        ownerEmail = new SessionManager(requireContext()).getCurrentUserEmail();
        if (savedInstanceState != null) selectedTag = savedInstanceState.getString(STATE_TAG, "");
        recyclerView = view.findViewById(R.id.notesRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);
        emptyTitle = view.findViewById(R.id.emptyTitle);
        emptySubtitle = view.findViewById(R.id.emptySubtitle);
        tagChipGroup = view.findViewById(R.id.tagChipGroup);
        tagFilterContainer = view.findViewById(R.id.tagFilterScroll);
        sortToggleGroup = view.findViewById(R.id.sortToggleGroup);
        adapter = new NoteAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        tagFilterContainer.setVisibility(showsTagFilters() ? View.VISIBLE : View.GONE);
        sortToggleGroup.setVisibility(showsSortControls() ? View.VISIBLE : View.GONE);
        setupSortControls();
        refreshNotes();
        return view;
    }

    private void setupSortControls() {
        if (!showsSortControls()) return;
        SessionManager manager = new SessionManager(requireContext());
        int checkedId = SessionManager.SORT_ALPHABETICAL.equals(manager.getSortType())
                ? R.id.sortAlphabeticalButton : R.id.sortDateButton;
        sortToggleGroup.check(checkedId);
        sortToggleGroup.addOnButtonCheckedListener((group, checked, isChecked) -> {
            if (!isChecked) return;
            manager.setSortType(checked == R.id.sortAlphabeticalButton
                    ? SessionManager.SORT_ALPHABETICAL : SessionManager.SORT_DATE);
            refreshNotes();
        });
    }

    private void rebuildTagChips() {
        if (!showsTagFilters() || getContext() == null) return;
        List<String> tags = database.getTags(ownerEmail);
        tagChipGroup.removeAllViews();
        addTagChip(getString(R.string.all), "");
        for (String tag : tags) addTagChip(tag, tag);
        boolean selectionExists = selectedTag.isEmpty() || tags.contains(selectedTag);
        if (!selectionExists) selectedTag = "";
        for (int index = 0; index < tagChipGroup.getChildCount(); index++) {
            Chip chip = (Chip) tagChipGroup.getChildAt(index);
            if (selectedTag.equals(chip.getTag())) { chip.setChecked(true); break; }
        }
    }

    private void addTagChip(String label, String value) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, tagChipGroup, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(value);
        chip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) { selectedTag = String.valueOf(button.getTag()); loadNotes(); }
        });
        tagChipGroup.addView(chip);
    }

    @Override
    public void onResume() { super.onResume(); refreshNotes(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_TAG, selectedTag);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSearchQueryChanged(String newQuery) {
        query = newQuery == null ? "" : newQuery;
        loadNotes();
    }

    @Override
    public void refreshNotes() {
        if (getView() == null || ownerEmail == null) return;
        rebuildTagChips();
        loadNotes();
    }

    private void loadNotes() {
        if (adapter == null || ownerEmail == null) return;
        String sort = showsSortControls() ? new SessionManager(requireContext()).getSortType()
                : SessionManager.SORT_DATE;
        List<Note> notes = database.getNotes(ownerEmail, favoritesOnly(), query,
                showsTagFilters() ? selectedTag : "", sort);
        adapter.submitList(notes);
        boolean empty = notes.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        if (!query.trim().isEmpty()) {
            emptyTitle.setText(R.string.no_matching_notes);
            emptySubtitle.setText(R.string.try_another_search);
        } else if (!selectedTag.isEmpty() && showsTagFilters()) {
            emptyTitle.setText(R.string.no_notes_in_category);
            emptySubtitle.setText(R.string.choose_another_category);
        } else if (favoritesOnly()) {
            emptyTitle.setText(R.string.no_favorite_notes);
            emptySubtitle.setText(R.string.favorite_empty_support);
        } else {
            emptyTitle.setText(R.string.no_notes_yet);
            emptySubtitle.setText(R.string.notes_empty_support);
        }
    }

    @Override public void onOpen(Note note) { ((MainActivity) requireActivity()).openNoteDetail(note.getId()); }

    @Override
    public void onEdit(Note note) {
        Intent intent = new Intent(requireContext(), AddEditNoteActivity.class);
        intent.putExtra(AddEditNoteActivity.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Note note) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_note_question)
                .setMessage(R.string.delete_note_warning)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (database.deleteNote(note.getId(), ownerEmail)) {
                        refreshNotes();
                        Snackbar.make(requireView(), R.string.note_deleted, Snackbar.LENGTH_SHORT).show();
                    } else Snackbar.make(requireView(), R.string.error_delete_note, Snackbar.LENGTH_LONG).show();
                }).show();
    }

    @Override public void onShare(Note note, View anchor) { NoteShareUtils.sendByEmail(requireContext(), note, anchor); }

    @Override
    public void onFavoriteChanged(Note note, boolean favorite, ImageButton button) {
        if (database.setFavorite(note.getId(), ownerEmail, favorite)) {
            note.setFavorite(favorite);
            button.setImageResource(favorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            pulse(button);
            button.postDelayed(this::refreshNotes, 260);
        } else Snackbar.make(requireView(), R.string.error_favorite, Snackbar.LENGTH_LONG).show();
    }

    public static void pulse(View view) {
        view.animate().cancel();
        view.animate().scaleX(1.28f).scaleY(1.28f).setDuration(130)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
    }
}

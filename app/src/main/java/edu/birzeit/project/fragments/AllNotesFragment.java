package edu.birzeit.project.fragments;

public class AllNotesFragment extends BaseNotesFragment {
    @Override protected boolean favoritesOnly() { return false; }
    @Override protected boolean showsTagFilters() { return true; }
    @Override protected boolean showsSortControls() { return false; }
}

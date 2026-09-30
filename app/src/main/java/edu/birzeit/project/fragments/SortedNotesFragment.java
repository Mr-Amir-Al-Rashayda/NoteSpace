package edu.birzeit.project.fragments;

public class SortedNotesFragment extends BaseNotesFragment {
    @Override protected boolean favoritesOnly() { return false; }
    @Override protected boolean showsTagFilters() { return false; }
    @Override protected boolean showsSortControls() { return true; }
}

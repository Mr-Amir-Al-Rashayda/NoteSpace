package edu.birzeit.project.fragments;

public class FavoritesFragment extends BaseNotesFragment {
    @Override protected boolean favoritesOnly() { return true; }
    @Override protected boolean showsTagFilters() { return false; }
    @Override protected boolean showsSortControls() { return false; }
}

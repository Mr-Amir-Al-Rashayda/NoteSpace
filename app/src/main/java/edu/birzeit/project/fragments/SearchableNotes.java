package edu.birzeit.project.fragments;

public interface SearchableNotes {
    void onSearchQueryChanged(String query);
    void refreshNotes();
}

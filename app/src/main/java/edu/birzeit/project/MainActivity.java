package edu.birzeit.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import edu.birzeit.project.activities.AddEditNoteActivity;
import edu.birzeit.project.activities.SignInActivity;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.fragments.AllNotesFragment;
import edu.birzeit.project.fragments.FavoritesFragment;
import edu.birzeit.project.fragments.NoteDetailFragment;
import edu.birzeit.project.fragments.ProfileFragment;
import edu.birzeit.project.fragments.SearchableNotes;
import edu.birzeit.project.fragments.SortedNotesFragment;
import edu.birzeit.project.models.User;
import edu.birzeit.project.utils.SessionManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton addNoteFab;
    private SessionManager sessionManager;
    private SearchView searchView;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) { openSignIn(); return; }
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        addNoteFab = findViewById(R.id.addNoteFab);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_navigation_drawer, R.string.close_navigation_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        addNoteFab.setOnClickListener(view -> startActivity(new Intent(this, AddEditNoteActivity.class)));
        updateDrawerHeader();
        if (isFinishing()) return;
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_all_notes);
            showTopLevel(new AllNotesFragment(), R.string.all_notes);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateCurrentScreenChrome);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getSupportFragmentManager().popBackStack();
                else finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            updateDrawerHeader();
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof SearchableNotes) ((SearchableNotes) fragment).refreshNotes();
            if (fragment instanceof NoteDetailFragment) ((NoteDetailFragment) fragment).refreshNote();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_notes_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { dispatchSearch(query); return true; }
                @Override public boolean onQueryTextChange(String newText) { dispatchSearch(newText); return true; }
            });
        }
        updateCurrentScreenChrome();
        return true;
    }

    private void dispatchSearch(String query) {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof SearchableNotes) ((SearchableNotes) fragment).onSearchQueryChanged(query);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_all_notes) showTopLevel(new AllNotesFragment(), R.string.all_notes);
        else if (id == R.id.nav_favorites) showTopLevel(new FavoritesFragment(), R.string.favorites);
        else if (id == R.id.nav_sorted) showTopLevel(new SortedNotesFragment(), R.string.sorted_notes);
        else if (id == R.id.nav_profile) showTopLevel(new ProfileFragment(), R.string.profile);
        else if (id == R.id.nav_logout) logout();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showTopLevel(Fragment fragment, int titleRes) {
        getSupportFragmentManager().popBackStackImmediate(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                .replace(R.id.fragmentContainer, fragment).commitNow();
        setTitle(titleRes);
        if (searchView != null) { searchView.setQuery("", false); searchView.setIconified(true); }
        updateCurrentScreenChrome();
    }

    public void openNoteDetail(long noteId) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_fade_out,
                        R.anim.fragment_fade_in, R.anim.fragment_slide_out)
                .replace(R.id.fragmentContainer, NoteDetailFragment.newInstance(noteId))
                .addToBackStack("note_detail").commit();
    }

    public void updateDrawerHeader() {
        if (navigationView == null) return;
        User user = DatabaseHelper.getInstance(this).getUser(sessionManager.getCurrentUserEmail());
        if (user == null) { logout(); return; }
        View header = navigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.headerName)).setText(user.getFullName());
        ((TextView) header.findViewById(R.id.headerEmail)).setText(user.getEmail());
        ((TextView) header.findViewById(R.id.headerInitials)).setText(user.getInitials());
    }

    private void updateCurrentScreenChrome() {
        Fragment fragment = getCurrentFragment();
        boolean notesList = fragment instanceof SearchableNotes;
        if (addNoteFab != null) addNoteFab.setVisibility(notesList ? View.VISIBLE : View.GONE);
        if (searchMenuItem != null) searchMenuItem.setVisible(notesList);
        if (!notesList && searchView != null) { searchView.setQuery("", false); searchView.setIconified(true); }
        if (fragment instanceof NoteDetailFragment) setTitle(R.string.note_details);
        else if (fragment instanceof ProfileFragment) setTitle(R.string.profile);
        else if (fragment instanceof FavoritesFragment) setTitle(R.string.favorites);
        else if (fragment instanceof SortedNotesFragment) setTitle(R.string.sorted_notes);
        else if (fragment instanceof AllNotesFragment) setTitle(R.string.all_notes);
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    }

    public String getCurrentUserEmail() { return sessionManager.getCurrentUserEmail(); }

    private void logout() {
        sessionManager.clearSession();
        openSignIn();
    }

    private void openSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

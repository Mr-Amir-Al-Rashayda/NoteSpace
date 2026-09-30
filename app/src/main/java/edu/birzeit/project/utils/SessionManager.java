package edu.birzeit.project.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    public static final String SHARED_PREF_NAME = "note_app_preferences";
    public static final String KEY_CURRENT_USER_EMAIL = "current_user_email";
    public static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_SORT_TYPE = "sort_type";
    public static final String SORT_DATE = "DATE";
    public static final String SORT_ALPHABETICAL = "ALPHABETICAL";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void startSession(String email) {
        preferences.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply();
    }

    public void clearSession() {
        preferences.edit().remove(KEY_CURRENT_USER_EMAIL).apply();
    }

    public String getCurrentUserEmail() {
        return preferences.getString(KEY_CURRENT_USER_EMAIL, null);
    }

    public boolean isLoggedIn() { return getCurrentUserEmail() != null; }

    public void updateRememberedEmail(boolean remember, String email) {
        SharedPreferences.Editor editor = preferences.edit().putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) editor.putString(KEY_REMEMBERED_EMAIL, email);
        else editor.remove(KEY_REMEMBERED_EMAIL);
        editor.apply();
    }

    public boolean shouldRememberEmail() { return preferences.getBoolean(KEY_REMEMBER_ME, false); }
    public String getRememberedEmail() { return preferences.getString(KEY_REMEMBERED_EMAIL, ""); }

    public void setSortType(String sortType) {
        preferences.edit().putString(KEY_SORT_TYPE, sortType).apply();
    }

    public String getSortType() {
        return preferences.getString(KEY_SORT_TYPE, SORT_DATE);
    }
}

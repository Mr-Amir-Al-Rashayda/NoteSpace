package edu.birzeit.project.utils;

import android.util.Patterns;

public final class ValidationUtils {
    private ValidationUtils() { }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidName(String name) {
        int length = name == null ? 0 : name.trim().length();
        return length >= 3 && length <= 10;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6 && password.length() <= 12
                && password.matches(".*[0-9].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*");
    }
}

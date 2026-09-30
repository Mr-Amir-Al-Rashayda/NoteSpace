package edu.birzeit.project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.birzeit.project.MainActivity;
import edu.birzeit.project.R;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.models.User;
import edu.birzeit.project.utils.SessionManager;
import edu.birzeit.project.utils.ValidationUtils;

public class ProfileFragment extends Fragment {
    private TextInputLayout firstLayout, lastLayout, passwordLayout, confirmLayout;
    private TextInputEditText firstInput, lastInput, passwordInput, confirmInput;
    private TextView emailText, initialsText;
    private DatabaseHelper database;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        database = DatabaseHelper.getInstance(requireContext());
        email = new SessionManager(requireContext()).getCurrentUserEmail();
        firstLayout = view.findViewById(R.id.firstNameLayout);
        lastLayout = view.findViewById(R.id.lastNameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        confirmLayout = view.findViewById(R.id.confirmPasswordLayout);
        firstInput = view.findViewById(R.id.firstNameInput);
        lastInput = view.findViewById(R.id.lastNameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmInput = view.findViewById(R.id.confirmPasswordInput);
        emailText = view.findViewById(R.id.profileEmail);
        initialsText = view.findViewById(R.id.profileInitials);
        view.findViewById(R.id.saveProfileButton).setOnClickListener(v -> saveProfile());
        loadProfile();
        return view;
    }

    private void loadProfile() {
        User user = database.getUser(email);
        if (user == null || getView() == null) {
            if (getView() != null) Snackbar.make(requireView(), R.string.profile_not_found, Snackbar.LENGTH_LONG).show();
            return;
        }
        emailText.setText(user.getEmail());
        initialsText.setText(user.getInitials());
        firstInput.setText(user.getFirstName());
        lastInput.setText(user.getLastName());
    }

    private void saveProfile() {
        clearErrors();
        String first = textOf(firstInput).trim();
        String last = textOf(lastInput).trim();
        String password = textOf(passwordInput);
        String confirm = textOf(confirmInput);
        View firstInvalid = null;
        if (first.isEmpty()) firstInvalid = setError(firstLayout, firstInput, R.string.error_first_name_required, firstInvalid);
        else if (!ValidationUtils.isValidName(first)) firstInvalid = setError(firstLayout, firstInput, R.string.error_name_length, firstInvalid);
        if (last.isEmpty()) firstInvalid = setError(lastLayout, lastInput, R.string.error_last_name_required, firstInvalid);
        else if (!ValidationUtils.isValidName(last)) firstInvalid = setError(lastLayout, lastInput, R.string.error_name_length, firstInvalid);
        if (!password.isEmpty() && !ValidationUtils.isValidPassword(password))
            firstInvalid = setError(passwordLayout, passwordInput, R.string.error_password_rules, firstInvalid);
        if (!password.isEmpty() && confirm.isEmpty())
            firstInvalid = setError(confirmLayout, confirmInput, R.string.error_confirm_required, firstInvalid);
        else if (!password.equals(confirm))
            firstInvalid = setError(confirmLayout, confirmInput, R.string.error_password_mismatch, firstInvalid);
        if (firstInvalid != null) { firstInvalid.requestFocus(); return; }
        if (database.updateUser(email, first, last, password)) {
            passwordInput.setText("");
            confirmInput.setText("");
            loadProfile();
            ((MainActivity) requireActivity()).updateDrawerHeader();
            Snackbar.make(requireView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
        } else Snackbar.make(requireView(), R.string.error_profile_update, Snackbar.LENGTH_LONG).show();
    }

    private View setError(TextInputLayout layout, View input, int message, View firstInvalid) {
        layout.setError(getString(message));
        return firstInvalid == null ? input : firstInvalid;
    }

    private void clearErrors() {
        firstLayout.setError(null); lastLayout.setError(null); passwordLayout.setError(null); confirmLayout.setError(null);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}

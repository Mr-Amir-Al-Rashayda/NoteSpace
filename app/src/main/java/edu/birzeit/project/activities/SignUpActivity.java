package edu.birzeit.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import edu.birzeit.project.R;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.utils.ValidationUtils;

public class SignUpActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, firstNameLayout, lastNameLayout, passwordLayout, confirmLayout;
    private TextInputEditText emailInput, firstNameInput, lastNameInput, passwordInput, confirmInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailLayout = findViewById(R.id.emailLayout);
        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmLayout = findViewById(R.id.confirmPasswordLayout);
        emailInput = findViewById(R.id.emailInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmPasswordInput);
        findViewById(R.id.signUpButton).setOnClickListener(this::register);
        findViewById(R.id.signInLink).setOnClickListener(view -> finish());
    }

    private void register(View feedbackView) {
        clearErrors();
        String email = textOf(emailInput).trim().toLowerCase(Locale.ROOT);
        String firstName = textOf(firstNameInput).trim();
        String lastName = textOf(lastNameInput).trim();
        String password = textOf(passwordInput);
        String confirm = textOf(confirmInput);
        View firstInvalid = null;

        if (email.isEmpty()) firstInvalid = setError(emailLayout, emailInput, R.string.error_email_required, firstInvalid);
        else if (!ValidationUtils.isValidEmail(email)) firstInvalid = setError(emailLayout, emailInput, R.string.error_email_invalid, firstInvalid);

        if (firstName.isEmpty()) firstInvalid = setError(firstNameLayout, firstNameInput, R.string.error_first_name_required, firstInvalid);
        else if (!ValidationUtils.isValidName(firstName)) firstInvalid = setError(firstNameLayout, firstNameInput, R.string.error_name_length, firstInvalid);

        if (lastName.isEmpty()) firstInvalid = setError(lastNameLayout, lastNameInput, R.string.error_last_name_required, firstInvalid);
        else if (!ValidationUtils.isValidName(lastName)) firstInvalid = setError(lastNameLayout, lastNameInput, R.string.error_name_length, firstInvalid);

        if (password.isEmpty()) firstInvalid = setError(passwordLayout, passwordInput, R.string.error_password_required, firstInvalid);
        else if (!ValidationUtils.isValidPassword(password)) firstInvalid = setError(passwordLayout, passwordInput, R.string.error_password_rules, firstInvalid);

        if (confirm.isEmpty()) firstInvalid = setError(confirmLayout, confirmInput, R.string.error_confirm_required, firstInvalid);
        else if (!password.equals(confirm)) firstInvalid = setError(confirmLayout, confirmInput, R.string.error_password_mismatch, firstInvalid);

        if (firstInvalid != null) {
            firstInvalid.requestFocus();
            return;
        }
        DatabaseHelper database = DatabaseHelper.getInstance(this);
        if (database.userExists(email)) {
            emailLayout.setError(getString(R.string.error_duplicate_email));
            emailInput.requestFocus();
            return;
        }
        if (!database.createUser(email, firstName, lastName, password)) {
            Snackbar.make(feedbackView, R.string.error_registration_failed, Snackbar.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(feedbackView, R.string.registration_success, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override public void onDismissed(Snackbar transientBottomBar, int event) {
                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                        intent.putExtra(SignInActivity.EXTRA_REGISTERED_EMAIL, email);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }).show();
    }

    private View setError(TextInputLayout layout, View input, int message, View firstInvalid) {
        layout.setError(getString(message));
        return firstInvalid == null ? input : firstInvalid;
    }

    private void clearErrors() {
        emailLayout.setError(null); firstNameLayout.setError(null); lastNameLayout.setError(null);
        passwordLayout.setError(null); confirmLayout.setError(null);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}

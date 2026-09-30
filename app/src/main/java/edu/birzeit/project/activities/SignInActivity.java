package edu.birzeit.project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import edu.birzeit.project.MainActivity;
import edu.birzeit.project.R;
import edu.birzeit.project.database.DatabaseHelper;
import edu.birzeit.project.utils.SessionManager;

public class SignInActivity extends AppCompatActivity {
    public static final String EXTRA_REGISTERED_EMAIL = "registered_email";

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberCheckBox;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_sign_in);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberCheckBox = findViewById(R.id.rememberCheckBox);

        if (sessionManager.shouldRememberEmail()) {
            emailInput.setText(sessionManager.getRememberedEmail());
            rememberCheckBox.setChecked(true);
        }
        String registeredEmail = getIntent().getStringExtra(EXTRA_REGISTERED_EMAIL);
        if (registeredEmail != null) emailInput.setText(registeredEmail);

        findViewById(R.id.signInButton).setOnClickListener(this::signIn);
        findViewById(R.id.signUpLink).setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void signIn(View view) {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        String email = textOf(emailInput).trim().toLowerCase(Locale.ROOT);
        String password = textOf(passwordInput);
        if (email.isEmpty()) {
            emailLayout.setError(getString(R.string.error_email_required));
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_required));
            passwordInput.requestFocus();
            return;
        }
        if (!DatabaseHelper.getInstance(this).authenticate(email, password)) {
            passwordLayout.setError(getString(R.string.error_credentials));
            passwordInput.requestFocus();
            Snackbar.make(view, R.string.error_credentials, Snackbar.LENGTH_LONG).show();
            return;
        }
        sessionManager.updateRememberedEmail(rememberCheckBox.isChecked(), email);
        sessionManager.startSession(email);
        openMain();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}

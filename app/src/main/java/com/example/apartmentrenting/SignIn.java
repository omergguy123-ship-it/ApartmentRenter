package com.example.apartmentrenting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private MaterialButton loginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Connect variables to your XML IDs
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);

        // Set click listener on the login button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation so the app doesn't crash if fields are empty
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required.");
            return;
        }

        // Authenticate the user with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login was successful
                        Toast.makeText(SignIn.this, "Welcome back!", Toast.LENGTH_SHORT).show();

                        // Send the user to the main ApartmentRenter dashboard
                        // Uncomment and update the class name below to match your home screen
                        // startActivity(new Intent(SignIn.this, MainActivity.class));
                        // finish();
                    } else {
                        // Login failed (wrong password, no account, etc.)
                        Toast.makeText(SignIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
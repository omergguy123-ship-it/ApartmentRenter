package com.example.apartmentrenting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {
    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Button signUpBtn;
    private TextView tvAlreadyHaveAccount;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signUpBtn = findViewById(R.id.signUpBtn);
        tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp(v);
            }
        });

        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(SignUp.this, SignIn.class);
                startActivity(signInIntent);
                finish();
            }
        });
    }

    private void performSignUp(View view) {
        String name = firstNameInput.getText().toString().trim();
        String lastname = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(view, "All fields are required!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show();
            return;
        }

        if (password.length() < 6) {
            Snackbar.make(view, "Password must be at least 6 characters long!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show();
            return;
        }

        signUpBtn.setEnabled(false);
        Toast.makeText(SignUp.this, "Creating account...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                UserInfo newUser = new UserInfo(name, lastname, uid);
                                uploadNewUserToDB(uid, newUser);
                            } else {
                                signUpBtn.setEnabled(true);
                                Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            signUpBtn.setEnabled(true);
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Sign up failed";
                            Toast.makeText(SignUp.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void uploadNewUserToDB(String userId, UserInfo newUser) {
        db.collection("users").document(userId).set(newUser)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signUpBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Account Created Successfully!", Toast.LENGTH_LONG).show();
                            // Redirect to Dashboard (BrouseHosesOrUploadListing)
                            Intent dashboardIntent = new Intent(SignUp.this, BrouseHosesOrUploadListing.class);
                            dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(dashboardIntent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Database save failed";
                            Toast.makeText(SignUp.this, "Profile saving failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
package com.example.apartmentrenting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login check
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent dashboardIntent = new Intent(MainActivity.this, BrouseHosesOrUploadListing.class);
            startActivity(dashboardIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Intent signUpIntent = new Intent(MainActivity.this, SignUp.class);
        Intent signInIntent = new Intent(MainActivity.this, SignIn.class);
        
        Button signUpButton = findViewById(R.id.SignUpButton);
        Button signInButton = findViewById(R.id.SignInButton);
        
        signUpButton.setOnClickListener(v -> startActivity(signUpIntent));
        signInButton.setOnClickListener(v -> startActivity(signInIntent));
    }
}
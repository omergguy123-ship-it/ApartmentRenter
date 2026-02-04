package com.example.apartmentrenting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent SignUp = new Intent(MainActivity.this , SignUp.class);
        Intent SignIn = new Intent(MainActivity.this , SignIn.class);
        Button SignUpButton = findViewById(R.id.SignUpButton);
        Button SignInButton = findViewById(R.id.SignInButton);
        SignUpButton.setOnClickListener(v -> startActivity(SignUp));
        SignInButton.setOnClickListener(v -> startActivity(SignIn));

    }
}
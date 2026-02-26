package com.example.apartmentrenting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.StartupTime;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    EditText FirstNameInput,LastNameInput,EmailInput,PasswordInput;
    Button SignUpBtn;
    UserInfo NewUser;
    String name, lastname,email,password,UID;
    FirebaseFirestore db;
    Map<String, Object> user;
    private FirebaseAuth mAuth;
    Intent finishsignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        FirstNameInput = findViewById(R.id.firstNameInput);
        LastNameInput = findViewById(R.id.lastNameInput);
        EmailInput = findViewById(R.id.emailInput);
        PasswordInput = findViewById(R.id.passwordInput);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //write UpdateUI
        SignUpBtn = findViewById(R.id.signUpBtn);
        db = FirebaseFirestore.getInstance();
        finishsignup = new Intent(SignUp.this,SignIn.class);


        SignUpBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                name = FirstNameInput.getText().toString();
                lastname = LastNameInput.getText().toString();
                email = EmailInput.getText().toString();
                password = PasswordInput.getText().toString();
                if ((!Objects.equals(name, ""))&&(!Objects.equals(lastname, ""))&&(!Objects.equals(email, "")&&(!Objects.equals(password, ""))))
                    {
                        mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign up success, get the UID
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        UID = firebaseUser.getUid();

                                    }
                                    NewUser = new UserInfo(name, lastname, UID);
                                    UploadNewUserToDB(UID, NewUser);


                                } else {
                                    // If sign up fails, display a message to the user.
                                }

                            }
                        });

                    }
                else{
                    Snackbar.make(v, "Fields cannot remain blank!", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).show();
                }

            }


        });


    }
    public void UploadNewUserToDB (String UserID,UserInfo NewUser){
        user = new HashMap<>();
        user.put(UserID,NewUser);

        db.collection("users").add(user)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUp.this,"User Created", Toast.LENGTH_LONG).show();
                    startActivity(finishsignup);
                }
                else{
                    Toast.makeText(SignUp.this,"Sign Up Failed", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
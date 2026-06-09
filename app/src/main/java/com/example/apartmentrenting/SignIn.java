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

/**
 * מסך כניסה למערכת (SignIn).
 * מאפשר למשתמש רשום להתחבר לאפליקציה באמצעות כתובת מייל וסיסמה.
 * עם חיבור מוצלח, מועבר המשתמש למסך הבית הראשי (BrouseHosesOrUploadListing).
 */
public class SignIn extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView tvSignUpLink;
    // FirebaseAuth - מחלקת Firebase המנהלת את כל פעולות האימות (כניסה, יציאה, הרשמה)
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // 1. קישור רכיבי ממשק המשתמש מהקובץ activity_sign_in.xml
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // 2. אתחול FirebaseAuth לשימוש בשירותי האימות של Firebase
        mAuth = FirebaseAuth.getInstance();

        // 3. כפתור כניסה - בלחיצה מפעיל את פונקציית ההתחברות
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin(v);
            }
        });

        // 4. קישור לדף ההרשמה - מוביל למסך SignUp ומסיים את המסך הנוכחי
        tvSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignIn.this, SignUp.class);
                startActivity(signUpIntent);
                finish();
            }
        });
    }

    /**
     * פונקציה המבצעת את תהליך הכניסה למערכת.
     * בודקת שדות קלט, ומשתמשת ב-FirebaseAuth.signInWithEmailAndPassword לאמת את המשתמש.
     * במקרה של הצלחה, מנווטת למסך הבית. במקרה של כישלון, מציגה הודעת שגיאה.
     *
     * @param view ה-View של הכפתור שנלחץ (משמש להצגת Snackbar)
     */
    private void performLogin(View view) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // בדיקה שהשדות אינם ריקים לפני ניסיון ההתחברות
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(view, "Email and Password cannot be empty!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show();
            return;
        }

        // ניטרול כפתור הכניסה למניעת לחיצות כפולות בזמן הטעינה
        loginBtn.setEnabled(false);
        Toast.makeText(SignIn.this, "Logging in...", Toast.LENGTH_SHORT).show();

        // קריאה ל-Firebase Authentication לאמת את המשתמש לפי מייל וסיסמה
        mAuth.signInWithEmailAndPassword(email, password)
               .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            // כניסה הצליחה - ניווט למסך הבית הראשי
                            Toast.makeText(SignIn.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent dashboardIntent = new Intent(SignIn.this, BrouseHosesOrUploadListing.class);
                            // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK - מנקה את ערמת הפעילויות (Back Stack)
                            // כך שהמשתמש לא יוכל לחזור למסך הכניסה לאחר התחברות
                            dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(dashboardIntent);
                            finish();
                        } else {
                            // כניסה נכשלה - הצגת הודעת שגיאה
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(SignIn.this, "Login Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
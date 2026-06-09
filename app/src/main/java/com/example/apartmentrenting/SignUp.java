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

/**
 * מסך הרשמה (SignUp) לאפליקציה.
 * מאפשר למשתמש חדש ליצור חשבון עם שם פרטי, שם משפחה, מייל וסיסמה.
 * לאחר ההרשמה, פרטי המשתמש נשמרים ב-Firestore תחת אוסף "users" ומזהה ה-UID שלו.
 * לאחר ההצלחה, המשתמש מועבר אוטומטית למסך הבית הראשי.
 */
public class SignUp extends AppCompatActivity {
    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Button signUpBtn;
    private TextView tvAlreadyHaveAccount;
    // db - חיבור ל-Cloud Firestore לשמירת פרטי המשתמש
    private FirebaseFirestore db;
    // mAuth - אובייקט Firebase Authentication לפעולות הרשמה
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 1. קישור רכיבי ממשק המשתמש מהקובץ activity_sign_up.xml
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signUpBtn = findViewById(R.id.signUpBtn);
        tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

        // 2. אתחול Firebase Auth ו-Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. כפתור הרשמה - בלחיצה מפעיל את תהליך הרשמת המשתמש
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp(v);
            }
        });

        // 4. קישור "כבר יש לי חשבון" - מנווט למסך הכניסה
        tvAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(SignUp.this, SignIn.class);
                startActivity(signInIntent);
                finish();
            }
        });
    }

    /**
     * פונקציה שמבצעת אימות קלט ומפעילה את תהליך ההרשמה ב-Firebase Authentication.
     * קודם יוצרת את חשבון המשתמש ב-Firebase Auth, ולאחר מכן קוראת ל-uploadNewUserToDB
     * כדי לשמור את הפרטים הנוספים (שם, שם משפחה) גם ב-Firestore.
     *
     * @param view ה-View של הכפתור שנלחץ (משמש להצגת Snackbar)
     */
    private void performSignUp(View view) {
        String name = firstNameInput.getText().toString().trim();
        String lastname = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // בדיקה שכל השדות מלאים
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Snackbar.make(view, "All fields are required!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show();
            return;
        }

        // בדיקת אורך סיסמה מינימלי (6 תווים - דרישת Firebase Auth)
        if (password.length() < 6) {
            Snackbar.make(view, "Password must be at least 6 characters long!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .show();
            return;
        }

        // ניטרול הכפתור למניעת לחיצות כפולות
        signUpBtn.setEnabled(false);
        Toast.makeText(SignUp.this, "Creating account...", Toast.LENGTH_SHORT).show();

        // יצירת חשבון חדש ב-Firebase Authentication באמצעות מייל וסיסמה
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // ההרשמה ב-Auth הצליחה - שולפים את האובייקט FirebaseUser שנוצר
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // שמירת הנתונים הנוספים (שם, שם משפחה, UID) ב-Firestore
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

    /**
     * פונקציה שמעלה את פרטי המשתמש החדש לאוסף "users" ב-Firestore.
     * מזהה המסמך הוא ה-UID הייחודי של המשתמש ב-Firebase Authentication.
     * בהצלחה - המשתמש מועבר למסך הבית. בכישלון - מוצגת הודעת שגיאה.
     *
     * @param userId  ה-UID של המשתמש (מגיע מ-Firebase Auth)
     * @param newUser אובייקט UserInfo המכיל את פרטי המשתמש לשמירה
     */
    private void uploadNewUserToDB(String userId, UserInfo newUser) {
        // שמירת אובייקט UserInfo תחת מסמך עם מזהה ה-UID של המשתמש
        db.collection("users").document(userId).set(newUser)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signUpBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Account Created Successfully!", Toast.LENGTH_LONG).show();
                            // ניווט למסך הבית ומחיקת Back Stack כך שהמשתמש לא יוכל לחזור למסך ההרשמה
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
package com.example.apartmentrenting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity - מסך הפתיחה (Welcome Screen) של האפליקציה.
 * הוא ה-Activity הראשון שמופעל כשמפעילים את האפליקציה (מוגדר ב-AndroidManifest.xml).
 *
 * תפקידים עיקריים:
 * 1. בדיקת כניסה אוטומטית (Auto-Login Check):
 *    אם המשתמש כבר מחובר (FirebaseAuth מחזיר FirebaseUser תקף),
 *    מדלג על מסך הפתיחה ומנווט ישירות למסך הבית (BrouseHosesOrUploadListing).
 *
 * 2. הצגת כפתורי הרשמה וכניסה למשתמשים חדשים שטרם נרשמו/התחברו.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // בדיקת כניסה אוטומטית - FirebaseAuth.getInstance().getCurrentUser() מחזיר null
        // אם אין משתמש מחובר, ואובייקט FirebaseUser תקף אם המשתמש כבר התחבר בעבר.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // המשתמש כבר מחובר - ניווט ישיר למסך הבית הראשי
            Intent dashboardIntent = new Intent(MainActivity.this, BrouseHosesOrUploadListing.class);
            startActivity(dashboardIntent);
            // סיום ה-Activity כך שלא ניתן לחזור אליו בלחיצת "Back"
            finish();
            return;
        }

        // אין משתמש מחובר - הצגת מסך הפתיחה עם כפתורי הרשמה וכניסה
        setContentView(R.layout.activity_main);

        // הגדרת Intent לניווט למסכי הרשמה וכניסה
        Intent signUpIntent = new Intent(MainActivity.this, SignUp.class);
        Intent signInIntent = new Intent(MainActivity.this, SignIn.class);

        // כפתור הרשמה - פותח את SignUp Activity
        Button signUpButton = findViewById(R.id.SignUpButton);
        // כפתור כניסה - פותח את SignIn Activity
        Button signInButton = findViewById(R.id.SignInButton);

        signUpButton.setOnClickListener(v -> startActivity(signUpIntent));
        signInButton.setOnClickListener(v -> startActivity(signInIntent));
    }
}
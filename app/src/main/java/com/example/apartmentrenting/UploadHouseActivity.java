package com.example.apartmentrenting;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * UploadHouseActivity - מסך פרסום נכס חדש להשכרה.
 * נפתח ממסך ה-Hosting בלחיצה על כפתור ה-"+" (btnHostAddNew).
 *
 * תכונות עיקריות:
 * - מאפשר למארח להזין פרטי נכס: כותרת, תיאור, מיקום, מחיר, מספר חדרים ושירותים.
 * - בחירת תמונה מגלריית המכשיר דרך ActivityResult API (registerForActivityResult).
 * - העלאת התמונה ל-Firebase Storage ושמירת ה-URL ב-Firestore.
 * - שמירת הנכס לאוסף "listings" ב-Firestore עם כל הפרטים.
 * - במקרה של כשל ב-Storage - שמירה מקומית של התמונה ב-Cache.
 */
public class UploadHouseActivity extends AppCompatActivity {

    // שדות קלט לפרטי הנכס
    private EditText etUploadTitle, etUploadDescription, etUploadLocation, etUploadPrice, etUploadBeds, etUploadBaths;
    // רשימת קטגוריות נפתחת (Dropdown) לבחירת סוג הנכס
    private AutoCompleteTextView spinnerUploadCategory;
    // Chip (תגית לחיצה) לכל שירות (Amenity) - ניתן לסמן/לבטל
    private Chip chipUploadWifi, chipUploadAc, chipUploadKitchen, chipUploadParking;
    // כרטיס לחיצה לבחירת תמונה
    private MaterialCardView cardUploadImage;
    // placeholder - מוצג לפני בחירת תמונה
    private LinearLayout uploadImagePlaceholder;
    // תצוגה מקדימה של התמונה שנבחרה
    private ImageView ivUploadPreview;
    // כפתור פרסום הנכס
    private Button btnSubmitListing;
    // כפתור חזרה למסך הקודם
    private ImageButton btnUploadBack;

    // שירותי Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // URI של התמונה שנבחרה מהגלריה (null אם לא נבחרה תמונה)
    private Uri selectedImageUri = null;
    // שם המארח - נטען מ-Firestore ומוצג בנכס
    private String currentHostName = "Professional Host";

    /**
     * pickPhotoLauncher - משגר (Launcher) לפתיחת גלריית המכשיר בפורמט ActivityResult API החדש.
     * registerForActivityResult עם ActivityResultContracts.GetContent() מאפשר בחירת קובץ מסוג image/*.
     * ה-callback (uri -> {...}) מופעל עם ה-URI של התמונה שנבחרה.
     * זהו דרישת בגרות (Item 6) - שימוש ב-ActivityResult API החדש.
     */
    private final ActivityResultLauncher<String> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // הצגת תצוגה מקדימה של התמונה שנבחרה
                    ivUploadPreview.setImageURI(uri);
                    ivUploadPreview.setVisibility(View.VISIBLE);
                    uploadImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_house);

        // 1. אתחול שירותי Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 2. קישור רכיבי ממשק המשתמש מהקובץ activity_upload_house.xml
        etUploadTitle = findViewById(R.id.etUploadTitle);
        etUploadDescription = findViewById(R.id.etUploadDescription);
        etUploadLocation = findViewById(R.id.etUploadLocation);
        etUploadPrice = findViewById(R.id.etUploadPrice);
        etUploadBeds = findViewById(R.id.etUploadBeds);
        etUploadBaths = findViewById(R.id.etUploadBaths);

        spinnerUploadCategory = findViewById(R.id.spinnerUploadCategory);

        chipUploadWifi = findViewById(R.id.chipUploadWifi);
        chipUploadAc = findViewById(R.id.chipUploadAc);
        chipUploadKitchen = findViewById(R.id.chipUploadKitchen);
        chipUploadParking = findViewById(R.id.chipUploadParking);

        cardUploadImage = findViewById(R.id.cardUploadImage);
        uploadImagePlaceholder = findViewById(R.id.uploadImagePlaceholder);
        ivUploadPreview = findViewById(R.id.ivUploadPreview);

        btnSubmitListing = findViewById(R.id.btnSubmitListing);
        btnUploadBack = findViewById(R.id.btnUploadBack);

        // 3. כפתור חזרה - סוגר את ה-Activity
        btnUploadBack.setOnClickListener(v -> finish());

        // 4. הגדרת Dropdown לבחירת קטגוריה
        // ArrayAdapter מחבר את מערך הקטגוריות לרשימה הנפתחת של AutoCompleteTextView
        String[] categories = new String[]{"Apartment", "Villa", "Cabin", "Studio"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerUploadCategory.setAdapter(categoryAdapter);
        spinnerUploadCategory.setText(categories[0], false); // ברירת מחדל: "Apartment"

        // 5. טעינת פרטי המשתמש המחובר מ-Firestore לקבלת שם המארח
        fetchHostProfile();

        // 6. לחיצה על כרטיס התמונה - פתיחת גלריית המכשיר לבחירת תמונה
        cardUploadImage.setOnClickListener(v -> pickPhotoLauncher.launch("image/*"));

        // 7. לחיצה על כפתור הפרסום - אימות קלט ושליחה ל-Firestore
        btnSubmitListing.setOnClickListener(v -> validateAndSubmitListing());
    }

    /**
     * fetchHostProfile - טוענת את שם המשתמש המחובר מ-Firestore.
     * שם המארח (firstName + lastName) נשמר בנכס ומוצג לאורחים ב-HouseDetailActivity.
     * השאילתה אסינכרונית - currentHostName מתעדכן בברקע.
     */
    private void fetchHostProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // שאילתה ל-Firestore לטעינת מסמך המשתמש לפי ה-UID שלו
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document != null && document.exists()) {
                            String first = document.getString("firstName");
                            String last = document.getString("lastName");
                            if (first != null) {
                                currentHostName = first + " " + (last != null ? last : "");
                            }
                        }
                    });
        }
    }

    /**
     * validateAndSubmitListing - מאמת את שדות הקלט ומשגר את תהליך פרסום הנכס.
     * בודקת שכל השדות הכרחיים מלאים (כותרת, תיאור, מיקום, מחיר, חדרים).
     * אם נבחרה תמונה - מעלה אותה ל-Firebase Storage ולאחר מכן שומרת את הנכס.
     * אם לא נבחרה תמונה - שומרת את הנכס ללא תמונה.
     * UUID.randomUUID() מייצר מזהה ייחודי אוניברסלי (UUID) לנכס החדש.
     */
    private void validateAndSubmitListing() {
        String title = etUploadTitle.getText().toString().trim();
        String description = etUploadDescription.getText().toString().trim();
        String location = etUploadLocation.getText().toString().trim();
        String priceStr = etUploadPrice.getText().toString().trim();
        String bedsStr = etUploadBeds.getText().toString().trim();
        String bathsStr = etUploadBaths.getText().toString().trim();
        String category = spinnerUploadCategory.getText().toString().trim();

        // בדיקה שכל השדות הכרחיים מלאים
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location) ||
            TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(bedsStr) || TextUtils.isEmpty(bathsStr)) {
            Toast.makeText(this, "Please fill in all listing details!", Toast.LENGTH_LONG).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int beds = Integer.parseInt(bedsStr);
        int baths = Integer.parseInt(bathsStr);

        // בדיקת סימון Chip לכל שירות (Amenity)
        boolean wifi = chipUploadWifi.isChecked();
        boolean ac = chipUploadAc.isChecked();
        boolean kitchen = chipUploadKitchen.isChecked();
        boolean parking = chipUploadParking.isChecked();

        // ניטרול כפתור הפרסום למניעת כפולים
        btnSubmitListing.setEnabled(false);
        Toast.makeText(this, "Publishing listing...", Toast.LENGTH_SHORT).show();

        // UUID.randomUUID() - ספריית Java ליצירת מזהה ייחודי אוניברסלי (128-bit ID)
        String listingId = UUID.randomUUID().toString();
        String hostUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";

        if (selectedImageUri != null) {
            // שמירת עותק מקומי של התמונה ב-Cache כגיבוי למקרה כשל ב-Storage
            String localCachePath = copyImageToLocalCache(selectedImageUri, listingId);

            // העלאת התמונה ל-Firebase Storage תחת "listings/<listingId>.jpg"
            StorageReference ref = storage.getReference().child("listings/" + listingId + ".jpg");
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                // ה-URL הציבורי של התמונה ב-Storage - נשמר בנכס ב-Firestore
                                String imageUrl = downloadUri.toString();
                                saveListingToDatabase(listingId, title, description, location, price, imageUrl, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                            })
                            .addOnFailureListener(e -> {
                                // כשלון בקבלת URL - שימוש בנתיב הקובץ המקומי כגיבוי
                                saveListingToDatabase(listingId, title, description, location, price, localCachePath, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                            }))
                    .addOnFailureListener(e -> {
                        // כשלון בהעלאה ל-Storage - שימוש בנתיב הקובץ המקומי
                        Toast.makeText(UploadHouseActivity.this, "Storage offline. Saving photo to local device cache.", Toast.LENGTH_SHORT).show();
                        saveListingToDatabase(listingId, title, description, location, price, localCachePath, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                    });
        } else {
            // לא נבחרה תמונה - פרסום ללא תמונה (imageUrl ריק)
            saveListingToDatabase(listingId, title, description, location, price, "", hostUid, category, beds, baths, wifi, ac, kitchen, parking);
        }
    }

    /**
     * copyImageToLocalCache - מעתיקה תמונה מה-URI שנבחר לתיקיית Cache של האפליקציה.
     * משמשת כגיבוי מקומי כאשר Firebase Storage אינו זמין (אין אינטרנט / אין הרשאות).
     * משתמשת ב-InputStream ו-FileOutputStream לקריאה וכתיבה של קובץ בינארי.
     *
     * @param uri       ה-URI של התמונה שנבחרה מהגלריה
     * @param listingId מזהה הנכס (לשם קובץ הגיבוי)
     * @return נתיב מוחלט של קובץ הגיבוי המקומי, או מחרוזת ריקה בכשלון
     */
    private String copyImageToLocalCache(Uri uri, String listingId) {
        try {
            // פתיחת זרם קלט (InputStream) לקריאת התמונה מה-URI
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";

            // יצירת תיקיית "listings" ב-Cache של האפליקציה
            java.io.File cacheDir = getCacheDir();
            java.io.File listingsDir = new java.io.File(cacheDir, "listings");
            if (!listingsDir.exists()) {
                listingsDir.mkdirs();
            }

            // כתיבת נתוני התמונה לקובץ מקומי בבלוקים של 4KB
            java.io.File localFile = new java.io.File(listingsDir, listingId + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(localFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return localFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * saveListingToDatabase - יוצרת אובייקט HouseListing ושומרת אותו ב-Firestore.
     * מגדירה דירוג ברירת מחדל של 5.0, שם המארח הנטען קודם, וכל שאר הפרמטרים.
     * בהצלחה - סוגרת את ה-Activity וחוזרת למסך ה-Hosting.
     *
     * @param listingId   מזהה ייחודי של הנכס (UUID)
     * @param title       כותרת הנכס
     * @param description תיאור הנכס
     * @param location    מיקום הנכס
     * @param price       מחיר ללילה
     * @param imageUrl    URL/נתיב התמונה (Firebase Storage URL או נתיב מקומי)
     * @param hostUid     ה-UID של המארח מ-Firebase Auth
     * @param category    קטגוריה (Apartment / Villa / Cabin / Studio)
     * @param beds        מספר חדרי שינה
     * @param baths       מספר חדרי אמבטיה
     * @param wifi        האם יש Wi-Fi
     * @param ac          האם יש מיזוג
     * @param kitchen     האם יש מטבח
     * @param parking     האם יש חניה
     */
    private void saveListingToDatabase(String listingId, String title, String description, String location, double price,
                                       String imageUrl, String hostUid, String category, int beds, int baths,
                                       boolean wifi, boolean ac, boolean kitchen, boolean parking) {

        // יצירת אובייקט HouseListing חדש עם כל הנתונים
        HouseListing listing = new HouseListing(
                listingId, title, description, location, price, imageUrl,
                hostUid, currentHostName, category, 5.0f, beds, baths, wifi, ac, kitchen, parking
        );

        // שמירת אובייקט הנכס ל-Firestore תחת אוסף "listings" עם ה-listingId כמזהה מסמך
        db.collection("listings").document(listingId).set(listing)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UploadHouseActivity.this, "Listing published successfully!", Toast.LENGTH_LONG).show();
                    // סגירת המסך וחזרה לדף Hosting
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitListing.setEnabled(true);
                    Toast.makeText(UploadHouseActivity.this, "Database write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
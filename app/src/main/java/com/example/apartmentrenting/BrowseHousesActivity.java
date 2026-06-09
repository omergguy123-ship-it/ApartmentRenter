package com.example.apartmentrenting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseHousesActivity - מסך חיפוש ועיון בנכסים זמינים להשכרה.
 * נפתח בלחיצה על "Browse Listings" מהלשונית Explore.
 *
 * תכונות עיקריות:
 * - טוען את כל הנכסים מ-Firestore ומציגם ב-RecyclerView.
 * - מאפשר סינון חי (real-time) לפי טקסט חיפוש.
 * - מאפשר סינון לפי קטגוריה (All / Apartment / Villa / Cabin / Studio) דרך ChipGroup.
 * - בהרצה ראשונה, מנקה נתוני בדיקה ישנים ומזריע (seed) 3 נכסי ברירת מחדל יפים.
 * - אם ה-DB ריק, מוסיף 3 נכסים לדוגמה (mock data) עם תמונות Unsplash.
 */
public class BrowseHousesActivity extends AppCompatActivity {

    // שדה חיפוש טקסטואלי בחלק העליון של המסך
    private EditText etSearch;
    // קבוצת Chip לסינון לפי קטגוריות נכס
    private ChipGroup chipGroupCategories;
    // RecyclerView להצגת כרטיסי נכסים
    private RecyclerView recyclerViewListings;
    // מכולה להצגת מסך "ריק" כשאין תוצאות
    private View emptyStateContainer;
    // סרגל התקדמות (ספינר) בזמן טעינת נתונים מ-Firestore
    private ProgressBar progressBar;
    // כפתור חזרה למסך הקודם
    private ImageButton btnBack;

    // חיבור ל-Cloud Firestore - מסד הנתונים של האפליקציה
    private FirebaseFirestore db;
    // רשימת כל הנכסים הטעונים מ-Firestore (לא מסוננת)
    private List<HouseListing> allListingsList = new ArrayList<>();
    // ה-Adapter שמחבר בין הנתונים ל-RecyclerView
    private HouseAdapter adapter;

    // משתנים לשמירת מצב הסינון הנוכחי
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All Properties";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_houses);

        // 1. אתחול חיבור ל-Firestore
        db = FirebaseFirestore.getInstance();

        // 2. קישור רכיבי ממשק המשתמש מהקובץ activity_browse_houses.xml
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        recyclerViewListings = findViewById(R.id.recyclerViewListings);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        // 3. כפתור חזרה - סוגר את ה-Activity ומחזיר למסך הקודם
        btnBack.setOnClickListener(v -> finish());

        // 4. הגדרת RecyclerView עם LinearLayoutManager (רשימה אנכית)
        recyclerViewListings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HouseAdapter(this, new ArrayList<>());
        recyclerViewListings.setAdapter(adapter);

        // 5. ניקוי נתוני בדיקה ישנים (פעם אחת בלבד) ולאחר מכן טעינת נכסים מ-Firestore
        // cleanManualListingsAndBookingsOnce מקבלת Runnable כ-callback שמופעל לאחר הניקוי
        cleanManualListingsAndBookingsOnce(this::fetchListingsFromFirestore);

        // 6. מאזין שינויי טקסט בשורת החיפוש - מסנן בזמן אמת (real-time)
        // TextWatcher מופעל בכל שינוי תו בשדה החיפוש
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // עדכון משתנה החיפוש וסינון הרשימה מחדש
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 7. מאזין בחירת Chip לסינון לפי קטגוריה
        // setOnCheckedStateChangeListener מופעל בכל שינוי בחירה ב-ChipGroup
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    // שמירת שם הקטגוריה שנבחרה וסינון מחדש
                    currentCategoryFilter = chip.getText().toString();
                    applyFilters();
                }
            } else {
                // אין Chip נבחר - חזרה לברירת מחדל "All Properties"
                currentCategoryFilter = "All Properties";
                applyFilters();
            }
        });
    }

    /**
     * fetchListingsFromFirestore - טוענת את כל הנכסים מאוסף "listings" ב-Firestore.
     * מציגה ProgressBar בזמן הטעינה, ולאחר מכן מסננת ומציגה את התוצאות.
     * אם המאגר ריק - קוראת ל-seedMockDataIfCollectionEmpty להוספת נכסי ברירת מחדל.
     */
    private void fetchListingsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewListings.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);

        // שאילתה ל-Firestore לטעינת כל המסמכים מאוסף "listings"
        // db.collection("listings").get() היא שאילתה אסינכרונית (asynchronous)
        db.collection("listings").get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        allListingsList.clear();
                        // המרת כל QueryDocumentSnapshot לאובייקט HouseListing באמצעות toObject()
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HouseListing listing = document.toObject(HouseListing.class);
                            // הגדרת ה-listingId ידנית כי Firestore אינו שומר אוטומטית את ה-Document ID בתוך האובייקט
                            listing.setListingId(document.getId());
                            allListingsList.add(listing);
                        }

                        if (allListingsList.isEmpty()) {
                            // אם המאגר ריק - הוספת נכסים לדוגמה
                            seedMockDataIfCollectionEmpty();
                        } else {
                            recyclerViewListings.setVisibility(View.VISIBLE);
                            applyFilters();
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error loading data";
                        Toast.makeText(BrowseHousesActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        emptyStateContainer.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * seedMockDataIfCollectionEmpty - מוסיפה 3 נכסים לדוגמה ל-Firestore ולרשימה המקומית.
     * מופעלת רק כשאוסף "listings" ריק לגמרי - מבטיחה שהאפליקציה תיראה מלאה בפתיחה ראשונה.
     * תמונות הנכסים נטענות מ-Unsplash (שירות תמונות חינמי).
     */
    private void seedMockDataIfCollectionEmpty() {
        // יצירת 3 כרטיסי נכס לדוגמה: וילה, סטודיו ופנטהאוז
        List<HouseListing> mocks = new ArrayList<>();
        mocks.add(new HouseListing(
                "mock1",
                "Grand Oceanview Villa",
                "Perched on the cliffs overlooking the blue ocean, this stunning villa offers luxurious living with high ceilings, private infinity pool, fully equipped kitchen, and high-speed fiber internet. Perfect for group retreats or family vacations.",
                "Malibu, California",
                450.0,
                "https://images.unsplash.com/photo-1613977257363-707ba9348227?q=80&w=600&auto=format&fit=crop",
                "host1",
                "Jane Doe",
                "Villa",
                4.9f,
                4, 3, true, true, true, true
        ));
        mocks.add(new HouseListing(
                "mock2",
                "Minimalist Nordic Studio",
                "A clean, beautifully designed studio apartment in the heart of Copenhagen. Features premium Danish furniture, heated floors, a cozy reading nook, close proximity to cafes, and high-speed Wi-Fi.",
                "Copenhagen, Denmark",
                95.0,
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?q=80&w=600&auto=format&fit=crop",
                "host2",
                "Lars Nelson",
                "Studio",
                4.7f,
                1, 1, true, false, true, false
        ));
        mocks.add(new HouseListing(
                "mock3",
                "Luxury Highrise Penthouse",
                "Live high above the clouds in this gorgeous metropolitan penthouse. Featuring floor-to-ceiling glass windows with stunning skyline views, dynamic central AC, a spacious marble kitchen, and reserved private parking.",
                "New York, USA",
                280.0,
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=600&auto=format&fit=crop",
                "host3",
                "Sarah Vance",
                "Apartment",
                4.8f,
                2, 2, true, true, true, true
        ));

        // שמירת נכסי הדוגמה ל-Firestore ועדכון הרשימה המקומית
        for (HouseListing house : mocks) {
            db.collection("listings").document(house.getListingId()).set(house);
        }

        allListingsList.addAll(mocks);
        recyclerViewListings.setVisibility(View.VISIBLE);
        applyFilters();
    }

    /**
     * cleanManualListingsAndBookingsOnce - מנקה נכסים והזמנות ישנות מ-Firestore פעם אחת בלבד.
     * משתמשת ב-SharedPreferences עם מפתח "db_cleaned_v6" כדוגל (flag) לזיהוי אם הניקוי כבר בוצע.
     * אם לא בוצע - מוחקת את כל המסמכים מאוסף "listings" ו-"bookings",
     * ולאחר מכן מפעילה את ה-callback (onComplete) לטעינת הנכסים הרשמיים.
     * אם כבר בוצע - מפעילה את ה-callback ישירות ללא ניקוי.
     *
     * @param onComplete Runnable שיופעל לאחר השלמת הניקוי (או אם הניקוי כבר בוצע)
     */
    private void cleanManualListingsAndBookingsOnce(Runnable onComplete) {
        // SharedPreferences - אחסון מקומי פשוט ל-key-value pairs ב-Android
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("db_cleaned_v6", false)) {
            // מחיקת כל הנכסים ב-Firestore (אוסף "listings")
            db.collection("listings").get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    // אם כבר ריק - שמירת הדוגל ומעבר לטעינה
                    prefs.edit().putBoolean("db_cleaned_v6", true).apply();
                    onComplete.run();
                    return;
                }

                // מחיקת כל מסמכי הנכסים אחד אחד, ולאחר מחיקת האחרון - הפעלת callback
                int total = queryDocumentSnapshots.size();
                final int[] count = {0};
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    db.collection("listings").document(doc.getId()).delete().addOnCompleteListener(task -> {
                        count[0]++;
                        if (count[0] == total) {
                            prefs.edit().putBoolean("db_cleaned_v6", true).apply();
                            onComplete.run();
                        }
                    });
                }
            }).addOnFailureListener(e -> onComplete.run());

            // מחיקת כל ההזמנות ב-Firestore (אוסף "bookings") - ניקוי מקביל ללא callback
            db.collection("bookings").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    db.collection("bookings").document(doc.getId()).delete();
                }
            });
        } else {
            // הניקוי כבר בוצע בהפעלה קודמת - מעבר ישיר לטעינת נכסים
            onComplete.run();
        }
    }

    /**
     * applyFilters - מסננת את רשימת כל הנכסים לפי הפילטרים הנוכחיים:
     * - currentSearchQuery: חיפוש טקסטואלי לפי כותרת או מיקום (case insensitive)
     * - currentCategoryFilter: סינון לפי קטגוריה (All / Apartment / Villa וכו')
     * מעבירה את הרשימה המסוננת ל-Adapter דרך filterList() שמציגה אותה ב-RecyclerView.
     */
    private void applyFilters() {
        List<HouseListing> filtered = new ArrayList<>();

        for (HouseListing house : allListingsList) {
            // בדיקת התאמה לפי קטגוריה (equalsIgnoreCase - ללא תלות באותיות גדולות/קטנות)
            boolean matchesCategory = currentCategoryFilter.equals("All Properties") ||
                                     house.getCategory().equalsIgnoreCase(currentCategoryFilter);

            // בדיקת התאמה לפי טקסט חיפוש בכותרת ובמיקום
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                                    house.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                    house.getLocation().toLowerCase().contains(currentSearchQuery.toLowerCase());

            // הנכס נכלל ברשימה המסוננת רק אם עומד בשני הקריטריונים
            if (matchesCategory && matchesSearch) {
                filtered.add(house);
            }
        }

        // עדכון ה-Adapter עם הרשימה המסוננת
        adapter.filterList(filtered);

        // הצגת מצב "ריק" אם אין תוצאות, אחרת הצגת ה-RecyclerView
        if (filtered.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerViewListings.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerViewListings.setVisibility(View.VISIBLE);
        }
    }
}
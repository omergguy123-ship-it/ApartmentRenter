package com.example.apartmentrenting;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HouseDetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private MaterialCardView btnDetailBackCard;
    private TextView detailRating;
    private TextView detailCategory;
    private TextView detailTitle;
    private TextView detailLocation;
    private TextView detailBeds;
    private TextView detailBaths;
    private TextView detailHostName;
    private TextView detailDescription;
    
    // Amenity Chips
    private Chip chipWifi, chipAc, chipKitchen, chipParking;
    
    // Bottom Bar
    private TextView detailPrice;
    private Button btnBookNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        // 1. קישור רכיבי ממשק המשתמש (UI Views) מהקובץ activity_house_detail.xml
        detailImage = findViewById(R.id.detailImage);
        btnDetailBackCard = findViewById(R.id.btnDetailBackCard);
        detailRating = findViewById(R.id.detailRating);
        detailCategory = findViewById(R.id.detailCategory);
        detailTitle = findViewById(R.id.detailTitle);
        detailLocation = findViewById(R.id.detailLocation);
        detailBeds = findViewById(R.id.detailBeds);
        detailBaths = findViewById(R.id.detailBaths);
        detailHostName = findViewById(R.id.detailHostName);
        detailDescription = findViewById(R.id.detailDescription);

        chipWifi = findViewById(R.id.chipWifi);
        chipAc = findViewById(R.id.chipAc);
        chipKitchen = findViewById(R.id.chipKitchen);
        chipParking = findViewById(R.id.chipParking);

        detailPrice = findViewById(R.id.detailPrice);
        btnBookNow = findViewById(R.id.btnBookNow);

        // 2. הגדרת כפתור החזרה למסך הקודם
        btnDetailBackCard.setOnClickListener(v -> finish());

        // 3. חילוץ המידע שהועבר מהמסך הקודם (כמו מזהה הנכס, מחיר, תיאור, תמונות וכד')
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String listingId = extras.getString("listingId", "");
            String title = extras.getString("title", "Apartment");
            String description = extras.getString("description", "No description available.");
            String location = extras.getString("location", "Unknown Location");
            double price = extras.getDouble("price", 0.0);
            String imageUrl = extras.getString("imageUrl", "");
            String hostName = extras.getString("hostName", "Professional Host");
            String hostUid = extras.getString("hostUid", "");
            String category = extras.getString("category", "Apartment");
            float rating = extras.getFloat("rating", 4.5f);
            int beds = extras.getInt("beds", 1);
            int baths = extras.getInt("baths", 1);
            
            boolean hasWifi = extras.getBoolean("wifi", false);
            boolean hasAc = extras.getBoolean("ac", false);
            boolean hasKitchen = extras.getBoolean("kitchen", false);
            boolean hasParking = extras.getBoolean("parking", false);

            // 4. הצגת הנתונים בטקסטים השונים בממשק
            detailTitle.setText(title);
            detailDescription.setText(description);
            detailLocation.setText(location);
            detailHostName.setText("Hosted by " + hostName);
            detailCategory.setText(category);
            detailRating.setText(String.format("★ %.1f", rating));
            detailBeds.setText(beds + (beds == 1 ? " Bed" : " Beds"));
            detailBaths.setText(baths + (baths == 1 ? " Bath" : " Baths"));
            detailPrice.setText(String.format("$%.0f", price));

            // טעינת תמונת הנכס בעזרת ספריית Glide החיצונית המנהלת זיכרון ומטפלת בטעינה אסינכרונית
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.image)
                        .into(detailImage);
            } else {
                detailImage.setImageResource(R.drawable.image);
            }

            // עיצוב וצביעת הצ'יפים של השירותים המוצעים (לדוגמה: כחול אם יש Wifi, אפור כבוי אם אין)
            styleAmenityChip(chipWifi, hasWifi, "Wi-Fi");
            styleAmenityChip(chipAc, hasAc, "Air Conditioning");
            styleAmenityChip(chipKitchen, hasKitchen, "Kitchen");
            styleAmenityChip(chipParking, hasParking, "Free Parking");

            // בדיקת מניעת הזמנה עצמית (Self-Booking Prevention)
            // שולף את מזהה המשתמש המחובר כרגע מתוך FirebaseAuth
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            
            // השוואה: אם מזהה בעל הנכס (hostUid) שווה למזהה המשתמש המחובר כעת (currentUid)
            if (!currentUid.isEmpty() && hostUid.equals(currentUid)) {
                // המארח צופה בנכס שלו: ננטרל את כפתור ההזמנה, נצבע באפור ונשנה את הכיתוב ל-"הנכס שלך"
                btnBookNow.setEnabled(false);
                btnBookNow.setText("Your Property");
                btnBookNow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9CA3AF")));
            } else {
                // האורח צופה בנכס: בלחיצה, נציג לו את חלונית בקשת ההזמנה
                btnBookNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBookingRequestDialog(listingId, title, imageUrl, location, price, hostUid, hostName, currentUid);
                    }
                });
            }
        }
    }

    /**
     * פונקציה המציגה את חלונית הדיאלוג לבקשת הזמנה.
     * החלונית כוללת בחירת תאריכים מלוח שנה (DatePickerDialog),
     * הזנת הערה למארח, בדיקות תקינות תאריכים ושליחת הבקשה.
     */
    private void showBookingRequestDialog(String listingId, String title, String imageUrl, String location, double price, 
                                         String hostUid, String hostName, String currentUid) {
        // אינפלציה (טעינה) של קובץ העיצוב המותאם אישית dialog_booking_request.xml לתוך אובייקט View
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_request, null);
        EditText etDialogCheckIn = dialogView.findViewById(R.id.etDialogCheckIn);
        EditText etDialogCheckOut = dialogView.findViewById(R.id.etDialogCheckOut);
        EditText etDialogNote = dialogView.findViewById(R.id.etDialogNote);

        Calendar calendar = Calendar.getInstance();

        // מאזין ללחיצה על שדה תאריך כניסה - פותח DatePickerDialog מובנה של אנדרואיד
        etDialogCheckIn.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // שמירת התאריך הנבחר בפורמט קריא YYYY-MM-DD
                        String dateStr = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etDialogCheckIn.setText(dateStr);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // מאזין ללחיצה על שדה תאריך עזיבה - פותח DatePickerDialog
        etDialogCheckOut.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // שמירת התאריך הנבחר בפורמט קריא YYYY-MM-DD
                        String dateStr = String.format(Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etDialogCheckOut.setText(dateStr);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // בניית תיבת דו-שיח מודרנית מבית Material Components
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Request Reservation")
                .setView(dialogView)
                .setPositiveButton("Send Request", (dialog, which) -> {
                    String checkIn = etDialogCheckIn.getText().toString().trim();
                    String checkOut = etDialogCheckOut.getText().toString().trim();
                    String note = etDialogNote.getText().toString().trim();

                    // בדיקה האם השדות אינם ריקים
                    if (android.text.TextUtils.isEmpty(checkIn) || android.text.TextUtils.isEmpty(checkOut)) {
                        Toast.makeText(this, "Check-in and Check-out dates are required!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // בדיקת וולידציית תאריכים: מוודא שתאריך העזיבה אינו מוקדם מתאריך הכניסה
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        java.util.Date d1 = sdf.parse(checkIn);
                        java.util.Date d2 = sdf.parse(checkOut);
                        if (d2.before(d1)) {
                            Toast.makeText(this, "Check-out date must be after check-in date!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // העברת הנתונים לפונקציית שליחת ההזמנה לשרת
                    submitBookingRequest(listingId, title, imageUrl, location, price, hostUid, hostName, currentUid, checkIn, checkOut, note);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * פונקציה המבצעת את שמירת בקשת ההזמנה בסטטוס PENDING ב-Cloud Firestore.
     * היא תחילה טוענת את שם האורח (FirstName + LastName) כדי שיוצג למארח,
     * ולאחר מכן יוצרת אובייקט Booking חדש ושולחת אותו לענן.
     */
    private void submitBookingRequest(String listingId, String title, String imageUrl, String location, double price, 
                                     String hostUid, String hostName, String currentUid, String checkIn, String checkOut, String note) {
        Toast.makeText(this, "Sending request...", Toast.LENGTH_SHORT).show();

        // שליפת שם האורח המלא משרת ה-Firestore (אוסף users)
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(currentUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String renterName = "Guest User";
                    if (documentSnapshot.exists()) {
                        String first = documentSnapshot.getString("firstName");
                        String last = documentSnapshot.getString("lastName");
                        if (first != null) {
                            renterName = first + " " + (last != null ? last : "");
                        }
                    }

                    // יצירת מזהה ייחודי חדש עבור ההזמנה בתוך Firestore
                    String bookingId = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("bookings").document().getId();
                    long bookingDate = System.currentTimeMillis();

                    // יצירת העצם (מונחה עצמים) של ההזמנה
                    Booking booking = new Booking(bookingId, listingId, currentUid, renterName, title, imageUrl, 
                                                  location, price, bookingDate, "PENDING", checkIn, checkOut, note, hostUid);

                    // העלאת עצם ההזמנה ל-Firestore (אוסף bookings)
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("bookings").document(bookingId).set(booking)
                            .addOnSuccessListener(aVoid -> {
                                // הצגת דיאלוג אישור הצלחה למשתמש
                                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                                        .setTitle("Request Sent!")
                                        .setMessage("Your booking request for \"" + title + "\" was successfully sent to the host.\n\nOnce the host approves your request, it will appear under your Bookings profile feed.")
                                        .setPositiveButton("Awesome", (dialog, which) -> {
                                            dialog.dismiss();
                                            finish();
                                        })
                                        .setCancelable(false)
                                        .show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit request: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * פונקציה לעיצוב הצ'יפים של השירותים (Amenities).
     * מציגה צבע כחול אקטיבי אם השירות קיים, או צבע אפור כבוי במידה והשירות לא מוצע בנכס.
     */
    private void styleAmenityChip(Chip chip, boolean offered, String name) {
        if (offered) {
            chip.setText(name);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setAlpha(1.0f);
        } else {
            chip.setText(name + " (Not Offered)");
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E5E7EB"))); // צבע אפור בהיר
            chip.setTextColor(Color.parseColor("#9CA3AF")); // טקסט אפור כהה
            chip.setAlpha(0.6f);
        }
    }
}

package com.example.apartmentrenting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * BrouseHosesOrUploadListing - מסך הבית הראשי של האפליקציה (Dashboard).
 * מוצג לאחר כניסה מוצלחת ומכיל שלוש לשוניות (Tabs) בסרגל ניווט תחתון:
 *
 * 1. Explore - מציג כרטיס מהיר למעבר למסך חיפוש הנכסים (BrowseHousesActivity).
 * 2. Hosting - ניהול נכסים של המארח:
 *    - STATE A: "Become a Host" - למי שטרם נרשם כמארח
 *    - STATE B: Dashboard מארח - רשימת בקשות נכנסות, הזמנות מאושרות, ונכסים פעילים
 * 3. Profile - פרטי משתמש, הזמנות מאושרות שלי (כשוכר), כפתורי Reminder ו-Logout.
 *
 * הטעינה מ-Firestore מתבצעת ב-onResume() כדי לרענן נתונים בכל חזרה למסך.
 */
public class BrouseHosesOrUploadListing extends AppCompatActivity {

    // Tab Contents
    private View exploreContent;
    private View hostContent;
    private View profileContent;

    // Bottom Navigation Buttons
    private LinearLayout tabExplore;
    private LinearLayout tabHost;
    private LinearLayout tabProfile;

    // Bottom Navigation Icon/Text refs
    private ImageView ivTabExplore, ivTabHost, ivTabProfile;
    private TextView tvTabExplore, tvTabHost, tvTabProfile;

    // Header Welcome Text
    private TextView tvUserWelcome;

    // Profile Details
    private TextView tvProfileFullName;
    private TextView tvProfileEmail;

    // Hosting Sub-Layouts & Widgets
    private View layoutBecomeHost;
    private View layoutHostDashboard;
    private Button btnBecomeHost;
    private Button btnHostAddNew;
    private RecyclerView rvMyListings;
    private View layoutMyListingsEmpty;

    // Bookings widgets
    private RecyclerView rvMyBookings;
    private View layoutMyBookingsEmpty;

    // Booking Requests widgets
    private RecyclerView rvBookingRequests;
    private View layoutBookingRequestsEmpty;

    // Confirmed Reservations widgets
    private RecyclerView rvConfirmedReservations;
    private View layoutConfirmedReservationsEmpty;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    // Permission request launcher for notification scheduling
    /**
     * requestPermissionLauncher - משגר ActivityResult לבקשת הרשאת התראות (POST_NOTIFICATIONS).
     * נדרש ב-Android 13 (API 33, Tiramisu) ומעלה.
     * אם ניתנה הרשאה - מפעיל את scheduleNotificationAlarm().
     * זהו דרישת בגרות (Item 6) - שימוש ב-ActivityResult API החדש.
     */
    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    scheduleNotificationAlarm();
                } else {
                    Toast.makeText(this, "Notification permission is required to schedule reminders!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private String activeTab = "explore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brouse_hoses_or_upload_listing);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Initialize Views
        exploreContent = findViewById(R.id.exploreContent);
        hostContent = findViewById(R.id.hostContent);
        profileContent = findViewById(R.id.profileContent);

        tabExplore = findViewById(R.id.tabExplore);
        tabHost = findViewById(R.id.tabHost);
        tabProfile = findViewById(R.id.tabProfile);

        ivTabExplore = findViewById(R.id.ivTabExplore);
        ivTabHost = findViewById(R.id.ivTabHost);
        ivTabProfile = findViewById(R.id.ivTabProfile);

        tvTabExplore = findViewById(R.id.tvTabExplore);
        tvTabHost = findViewById(R.id.tvTabHost);
        tvTabProfile = findViewById(R.id.tvTabProfile);

        tvUserWelcome = findViewById(R.id.tvUserWelcome);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // Hosting components
        layoutBecomeHost = findViewById(R.id.layoutBecomeHost);
        layoutHostDashboard = findViewById(R.id.layoutHostDashboard);
        btnBecomeHost = findViewById(R.id.btnBecomeHost);
        btnHostAddNew = findViewById(R.id.btnHostAddNew);
        rvMyListings = findViewById(R.id.rvMyListings);
        layoutMyListingsEmpty = findViewById(R.id.layoutMyListingsEmpty);

        // Bookings components
        rvMyBookings = findViewById(R.id.rvMyBookings);
        layoutMyBookingsEmpty = findViewById(R.id.layoutMyBookingsEmpty);

        // Booking requests components
        rvBookingRequests = findViewById(R.id.rvBookingRequests);
        layoutBookingRequestsEmpty = findViewById(R.id.layoutBookingRequestsEmpty);

        // Confirmed reservations components
        rvConfirmedReservations = findViewById(R.id.rvConfirmedReservations);
        layoutConfirmedReservationsEmpty = findViewById(R.id.layoutConfirmedReservationsEmpty);

        // Setup LayoutManagers
        rvMyListings.setLayoutManager(new LinearLayoutManager(this));
        rvMyBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookingRequests.setLayoutManager(new LinearLayoutManager(this));
        rvConfirmedReservations.setLayoutManager(new LinearLayoutManager(this));

        // 2. Fetch User Data & Set up layouts
        loadUserProfile();

        // 3. Navigation Click Listeners
        tabExplore.setOnClickListener(v -> selectTab("explore"));
        tabHost.setOnClickListener(v -> selectTab("host"));
        tabProfile.setOnClickListener(v -> selectTab("profile"));

        // 4. Action Card Click Listeners
        MaterialCardView cardBrowseHouses = findViewById(R.id.cardBrowseHouses);
        cardBrowseHouses.setOnClickListener(v -> {
            Intent intent = new Intent(BrouseHosesOrUploadListing.this, BrowseHousesActivity.class);
            startActivity(intent);
        });

        // 5. Host Dashboard Add Listing Trigger
        btnHostAddNew.setOnClickListener(v -> {
            Intent intent = new Intent(BrouseHosesOrUploadListing.this, UploadHouseActivity.class);
            startActivity(intent);
        });

        // 6. Host Opt-in Button Trigger
        btnBecomeHost.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                db.collection("users").document(currentUser.getUid()).update("isHost", true)
                        .addOnSuccessListener(aVoid -> {
                            layoutBecomeHost.setVisibility(View.GONE);
                            layoutHostDashboard.setVisibility(View.VISIBLE);
                            checkHostStatusAndLoad();
                            loadHostBookingRequests();
                            loadHostConfirmedReservations();
                        })
                        .addOnFailureListener(e -> {
                            layoutBecomeHost.setVisibility(View.GONE);
                            layoutHostDashboard.setVisibility(View.VISIBLE);
                            checkHostStatusAndLoad();
                            loadHostBookingRequests();
                            loadHostConfirmedReservations();
                        });
            }
        });

        // 7. Sign Out Listener
        Button btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(BrouseHosesOrUploadListing.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BrouseHosesOrUploadListing.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 8. Schedule Reminder Listener
        Button btnScheduleReminder = findViewById(R.id.btnScheduleReminder);
        btnScheduleReminder.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    scheduleNotificationAlarm();
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                scheduleNotificationAlarm();
            }
        });
    }

    /**
     * scheduleNotificationAlarm - מתזמנת התראה מקומית דרך AlarmManager.
     * AlarmManager הוא שירות מערכת אנדרואיד לתזמון משימות בעתיד.
     * יוצר PendingIntent שיפעיל את ReminderReceiver (BroadcastReceiver) לאחר 10 שניות.
     * FLAG_IMMUTABLE - נדרש ב-Android 12 ומעלה לאבטחת PendingIntents.
     * ELAPSED_REALTIME_WAKEUP - הטיימר מתחיל ממועד הדלקת המכשיר ומעיר אותו אם ישן.
     * זהו דרישת בגרות (Item 6) - שימוש ב-AlarmManager ו-Notifications.
     */
    private void scheduleNotificationAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            // PendingIntent עוטף Intent ומאפשר למערכת ההפעלה להפעיל אותו בעתיד
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
            );

            // הגדרת זמן ההפעלה: 10 שניות מהרגע הנוכחי
            long triggerTime = SystemClock.elapsedRealtime() + 10000;
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
            
            Toast.makeText(this, "Reminder scheduled in 10 seconds! Close/exit the app to test notifications.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * onResume - נקרא בכל פעם שה-Activity חוזר לחזית (לדוגמה: חזרה מ-HouseDetailActivity).
     * מרענן את כל רשימות הנתונים מ-Firestore כדי להציג מצב עדכני:
     * - בדיקת סטטוס מארח ונכסיו
     * - בקשות הזמנה נכנסות (PENDING)
     * - הזמנות מאושרות (APPROVED) של האורחים
     * - הזמנות מאושרות של המשתמש הנוכחי (כשוכר)
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkHostStatusAndLoad();
        loadHostBookingRequests();
        loadHostConfirmedReservations();
        loadMyBookings();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();
            tvProfileEmail.setText(email);

            db.collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                if (firstName != null) {
                                    tvUserWelcome.setText("Hello, " + firstName + "!");
                                    tvProfileFullName.setText(firstName + " " + (lastName != null ? lastName : ""));
                                } else {
                                    tvUserWelcome.setText("Welcome back!");
                                    tvProfileFullName.setText("Guest User");
                                }
                            }
                        }
                    });
        }
    }

    /**
     * checkHostStatusAndLoad - בודקת אם המשתמש הנוכחי הוא מארח ומציגה את הממשק המתאים.
     * שולחת שאילתה ל-Firestore לאוסף "listings" כדי לבדוק אם יש נכסים בבעלות המשתמש.
     * - אם יש נכסים: מציגה את dashboard המארח עם הנכסים.
     * - אם אין נכסים: בודקת את שדה isHost במסמך המשתמש ב-Firestore:
     *   - אם isHost=true (הצטרף כמארח ידנית): מציגה dashboard מארח ריק.
     *   - אם isHost=false/null: מציגה מסך "Become a Host".
     */
    private void checkHostStatusAndLoad() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("listings").whereEqualTo("hostUid", uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<HouseListing> myListings = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            HouseListing listing = doc.toObject(HouseListing.class);
                            if (listing != null) {
                                listing.setListingId(doc.getId());
                                myListings.add(listing);
                            }
                        }

                        if (!myListings.isEmpty()) {
                            // User owns listings -> Show listings dashboard directly
                            layoutBecomeHost.setVisibility(View.GONE);
                            layoutHostDashboard.setVisibility(View.VISIBLE);
                            layoutMyListingsEmpty.setVisibility(View.GONE);
                            rvMyListings.setVisibility(View.VISIBLE);

                            HouseAdapter hostAdapter = new HouseAdapter(this, myListings);
                            rvMyListings.setAdapter(hostAdapter);
                        } else {
                            // Check user document for explicit isHost opt-in
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        boolean isHost = false;
                                        if (documentSnapshot.exists()) {
                                            Boolean flag = documentSnapshot.getBoolean("isHost");
                                            if (flag != null && flag) {
                                                isHost = true;
                                            }
                                        }

                                        if (isHost) {
                                            layoutBecomeHost.setVisibility(View.GONE);
                                            layoutHostDashboard.setVisibility(View.VISIBLE);
                                            layoutMyListingsEmpty.setVisibility(View.VISIBLE);
                                            rvMyListings.setVisibility(View.GONE);
                                        } else {
                                            layoutBecomeHost.setVisibility(View.VISIBLE);
                                            layoutHostDashboard.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        layoutBecomeHost.setVisibility(View.VISIBLE);
                                        layoutHostDashboard.setVisibility(View.GONE);
                                    });
                        }
                    }
                });
    }

    /**
     * loadMyBookings - טוענת את ההזמנות המאושרות של המשתמש הנוכחי (כשוכר).
     * שואלת מ-Firestore את אוסף "bookings" לפי renterUid ו-status="APPROVED".
     * מוצג בלשונית Profile תחת "My Bookings".
     * מסרב להזמנות PENDING ו-DECLINED - מציגות רק הזמנות שאושרו.
     */
    private void loadMyBookings() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("bookings")
                .whereEqualTo("renterUid", currentUser.getUid())
                .whereEqualTo("status", "APPROVED")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Booking> myBookings = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) {
                                booking.setBookingId(doc.getId());
                                myBookings.add(booking);
                            }
                        }

                        if (myBookings.isEmpty()) {
                            rvMyBookings.setVisibility(View.GONE);
                            layoutMyBookingsEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvMyBookings.setVisibility(View.VISIBLE);
                            layoutMyBookingsEmpty.setVisibility(View.GONE);

                            BookingAdapter bookingAdapter = new BookingAdapter(this, myBookings);
                            rvMyBookings.setAdapter(bookingAdapter);
                        }
                    } else {
                        rvMyBookings.setVisibility(View.GONE);
                        layoutMyBookingsEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * loadHostBookingRequests - טוענת את בקשות ההזמנה הממתינות לאישור המארח.
     * שואלת מ-Firestore את אוסף "bookings" לפי hostUid ו-status="PENDING".
     * מוצג בלשונית Hosting תחת "Incoming Requests".
     * מעביר ל-BookingRequestAdapter callback שמרענן הנתונים לאחר אישור/דחייה.
     */
    private void loadHostBookingRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("bookings")
                .whereEqualTo("hostUid", currentUser.getUid())
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Booking> requests = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) {
                                booking.setBookingId(doc.getId());
                                requests.add(booking);
                            }
                        }

                        if (requests.isEmpty()) {
                            rvBookingRequests.setVisibility(View.GONE);
                            layoutBookingRequestsEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvBookingRequests.setVisibility(View.VISIBLE);
                            layoutBookingRequestsEmpty.setVisibility(View.GONE);

                            BookingRequestAdapter adapter = new BookingRequestAdapter(this, requests, new BookingRequestAdapter.OnActionCompleted() {
                                @Override
                                public void onCompleted() {
                                    loadHostBookingRequests();
                                    loadHostConfirmedReservations();
                                    loadMyBookings();
                                }
                            });
                            rvBookingRequests.setAdapter(adapter);
                        }
                    } else {
                        rvBookingRequests.setVisibility(View.GONE);
                        layoutBookingRequestsEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * loadHostConfirmedReservations - טוענת את ההזמנות המאושרות של אורחי המארח.
     * שואלת מ-Firestore את אוסף "bookings" לפי hostUid ו-status="APPROVED".
     * מוצג בלשונית Hosting תחת "Confirmed Reservations".
     * מאפשר למארח לראות את רשימת כל האורחים שהוא אישר להם הזמנה.
     */
    private void loadHostConfirmedReservations() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("bookings")
                .whereEqualTo("hostUid", currentUser.getUid())
                .whereEqualTo("status", "APPROVED")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Booking> confirmedList = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) {
                                booking.setBookingId(doc.getId());
                                confirmedList.add(booking);
                            }
                        }

                        if (confirmedList.isEmpty()) {
                            rvConfirmedReservations.setVisibility(View.GONE);
                            layoutConfirmedReservationsEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvConfirmedReservations.setVisibility(View.VISIBLE);
                            layoutConfirmedReservationsEmpty.setVisibility(View.GONE);

                            ConfirmedReservationsAdapter adapter = new ConfirmedReservationsAdapter(this, confirmedList);
                            rvConfirmedReservations.setAdapter(adapter);
                        }
                    } else {
                        rvConfirmedReservations.setVisibility(View.GONE);
                        layoutConfirmedReservationsEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * selectTab - מחליפה את תוכן הלשונית הפעילה בסרגל הניווט התחתון.
     * מסתירה את כל תכני הלשוניות ומציגה רק את זו שנבחרה.
     * מעדכנת צבעים ועיצוב (צבע ראשי לפעיל, אפור לבלתי פעיל).
     *
     * @param tabName שם הלשונית שנבחרה: "explore" / "host" / "profile"
     */
    private void selectTab(String tabName) {
        activeTab = tabName;
        
        // Hide all contents
        exploreContent.setVisibility(View.GONE);
        hostContent.setVisibility(View.GONE);
        profileContent.setVisibility(View.GONE);

        // Reset tab colors
        int colorInactive = ContextCompat.getColor(this, R.color.colorTextSecondary);
        ivTabExplore.setImageTintList(ColorStateList.valueOf(colorInactive));
        tvTabExplore.setTextColor(colorInactive);
        tvTabExplore.setTypeface(null, android.graphics.Typeface.NORMAL);

        ivTabHost.setImageTintList(ColorStateList.valueOf(colorInactive));
        tvTabHost.setTextColor(colorInactive);
        tvTabHost.setTypeface(null, android.graphics.Typeface.NORMAL);

        ivTabProfile.setImageTintList(ColorStateList.valueOf(colorInactive));
        tvTabProfile.setTextColor(colorInactive);
        tvTabProfile.setTypeface(null, android.graphics.Typeface.NORMAL);

        int colorActive = ContextCompat.getColor(this, R.color.colorPrimary);

        switch (tabName) {
            case "explore":
                exploreContent.setVisibility(View.VISIBLE);
                ivTabExplore.setImageTintList(ColorStateList.valueOf(colorActive));
                tvTabExplore.setTextColor(colorActive);
                tvTabExplore.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "host":
                hostContent.setVisibility(View.VISIBLE);
                ivTabHost.setImageTintList(ColorStateList.valueOf(colorActive));
                tvTabHost.setTextColor(colorActive);
                tvTabHost.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "profile":
                profileContent.setVisibility(View.VISIBLE);
                ivTabProfile.setImageTintList(ColorStateList.valueOf(colorActive));
                tvTabProfile.setTextColor(colorActive);
                tvTabProfile.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}
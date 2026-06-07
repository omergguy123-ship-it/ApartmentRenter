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

        // 1. Bind UI elements
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

        // 2. Set back action
        btnDetailBackCard.setOnClickListener(v -> finish());

        // 3. Extract parameters from intent bundle
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

            // 4. Bind values to UI
            detailTitle.setText(title);
            detailDescription.setText(description);
            detailLocation.setText(location);
            detailHostName.setText("Hosted by " + hostName);
            detailCategory.setText(category);
            detailRating.setText(String.format("★ %.1f", rating));
            detailBeds.setText(beds + (beds == 1 ? " Bed" : " Beds"));
            detailBaths.setText(baths + (baths == 1 ? " Bath" : " Baths"));
            detailPrice.setText(String.format("$%.0f", price));

            // Load listing image
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.image)
                        .into(detailImage);
            } else {
                detailImage.setImageResource(R.drawable.image);
            }

            // Style Amenity Chips
            styleAmenityChip(chipWifi, hasWifi, "Wi-Fi");
            styleAmenityChip(chipAc, hasAc, "Air Conditioning");
            styleAmenityChip(chipKitchen, hasKitchen, "Kitchen");
            styleAmenityChip(chipParking, hasParking, "Free Parking");

            // Self-Booking Prevention check
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            if (!currentUid.isEmpty() && hostUid.equals(currentUid)) {
                btnBookNow.setEnabled(false);
                btnBookNow.setText("Your Property");
                btnBookNow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9CA3AF")));
            } else {
                // Book Now Click Event
                btnBookNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBookingRequestDialog(listingId, title, imageUrl, location, price, hostUid, hostName, currentUid);
                    }
                });
            }
        }
    }

    private void showBookingRequestDialog(String listingId, String title, String imageUrl, String location, double price, 
                                         String hostUid, String hostName, String currentUid) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_request, null);
        EditText etDialogCheckIn = dialogView.findViewById(R.id.etDialogCheckIn);
        EditText etDialogCheckOut = dialogView.findViewById(R.id.etDialogCheckOut);
        EditText etDialogNote = dialogView.findViewById(R.id.etDialogNote);

        java.util.Calendar calendar = java.util.Calendar.getInstance();

        etDialogCheckIn.setOnClickListener(v -> {
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = String.format(java.util.Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etDialogCheckIn.setText(dateStr);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        etDialogCheckOut.setOnClickListener(v -> {
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = String.format(java.util.Locale.US, "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etDialogCheckOut.setText(dateStr);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Request Reservation")
                .setView(dialogView)
                .setPositiveButton("Send Request", (dialog, which) -> {
                    String checkIn = etDialogCheckIn.getText().toString().trim();
                    String checkOut = etDialogCheckOut.getText().toString().trim();
                    String note = etDialogNote.getText().toString().trim();

                    if (android.text.TextUtils.isEmpty(checkIn) || android.text.TextUtils.isEmpty(checkOut)) {
                        Toast.makeText(this, "Check-in and Check-out dates are required!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                        java.util.Date d1 = sdf.parse(checkIn);
                        java.util.Date d2 = sdf.parse(checkOut);
                        if (d2.before(d1)) {
                            Toast.makeText(this, "Check-out date must be after check-in date!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (Exception e) {
                        // ignore or handle
                    }

                    submitBookingRequest(listingId, title, imageUrl, location, price, hostUid, hostName, currentUid, checkIn, checkOut, note);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void submitBookingRequest(String listingId, String title, String imageUrl, String location, double price, 
                                     String hostUid, String hostName, String currentUid, String checkIn, String checkOut, String note) {
        Toast.makeText(this, "Sending request...", Toast.LENGTH_SHORT).show();

        // Retrieve renter's profile name from database
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

                    String bookingId = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("bookings").document().getId();
                    long bookingDate = System.currentTimeMillis();

                    Booking booking = new Booking(bookingId, listingId, currentUid, renterName, title, imageUrl, 
                                                  location, price, bookingDate, "PENDING", checkIn, checkOut, note, hostUid);

                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("bookings").document(bookingId).set(booking)
                            .addOnSuccessListener(aVoid -> {
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

    private void styleAmenityChip(Chip chip, boolean offered, String name) {
        if (offered) {
            chip.setText(name);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setAlpha(1.0f);
        } else {
            chip.setText(name + " (Not Offered)");
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E5E7EB"))); // light grey
            chip.setTextColor(Color.parseColor("#9CA3AF")); // disabled grey text
            chip.setAlpha(0.6f);
        }
    }
}

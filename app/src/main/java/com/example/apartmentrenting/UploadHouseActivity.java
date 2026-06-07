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

public class UploadHouseActivity extends AppCompatActivity {

    private EditText etUploadTitle, etUploadDescription, etUploadLocation, etUploadPrice, etUploadBeds, etUploadBaths;
    private AutoCompleteTextView spinnerUploadCategory;
    private Chip chipUploadWifi, chipUploadAc, chipUploadKitchen, chipUploadParking;
    private MaterialCardView cardUploadImage;
    private LinearLayout uploadImagePlaceholder;
    private ImageView ivUploadPreview;
    private Button btnSubmitListing;
    private ImageButton btnUploadBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;
    private String currentHostName = "Professional Host";

    // Register Photo Picker
    private final ActivityResultLauncher<String> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // 1. Bind UI Views
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

        // 2. Setup Back Button
        btnUploadBack.setOnClickListener(v -> finish());

        // 3. Setup Categories Dropdown
        String[] categories = new String[]{"Apartment", "Villa", "Cabin", "Studio"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerUploadCategory.setAdapter(categoryAdapter);
        spinnerUploadCategory.setText(categories[0], false); // default to Apartment

        // 4. Load Current User Profile (for designating Host Name)
        fetchHostProfile();

        // 5. Image Click Handler
        cardUploadImage.setOnClickListener(v -> pickPhotoLauncher.launch("image/*"));

        // 6. Submit Button Handler
        btnSubmitListing.setOnClickListener(v -> validateAndSubmitListing());
    }

    private void fetchHostProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
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

    private void validateAndSubmitListing() {
        String title = etUploadTitle.getText().toString().trim();
        String description = etUploadDescription.getText().toString().trim();
        String location = etUploadLocation.getText().toString().trim();
        String priceStr = etUploadPrice.getText().toString().trim();
        String bedsStr = etUploadBeds.getText().toString().trim();
        String bathsStr = etUploadBaths.getText().toString().trim();
        String category = spinnerUploadCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location) || 
            TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(bedsStr) || TextUtils.isEmpty(bathsStr)) {
            Toast.makeText(this, "Please fill in all listing details!", Toast.LENGTH_LONG).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int beds = Integer.parseInt(bedsStr);
        int baths = Integer.parseInt(bathsStr);

        boolean wifi = chipUploadWifi.isChecked();
        boolean ac = chipUploadAc.isChecked();
        boolean kitchen = chipUploadKitchen.isChecked();
        boolean parking = chipUploadParking.isChecked();

        btnSubmitListing.setEnabled(false);
        Toast.makeText(this, "Publishing listing...", Toast.LENGTH_SHORT).show();

        // Create a unique listing ID
        String listingId = UUID.randomUUID().toString();
        String hostUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";

        if (selectedImageUri != null) {
            // Cache the selected photo locally so it remains accessible offline/after app restarts
            String localCachePath = copyImageToLocalCache(selectedImageUri, listingId);

            // Attempt to upload photo to Firebase Storage
            StorageReference ref = storage.getReference().child("listings/" + listingId + ".jpg");
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String imageUrl = downloadUri.toString();
                                saveListingToDatabase(listingId, title, description, location, price, imageUrl, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                            })
                            .addOnFailureListener(e -> {
                                // Fallback to local cache path if download URL retrieval fails
                                saveListingToDatabase(listingId, title, description, location, price, localCachePath, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                            }))
                    .addOnFailureListener(e -> {
                        // Fallback to local cache path if Storage write permission is Denied/Disabled
                        Toast.makeText(UploadHouseActivity.this, "Storage offline. Saving photo to local device cache.", Toast.LENGTH_SHORT).show();
                        saveListingToDatabase(listingId, title, description, location, price, localCachePath, hostUid, category, beds, baths, wifi, ac, kitchen, parking);
                    });
        } else {
            // If no photo was chosen, save with empty string
            saveListingToDatabase(listingId, title, description, location, price, "", hostUid, category, beds, baths, wifi, ac, kitchen, parking);
        }
    }

    private String copyImageToLocalCache(Uri uri, String listingId) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return "";

            java.io.File cacheDir = getCacheDir();
            java.io.File listingsDir = new java.io.File(cacheDir, "listings");
            if (!listingsDir.exists()) {
                listingsDir.mkdirs();
            }

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

    private void saveListingToDatabase(String listingId, String title, String description, String location, double price,
                                       String imageUrl, String hostUid, String category, int beds, int baths,
                                       boolean wifi, boolean ac, boolean kitchen, boolean parking) {

        HouseListing listing = new HouseListing(
                listingId, title, description, location, price, imageUrl, 
                hostUid, currentHostName, category, 5.0f, beds, baths, wifi, ac, kitchen, parking
        );

        db.collection("listings").document(listingId).set(listing)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UploadHouseActivity.this, "Listing published successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitListing.setEnabled(true);
                    Toast.makeText(UploadHouseActivity.this, "Database write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
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

public class BrowseHousesActivity extends AppCompatActivity {

    private EditText etSearch;
    private ChipGroup chipGroupCategories;
    private RecyclerView recyclerViewListings;
    private View emptyStateContainer;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private List<HouseListing> allListingsList = new ArrayList<>();
    private HouseAdapter adapter;

    private String currentSearchQuery = "";
    private String currentCategoryFilter = "All Properties";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_houses);

        db = FirebaseFirestore.getInstance();

        // 1. Bind Views
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        recyclerViewListings = findViewById(R.id.recyclerViewListings);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        // 2. Setup Back Button
        btnBack.setOnClickListener(v -> finish());

        // 3. Setup RecyclerView
        recyclerViewListings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HouseAdapter(this, new ArrayList<>());
        recyclerViewListings.setAdapter(adapter);

        // 4. Clean manual test data exactly once on first load, then fetch
        cleanManualListingsAndBookingsOnce(this::fetchListingsFromFirestore);

        // 5. Setup Search Filter Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 6. Setup Category Chip Listener
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    currentCategoryFilter = chip.getText().toString();
                    applyFilters();
                }
            } else {
                currentCategoryFilter = "All Properties";
                applyFilters();
            }
        });
    }

    private void fetchListingsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewListings.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);

        db.collection("listings").get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        allListingsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            HouseListing listing = document.toObject(HouseListing.class);
                            listing.setListingId(document.getId());
                            allListingsList.add(listing);
                        }

                        if (allListingsList.isEmpty()) {
                            // If empty, let's inject a few mock listings to guarantee beautiful UI on first open!
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

    private void seedMockDataIfCollectionEmpty() {
        // Create 3 beautiful premium default cards so the app has content immediately
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

        // Insert mock data to Firestore to keep database alive, but also bind locally
        for (HouseListing house : mocks) {
            db.collection("listings").document(house.getListingId()).set(house);
        }

        allListingsList.addAll(mocks);
        recyclerViewListings.setVisibility(View.VISIBLE);
        applyFilters();
    }

    private void cleanManualListingsAndBookingsOnce(Runnable onComplete) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("db_cleaned_v6", false)) {
            // Delete all listings so they are forced to re-seed with the new Unsplash URLs!
            db.collection("listings").get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    prefs.edit().putBoolean("db_cleaned_v6", true).apply();
                    onComplete.run();
                    return;
                }

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

            // Delete all bookings
            db.collection("bookings").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    db.collection("bookings").document(doc.getId()).delete();
                }
            });
        } else {
            onComplete.run();
        }
    }

    private void applyFilters() {
        List<HouseListing> filtered = new ArrayList<>();

        for (HouseListing house : allListingsList) {
            boolean matchesCategory = currentCategoryFilter.equals("All Properties") || 
                                     house.getCategory().equalsIgnoreCase(currentCategoryFilter);
            
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                    house.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) || 
                                    house.getLocation().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesCategory && matchesSearch) {
                filtered.add(house);
            }
        }

        adapter.filterList(filtered);

        if (filtered.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerViewListings.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerViewListings.setVisibility(View.VISIBLE);
        }
    }
}
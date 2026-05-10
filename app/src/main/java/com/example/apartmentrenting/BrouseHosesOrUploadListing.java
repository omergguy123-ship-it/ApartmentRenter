package com.example.apartmentrenting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class BrouseHosesOrUploadListing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Ensure this layout name matches your actual XML file name
        setContentView(R.layout.activity_brouse_hoses_or_upload_listing);

        // Handle Window Insets (Edge-to-Edge padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize Browse Houses Button
        MaterialButton btnBrowse = findViewById(R.id.btnBrowseHouses);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change 'BrowseActivity' to the actual name of your browse class
                Intent intent = new Intent(BrouseHosesOrUploadListing.this, BrowseHousesActivity.class);
                startActivity(intent);
            }
        });

        // 2. Initialize Upload Listing Button
        MaterialButton btnUpload = findViewById(R.id.btnUploadListing);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change 'UploadActivity' to the actual name of your upload class
                Intent intent = new Intent(BrouseHosesOrUploadListing.this, UploadHouseActivity.class);
                startActivity(intent);
            }
        });
    }
}
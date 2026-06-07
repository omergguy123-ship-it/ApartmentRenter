package com.example.apartmentrenting;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {
    private Context context;
    private List<HouseListing> listingsList;
    private List<HouseListing> listingsListFull; // For filtering search

    public HouseAdapter(Context context, List<HouseListing> listingsList) {
        this.context = context;
        this.listingsList = listingsList;
        this.listingsListFull = new ArrayList<>(listingsList);
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house, parent, false);
        return new HouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {
        HouseListing listing = listingsList.get(position);

        holder.houseTitle.setText(listing.getTitle());
        holder.houseLocation.setText(listing.getLocation());
        holder.houseCategory.setText(listing.getCategory());
        holder.houseRating.setText(String.format("★ %.1f", listing.getRating()));
        holder.housePrice.setText(String.format("$%.0f / night", listing.getPrice()));

        // Load image using Glide with fallback to local resource
        if (listing.getImageUrl() != null && !listing.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(listing.getImageUrl())
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image)
                    .into(holder.houseImage);
        } else {
            holder.houseImage.setImageResource(R.drawable.image);
        }

        // On Click -> navigate to Detail screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HouseDetailActivity.class);
                intent.putExtra("listingId", listing.getListingId());
                intent.putExtra("title", listing.getTitle());
                intent.putExtra("description", listing.getDescription());
                intent.putExtra("location", listing.getLocation());
                intent.putExtra("price", listing.getPrice());
                intent.putExtra("imageUrl", listing.getImageUrl());
                intent.putExtra("hostUid", listing.getHostUid());
                intent.putExtra("hostName", listing.getHostName());
                intent.putExtra("category", listing.getCategory());
                intent.putExtra("rating", listing.getRating());
                intent.putExtra("beds", listing.getBeds());
                intent.putExtra("baths", listing.getBaths());
                intent.putExtra("wifi", listing.isWifi());
                intent.putExtra("ac", listing.isAc());
                intent.putExtra("kitchen", listing.isKitchen());
                intent.putExtra("parking", listing.isParking());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listingsList.size();
    }

    // Dynamic search filtering
    public void filterList(List<HouseListing> filteredList) {
        this.listingsList = filteredList;
        notifyDataSetChanged();
    }

    public static class HouseViewHolder extends RecyclerView.ViewHolder {
        ImageView houseImage;
        TextView houseTitle, houseLocation, houseCategory, houseRating, housePrice;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            houseImage = itemView.findViewById(R.id.houseImage);
            houseTitle = itemView.findViewById(R.id.houseTitle);
            houseLocation = itemView.findViewById(R.id.houseLocation);
            houseCategory = itemView.findViewById(R.id.houseCategory);
            houseRating = itemView.findViewById(R.id.houseRating);
            housePrice = itemView.findViewById(R.id.housePrice);
        }
    }
}

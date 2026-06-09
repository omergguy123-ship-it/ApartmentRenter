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

/**
 * HouseAdapter - מתאם (Adapter) עבור RecyclerView של רשימת הנכסים.
 * מוצג ב-BrowseHousesActivity (חיפוש ועיון) וגם ב-BrouseHosesOrUploadListing (הנכסים של המארח).
 *
 * אחראי על:
 * - יצירת כרטיסי נכס (item_house.xml) עבור כל נכס ברשימה.
 * - טעינת תמונת הנכס בעזרת ספריית Glide החיצונית.
 * - ניווט למסך פרטי הנכס (HouseDetailActivity) בלחיצה על כרטיס.
 * - סינון רשימה דינמי לפי טקסט/קטגוריה דרך פונקציית filterList.
 */
public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {
    // context - ה-Context של ה-Activity שמשתמשת ב-Adapter (לטעינת Views והשקת Intents)
    private Context context;
    // listingsList - הרשימה הנוכחית המוצגת (יכולה להיות מסוננת)
    private List<HouseListing> listingsList;
    // listingsListFull - עותק מלא של הרשימה המקורית לשימוש בסינון (לא בשימוש ישיר כרגע)
    private List<HouseListing> listingsListFull;

    public HouseAdapter(Context context, List<HouseListing> listingsList) {
        this.context = context;
        this.listingsList = listingsList;
        this.listingsListFull = new ArrayList<>(listingsList);
    }

    /**
     * onCreateViewHolder - מתבצע פעם אחת לכל סוג ViewHolder.
     * מנפח (inflate) את קובץ item_house.xml ומחזיר ViewHolder חדש.
     */
    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_house, parent, false);
        return new HouseViewHolder(view);
    }

    /**
     * onBindViewHolder - מתבצע לכל פריט ברשימה הנראה על המסך.
     * מאכלס את הנתונים של נכס ספציפי לתוך ה-ViewHolder שלו.
     * כולל: טעינת תמונה עם Glide, הצגת טקסטים, והגדרת מאזין לחיצה לניווט.
     *
     * @param holder   ה-ViewHolder שמחזיק את הרכיבים הגרפיים של הכרטיס
     * @param position מיקום הפריט ברשימה
     */
    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {
        HouseListing listing = listingsList.get(position);

        // הצגת טקסטי הנכס: כותרת, מיקום, קטגוריה, דירוג ומחיר
        holder.houseTitle.setText(listing.getTitle());
        holder.houseLocation.setText(listing.getLocation());
        holder.houseCategory.setText(listing.getCategory());
        holder.houseRating.setText(String.format("★ %.1f", listing.getRating()));
        holder.housePrice.setText(String.format("$%.0f / night", listing.getPrice()));

        // טעינת תמונת הנכס בעזרת ספריית Glide - ספרייה חיצונית לטעינת תמונות אסינכרוניות מהרשת.
        // placeholder - תמונת ברירת מחדל בזמן הטעינה. error - תמונה אם נכשלה הטעינה.
        if (listing.getImageUrl() != null && !listing.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(listing.getImageUrl())
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image)
                    .into(holder.houseImage);
        } else {
            holder.houseImage.setImageResource(R.drawable.image);
        }

        // בלחיצה על כרטיס הנכס - בניית Intent עם כל פרטי הנכס ומעבר למסך HouseDetailActivity.
        // העברת הנתונים דרך Intent Extras (Bundle) מאפשרת להציגם ב-HouseDetailActivity ללא שאילתת Firestore נוספת.
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

    /**
     * filterList - מקבלת רשימה מסוננת ומעדכנת את ה-Adapter להציג אותה.
     * נקראת מ-BrowseHousesActivity כאשר המשתמש מקיש בשורת החיפוש או משנה קטגוריה.
     * notifyDataSetChanged() מעדכנת את ה-RecyclerView לצייר מחדש את כל הפריטים.
     *
     * @param filteredList הרשימה המסוננת להצגה
     */
    public void filterList(List<HouseListing> filteredList) {
        this.listingsList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * HouseViewHolder - מחלקה פנימית (Inner Class) המחזיקה רפרנסים לרכיבי ה-UI של כרטיס הנכס.
     * דפוס ViewHolder מונע קריאות מיותרות ל-findViewById בכל גלילה ומשפר ביצועים.
     */
    public static class HouseViewHolder extends RecyclerView.ViewHolder {
        ImageView houseImage;
        TextView houseTitle, houseLocation, houseCategory, houseRating, housePrice;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור כל רכיב ה-UI מ-item_house.xml לפי ה-ID שלו
            houseImage = itemView.findViewById(R.id.houseImage);
            houseTitle = itemView.findViewById(R.id.houseTitle);
            houseLocation = itemView.findViewById(R.id.houseLocation);
            houseCategory = itemView.findViewById(R.id.houseCategory);
            houseRating = itemView.findViewById(R.id.houseRating);
            housePrice = itemView.findViewById(R.id.housePrice);
        }
    }
}

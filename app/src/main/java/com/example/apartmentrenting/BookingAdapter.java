package com.example.apartmentrenting;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * BookingAdapter - מתאם (Adapter) עבור RecyclerView של הזמנות מאושרות של השוכר.
 * מוצג בלשונית "Profile" ב-BrouseHosesOrUploadListing תחת "My Bookings".
 *
 * מציג רק הזמנות בסטטוס "APPROVED" (אושרו על ידי המארח).
 * בלחיצה על הזמנה - שולף את פרטי הנכס העדכניים מ-Firestore ומנווט ל-HouseDetailActivity.
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    // context - ה-Context של ה-Activity שמשתמשת ב-Adapter
    private Context context;
    // bookingList - רשימת ההזמנות המאושרות של המשתמש המחובר
    private List<Booking> bookingList;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    /**
     * onCreateViewHolder - מנפח (inflate) את קובץ item_booking.xml ויוצר ViewHolder חדש.
     */
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    /**
     * onBindViewHolder - מאכלס את נתוני ההזמנה לתוך הכרטיס.
     * ממיר חותמת זמן Unix לפורמט קריא למשתמש (לדוגמה: "Jun 12, 2026").
     * בלחיצה - שולף נתוני נכס מ-Firestore ומנווט לפרטי הנכס.
     *
     * @param holder   ה-ViewHolder שמחזיק את הרכיבים הגרפיים של כרטיס ההזמנה
     * @param position מיקום הפריט ברשימה
     */
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.bookingTitle.setText(booking.getPropertyTitle());
        holder.bookingLocation.setText(booking.getLocation());
        holder.bookingPrice.setText(String.format("$%.0f", booking.getPrice()));

        // המרת חותמת זמן Unix (milliseconds) לפורמט קריא בעזרת Calendar ו-DateFormat
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(booking.getBookingDate());
        String date = DateFormat.format("MMM d, yyyy", cal).toString();
        holder.bookingDateText.setText("Booked on: " + date);

        // טעינת תמונת הנכס בעזרת ספריית Glide - ניהול אסינכרוני של טעינת תמונות מהרשת
        if (booking.getImageUrl() != null && !booking.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(booking.getImageUrl())
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image)
                    .into(holder.bookingImage);
        } else {
            holder.bookingImage.setImageResource(R.drawable.image);
        }

        // בלחיצה על כרטיס ההזמנה - שאילתת Firestore לטעינת פרטי הנכס העדכניים,
        // ולאחר מכן מעבר ל-HouseDetailActivity עם הנתונים בתוך Intent Extras.
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Opening listing details...", Toast.LENGTH_SHORT).show();
            // שאילתה מ-Firestore לאוסף "listings" לפי ה-listingId של ההזמנה
            FirebaseFirestore.getInstance().collection("listings").document(booking.getListingId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // המרת מסמך Firestore לאובייקט HouseListing עם toObject()
                            HouseListing listing = documentSnapshot.toObject(HouseListing.class);
                            if (listing != null) {
                                listing.setListingId(documentSnapshot.getId());
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
                        } else {
                            Toast.makeText(context, "This property is no longer active", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    /**
     * BookingViewHolder - מחלקה פנימית המחזיקה רפרנסים לרכיבי ה-UI של כרטיס ההזמנה.
     * דפוס ViewHolder מונע קריאות חוזרות ל-findViewById ומשפר ביצועי גלילה.
     */
    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView bookingImage;
        TextView bookingTitle, bookingLocation, bookingDateText, bookingPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור רכיבי ה-UI מ-item_booking.xml
            bookingImage = itemView.findViewById(R.id.bookingImage);
            bookingTitle = itemView.findViewById(R.id.bookingTitle);
            bookingLocation = itemView.findViewById(R.id.bookingLocation);
            bookingDateText = itemView.findViewById(R.id.bookingDateText);
            bookingPrice = itemView.findViewById(R.id.bookingPrice);
        }
    }
}

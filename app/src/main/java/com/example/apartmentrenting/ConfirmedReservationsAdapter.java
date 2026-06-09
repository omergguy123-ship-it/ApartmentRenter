package com.example.apartmentrenting;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ConfirmedReservationsAdapter - מתאם (Adapter) עבור RecyclerView של הזמנות מאושרות אצל המארח.
 * מוצג בלשונית "Hosting" ב-BrouseHosesOrUploadListing תחת "Confirmed Reservations".
 *
 * מציג רק הזמנות בסטטוס "APPROVED" כדי שהמארח יוכל לראות את כל האורחים המאושרים שלו.
 * בניגוד ל-BookingRequestAdapter, אין כאן כפתורי פעולה - תצוגה בלבד.
 */
public class ConfirmedReservationsAdapter extends RecyclerView.Adapter<ConfirmedReservationsAdapter.ConfirmedViewHolder> {

    // context - ה-Context של ה-Activity שמשתמשת ב-Adapter
    private Context context;
    // confirmedList - רשימת ההזמנות המאושרות של האורחים אצל המארח
    private List<Booking> confirmedList;

    public ConfirmedReservationsAdapter(Context context, List<Booking> confirmedList) {
        this.context = context;
        this.confirmedList = confirmedList;
    }

    /**
     * onCreateViewHolder - מנפח (inflate) את קובץ item_confirmed_reservation.xml ויוצר ViewHolder חדש.
     */
    @NonNull
    @Override
    public ConfirmedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_confirmed_reservation, parent, false);
        return new ConfirmedViewHolder(view);
    }

    /**
     * onBindViewHolder - מאכלס את נתוני ההזמנה המאושרת לתוך הכרטיס.
     * מציג: שם נכס, שם אורח, מחיר, תאריכים, והערת האורח (אם קיימת).
     *
     * @param holder   ה-ViewHolder שמחזיק את הרכיבים הגרפיים של כרטיס ההזמנה המאושרת
     * @param position מיקום הפריט ברשימה
     */
    @Override
    public void onBindViewHolder(@NonNull ConfirmedViewHolder holder, int position) {
        Booking booking = confirmedList.get(position);

        holder.confPropertyTitle.setText(booking.getPropertyTitle());
        holder.confRenterName.setText("Guest: " + (booking.getRenterName() != null ? booking.getRenterName() : "Guest User"));
        holder.confPrice.setText(String.format("$%.0f", booking.getPrice()));
        holder.confDatesText.setText(booking.getCheckInDate() + " - " + booking.getCheckOutDate());

        // הצגת אזור ההערה רק אם האורח כתב הערה (מסתיר את ה-View אם הערה ריקה)
        if (TextUtils.isEmpty(booking.getNote())) {
            holder.layoutConfNote.setVisibility(View.GONE);
        } else {
            holder.layoutConfNote.setVisibility(View.VISIBLE);
            holder.confNoteText.setText(booking.getNote());
        }
    }

    @Override
    public int getItemCount() {
        return confirmedList.size();
    }

    /**
     * ConfirmedViewHolder - מחלקה פנימית המחזיקה רפרנסים לרכיבי ה-UI של כרטיס ההזמנה המאושרת.
     * דפוס ViewHolder מונע קריאות חוזרות ל-findViewById ומשפר ביצועי גלילה.
     */
    public static class ConfirmedViewHolder extends RecyclerView.ViewHolder {
        TextView confPropertyTitle, confPrice, confRenterName, confDatesText, confNoteText;
        View layoutConfNote;

        public ConfirmedViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור רכיבי ה-UI מ-item_confirmed_reservation.xml
            confPropertyTitle = itemView.findViewById(R.id.confPropertyTitle);
            confPrice = itemView.findViewById(R.id.confPrice);
            confRenterName = itemView.findViewById(R.id.confRenterName);
            confDatesText = itemView.findViewById(R.id.confDatesText);
            confNoteText = itemView.findViewById(R.id.confNoteText);
            layoutConfNote = itemView.findViewById(R.id.layoutConfNote);
        }
    }
}

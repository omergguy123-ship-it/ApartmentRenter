package com.example.apartmentrenting;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * BookingRequestAdapter - מתאם (Adapter) עבור RecyclerView של בקשות הזמנה נכנסות אצל המארח.
 * מוצג בלשונית "Hosting" ב-BrouseHosesOrUploadListing תחת "Incoming Requests".
 *
 * מציג רק הזמנות בסטטוס "PENDING" (ממתינות לאישור המארח).
 * לכל בקשה יש שני כפתורות: "Accept" (אישור) ו-"Decline" (דחייה),
 * המעדכנים את שדה ה-status ב-Firestore ל-"APPROVED" או "DECLINED" בהתאמה.
 *
 * הממשק OnActionCompleted מאפשר ל-Activity לרענן את הנתונים לאחר פעולה.
 */
public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.RequestViewHolder> {

    /**
     * OnActionCompleted - ממשק (Interface) callback המופעל לאחר כל פעולת אישור/דחייה.
     * ה-Activity שמשתמשת ב-Adapter מממשת את הממשק הזה ומרעננת את הרשימות.
     */
    public interface OnActionCompleted {
        void onCompleted();
    }

    // context - ה-Context של ה-Activity לשימוש ב-Toast ו-LayoutInflater
    private Context context;
    // requestList - רשימת הבקשות הממתינות לאישור
    private List<Booking> requestList;
    // callback - ממשק ה-callback שיופעל לאחר כל פעולת אישור/דחייה
    private OnActionCompleted callback;

    public BookingRequestAdapter(Context context, List<Booking> requestList, OnActionCompleted callback) {
        this.context = context;
        this.requestList = requestList;
        this.callback = callback;
    }

    /**
     * onCreateViewHolder - מנפח (inflate) את קובץ item_booking_request.xml ויוצר ViewHolder חדש.
     */
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false);
        return new RequestViewHolder(view);
    }

    /**
     * onBindViewHolder - מאכלס את נתוני הבקשה לתוך הכרטיס.
     * מציג שם נכס, שם אורח, מחיר, תאריכים, והערת האורח.
     * מגדיר לחיצות על כפתורי Accept/Decline המעדכנים את Firestore.
     *
     * @param holder   ה-ViewHolder שמחזיק את הרכיבים הגרפיים של כרטיס הבקשה
     * @param position מיקום הפריט ברשימה
     */
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Booking request = requestList.get(position);

        holder.reqPropertyTitle.setText(request.getPropertyTitle());
        holder.reqRenterName.setText("Guest: " + (request.getRenterName() != null ? request.getRenterName() : "Guest User"));
        holder.reqPrice.setText(String.format("$%.0f", request.getPrice()));
        holder.reqDatesText.setText(request.getCheckInDate() + " - " + request.getCheckOutDate());

        // הצגת אזור ההערה רק אם האורח כתב הערה (מסתיר אם הערה ריקה)
        if (TextUtils.isEmpty(request.getNote())) {
            holder.layoutReqNote.setVisibility(View.GONE);
        } else {
            holder.layoutReqNote.setVisibility(View.VISIBLE);
            holder.reqNoteText.setText(request.getNote());
        }

        // כפתור אישור (Accept) - מעדכן את שדה ה-status ל-"APPROVED" ב-Firestore.
        // ניטרול שני הכפתורים למניעת לחיצות כפולות בזמן פעולת הרשת.
        holder.btnReqAccept.setOnClickListener(v -> {
            holder.btnReqAccept.setEnabled(false);
            holder.btnReqDecline.setEnabled(false);

            // עדכון שדה status ב-Firestore לאוסף "bookings" לפי ה-bookingId הייחודי
            FirebaseFirestore.getInstance().collection("bookings").document(request.getBookingId())
                    .update("status", "APPROVED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking Request Approved!", Toast.LENGTH_SHORT).show();
                        // הפעלת ה-callback כדי שה-Activity ירענן את רשימות הבקשות והאישורים
                        if (callback != null) callback.onCompleted();
                    })
                    .addOnFailureListener(e -> {
                        // בכשלון - מחזיר את הכפתורים לפעולה
                        holder.btnReqAccept.setEnabled(true);
                        holder.btnReqDecline.setEnabled(true);
                        Toast.makeText(context, "Approval failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // כפתור דחייה (Decline) - מעדכן את שדה ה-status ל-"DECLINED" ב-Firestore.
        holder.btnReqDecline.setOnClickListener(v -> {
            holder.btnReqAccept.setEnabled(false);
            holder.btnReqDecline.setEnabled(false);

            // עדכון שדה status ב-Firestore לאוסף "bookings" לפי ה-bookingId הייחודי
            FirebaseFirestore.getInstance().collection("bookings").document(request.getBookingId())
                    .update("status", "DECLINED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking Request Declined.", Toast.LENGTH_SHORT).show();
                        // הפעלת ה-callback כדי שה-Activity ירענן את הרשימות
                        if (callback != null) callback.onCompleted();
                    })
                    .addOnFailureListener(e -> {
                        // בכשלון - מחזיר את הכפתורים לפעולה
                        holder.btnReqAccept.setEnabled(true);
                        holder.btnReqDecline.setEnabled(true);
                        Toast.makeText(context, "Declination failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * RequestViewHolder - מחלקה פנימית המחזיקה רפרנסים לרכיבי ה-UI של כרטיס בקשת הזמנה.
     * דפוס ViewHolder מונע קריאות חוזרות ל-findViewById ומשפר ביצועי גלילה.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView reqPropertyTitle, reqPrice, reqRenterName, reqDatesText, reqNoteText;
        View layoutReqNote;
        Button btnReqDecline, btnReqAccept;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // קישור רכיבי ה-UI מ-item_booking_request.xml
            reqPropertyTitle = itemView.findViewById(R.id.reqPropertyTitle);
            reqPrice = itemView.findViewById(R.id.reqPrice);
            reqRenterName = itemView.findViewById(R.id.reqRenterName);
            reqDatesText = itemView.findViewById(R.id.reqDatesText);
            reqNoteText = itemView.findViewById(R.id.reqNoteText);
            layoutReqNote = itemView.findViewById(R.id.layoutReqNote);
            btnReqDecline = itemView.findViewById(R.id.btnReqDecline);
            btnReqAccept = itemView.findViewById(R.id.btnReqAccept);
        }
    }
}

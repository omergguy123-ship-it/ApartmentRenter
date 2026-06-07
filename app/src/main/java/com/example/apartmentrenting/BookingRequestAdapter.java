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

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.RequestViewHolder> {

    public interface OnActionCompleted {
        void onCompleted();
    }

    private Context context;
    private List<Booking> requestList;
    private OnActionCompleted callback;

    public BookingRequestAdapter(Context context, List<Booking> requestList, OnActionCompleted callback) {
        this.context = context;
        this.requestList = requestList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Booking request = requestList.get(position);

        holder.reqPropertyTitle.setText(request.getPropertyTitle());
        holder.reqRenterName.setText("Guest: " + (request.getRenterName() != null ? request.getRenterName() : "Guest User"));
        holder.reqPrice.setText(String.format("$%.0f", request.getPrice()));
        holder.reqDatesText.setText(request.getCheckInDate() + " - " + request.getCheckOutDate());

        if (TextUtils.isEmpty(request.getNote())) {
            holder.layoutReqNote.setVisibility(View.GONE);
        } else {
            holder.layoutReqNote.setVisibility(View.VISIBLE);
            holder.reqNoteText.setText(request.getNote());
        }

        // Accept Click
        holder.btnReqAccept.setOnClickListener(v -> {
            holder.btnReqAccept.setEnabled(false);
            holder.btnReqDecline.setEnabled(false);
            
            FirebaseFirestore.getInstance().collection("bookings").document(request.getBookingId())
                    .update("status", "APPROVED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking Request Approved!", Toast.LENGTH_SHORT).show();
                        if (callback != null) callback.onCompleted();
                    })
                    .addOnFailureListener(e -> {
                        holder.btnReqAccept.setEnabled(true);
                        holder.btnReqDecline.setEnabled(true);
                        Toast.makeText(context, "Approval failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Decline Click
        holder.btnReqDecline.setOnClickListener(v -> {
            holder.btnReqAccept.setEnabled(false);
            holder.btnReqDecline.setEnabled(false);
            
            FirebaseFirestore.getInstance().collection("bookings").document(request.getBookingId())
                    .update("status", "DECLINED")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Booking Request Declined.", Toast.LENGTH_SHORT).show();
                        if (callback != null) callback.onCompleted();
                    })
                    .addOnFailureListener(e -> {
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

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView reqPropertyTitle, reqPrice, reqRenterName, reqDatesText, reqNoteText;
        View layoutReqNote;
        Button btnReqDecline, btnReqAccept;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
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

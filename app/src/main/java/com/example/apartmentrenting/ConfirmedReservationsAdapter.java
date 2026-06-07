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

public class ConfirmedReservationsAdapter extends RecyclerView.Adapter<ConfirmedReservationsAdapter.ConfirmedViewHolder> {

    private Context context;
    private List<Booking> confirmedList;

    public ConfirmedReservationsAdapter(Context context, List<Booking> confirmedList) {
        this.context = context;
        this.confirmedList = confirmedList;
    }

    @NonNull
    @Override
    public ConfirmedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_confirmed_reservation, parent, false);
        return new ConfirmedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmedViewHolder holder, int position) {
        Booking booking = confirmedList.get(position);

        holder.confPropertyTitle.setText(booking.getPropertyTitle());
        holder.confRenterName.setText("Guest: " + (booking.getRenterName() != null ? booking.getRenterName() : "Guest User"));
        holder.confPrice.setText(String.format("$%.0f", booking.getPrice()));
        holder.confDatesText.setText(booking.getCheckInDate() + " - " + booking.getCheckOutDate());

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

    public static class ConfirmedViewHolder extends RecyclerView.ViewHolder {
        TextView confPropertyTitle, confPrice, confRenterName, confDatesText, confNoteText;
        View layoutConfNote;

        public ConfirmedViewHolder(@NonNull View itemView) {
            super(itemView);
            confPropertyTitle = itemView.findViewById(R.id.confPropertyTitle);
            confPrice = itemView.findViewById(R.id.confPrice);
            confRenterName = itemView.findViewById(R.id.confRenterName);
            confDatesText = itemView.findViewById(R.id.confDatesText);
            confNoteText = itemView.findViewById(R.id.confNoteText);
            layoutConfNote = itemView.findViewById(R.id.layoutConfNote);
        }
    }
}

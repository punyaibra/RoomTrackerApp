package com.example.roomtracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.model.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnBookingActionListener listener;
    private boolean isStaffOrAdmin;

    public interface OnBookingActionListener {
        void onAction(Booking booking);
    }

    public BookingAdapter(List<Booking> bookingList, boolean isStaffOrAdmin, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.isStaffOrAdmin = isStaffOrAdmin;
        this.listener = listener;
    }

    public void updateList(List<Booking> newList) {
        bookingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_table_row, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvRoom.setText(booking.getRoomName());
        holder.tvUser.setText(booking.getUserName());
        holder.tvDate.setText(booking.getDate() + "\n" + booking.getStartTime() + "-" + booking.getEndTime());
        holder.tvStatus.setText(booking.getStatus());

        // Load Room Image
        boolean isImageLoaded = false;
        String imageUri = booking.getRoomImageUri();

        if (imageUri != null && !imageUri.isEmpty()) {
            try {
                android.net.Uri uri = android.net.Uri.parse(imageUri);
                // Check if it's a file URI and if file exists
                if ("file".equals(uri.getScheme())) {
                    java.io.File file = new java.io.File(uri.getPath());
                    if (file.exists()) {
                        holder.ivRoomImage.setImageURI(uri);
                        isImageLoaded = true;
                    }
                } else {
                    holder.ivRoomImage.setImageURI(uri);
                    if (holder.ivRoomImage.getDrawable() != null) {
                        isImageLoaded = true;
                    }
                }
            } catch (Exception e) {
                isImageLoaded = false;
            }
        }

        if (isImageLoaded) {
            holder.ivRoomImage.clearColorFilter();
            holder.ivRoomImage.setPadding(0, 0, 0, 0);
            holder.ivRoomImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ivRoomImage.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            setFallbackImage(holder, booking.getRoomName());
        }

        // Color code status
        int color;
        switch (booking.getStatus()) {
            case "APPROVED":
                color = 0xFF4CAF50; // Green
                break;
            case "REJECTED":
                color = 0xFFF44336; // Red
                break;
            case "COMPLETED":
                color = 0xFF9E9E9E; // Grey
                break;
            default:
                color = 0xFF2196F3; // Blue (Pending)
                break;
        }
        holder.tvStatus.setTextColor(color);

        // Action Button Visibility
        // Show edit icon if Admin/Staff, or if it's the User's own booking (maybe
        // purely for viewing details)
        // For this table, let's say clicking the edit icon allows actions.
        if (isStaffOrAdmin) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onAction(booking));
        } else {
            // Regular users can only see details or cancel pending?
            // For now, let's allow them to click to see details
            holder.btnEdit.setImageResource(android.R.drawable.ic_menu_info_details);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onAction(booking));
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private void setFallbackImage(BookingViewHolder holder, String roomName) {
        holder.ivRoomImage.setImageResource(R.drawable.ic_nav_booking);

        // Use simpler logic for distinct colors (Material Design Colors)
        int[] colors = {
                0xFFEF5350, 0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726,
                0xFFAB47BC, 0xFF26C6DA, 0xFFFF7043, 0xFF5C6BC0
        };

        int hash = roomName != null ? Math.abs(roomName.hashCode()) : 0;
        int color = colors[hash % colors.length];

        holder.ivRoomImage.setBackgroundColor(color);
        holder.ivRoomImage.setColorFilter(android.graphics.Color.WHITE);

        // Restore padding
        int padding = (int) (12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        holder.ivRoomImage.setPadding(padding, padding, padding, padding);
        holder.ivRoomImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoom, tvUser, tvDate, tvStatus;
        ImageView btnEdit, ivRoomImage;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoom = itemView.findViewById(R.id.tvBookingRoom);
            tvUser = itemView.findViewById(R.id.tvBookingUser);
            tvDate = itemView.findViewById(R.id.tvBookingDate);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            btnEdit = itemView.findViewById(R.id.btnEditBooking);
            ivRoomImage = itemView.findViewById(R.id.ivBookingRoomImage);
        }
    }
}

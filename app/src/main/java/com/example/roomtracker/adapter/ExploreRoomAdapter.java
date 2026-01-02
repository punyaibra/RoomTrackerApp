package com.example.roomtracker.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.model.Room;
import java.util.List;

public class ExploreRoomAdapter extends RecyclerView.Adapter<ExploreRoomAdapter.ViewHolder> {

    private List<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public ExploreRoomAdapter(List<Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    public void updateList(List<Room> newRooms) {
        this.rooms = newRooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_room_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvLocation, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRoomImage);
            tvName = itemView.findViewById(R.id.tvRoomName);
            tvLocation = itemView.findViewById(R.id.tvRoomLocation);
            tvBadge = itemView.findViewById(R.id.tvAvailabilityBadge);

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onRoomClick(rooms.get(getAdapterPosition()));
            });
        }

        void bind(Room room) {
            tvName.setText(room.getName());
            tvLocation.setText(room.getBuilding() + (room.getFloor() != null ? ", Floor " + room.getFloor() : "")
                    + " • Cap: " + room.getCapacity());

            // Badge logic: Only show INACTIVE badge
            // All ACTIVE rooms show as Available (user can choose any time)
            boolean isActive = !"INACTIVE".equals(room.getStatus());

            if (!isActive) {
                tvBadge.setText("Inactive");
                tvBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E"))); // Gray
            } else {
                tvBadge.setText("Available");
                tvBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))); // Green
            }

            // Load Image
            if (room.getImageUri() != null && !room.getImageUri().isEmpty()) {
                try {
                    ivImage.setImageURI(Uri.parse(room.getImageUri()));
                } catch (Exception e) {
                    ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}

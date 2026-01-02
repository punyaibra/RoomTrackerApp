package com.example.roomtracker.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.model.Room;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomActionListener listener;
    private String userRole;

    public interface OnRoomActionListener {
        void onViewDetails(Room room);

        void onBookRoom(Room room);

        void onEditRoom(Room room);

        void onDeleteRoom(Room room);
    }

    public RoomAdapter(List<Room> roomList, String userRole, OnRoomActionListener listener) {
        this.roomList = roomList;
        this.userRole = userRole;
        this.listener = listener;
    }

    public void updateList(List<Room> newList) {
        this.roomList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_table_row, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.tvName.setText(room.getName());
        holder.tvLocation.setText(room.getLocation());

        if (room.getImageUri() != null && !room.getImageUri().isEmpty()) {
            try {
                holder.ivRoomImage.setImageURI(Uri.parse(room.getImageUri()));
            } catch (Exception e) {
                holder.ivRoomImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivRoomImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if ("INACTIVE".equals(room.getStatus())) {
            holder.tvStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // Action Icon Click (Popup Menu)
        holder.ivAction.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);

            // Menu Items based on Role (Mimicking RoomListFragment logic)
            popup.getMenu().add("View Details");

            if ("ADMIN".equals(userRole)) {
                popup.getMenu().add("Edit");
                popup.getMenu().add("Delete");
            } else if ("STAFF".equals(userRole)) {
                popup.getMenu().add("Book Room");
                popup.getMenu().add("Edit");
                popup.getMenu().add("Delete");
            } else {
                // User / Mahasiswa
                popup.getMenu().add("Book Room");
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("View Details".equals(title)) {
                    listener.onViewDetails(room);
                } else if ("Book Room".equals(title)) {
                    listener.onBookRoom(room);
                } else if ("Edit".equals(title)) {
                    listener.onEditRoom(room);
                } else if ("Delete".equals(title)) {
                    listener.onDeleteRoom(room);
                }
                return true;
            });
            popup.show();
        });

        // Row Item Click (View Detail)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(room);
            }
        });

        holder.ivAction.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvStatus;
        ImageView ivAction, ivRoomImage;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvRoomStatus);
            ivAction = itemView.findViewById(R.id.ivAction);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
        }
    }
}

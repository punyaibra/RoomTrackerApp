package com.example.roomtracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.roomtracker.R;
import com.example.roomtracker.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditProfile(User user);

        void onChangeRole(User user);

        void onDelete(User user);

        void onVerify(User user);

        void onRowClick(User user);
    }

    private String currentUserRole;

    public UserAdapter(List<User> userList, String currentUserRole, OnUserActionListener listener) {
        this.userList = userList;
        this.currentUserRole = currentUserRole;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_table_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserId.setText(String.valueOf(user.getId()));
        holder.tvUserName.setText(user.getName());
        holder.tvUserRole.setText(user.getRole());

        if ("MAHASISWA".equals(user.getRole())) {
            if (user.getIsVerified() == 1) {
                holder.tvUserStatus.setText("Aktif");
                holder.tvUserStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else if (user.getIsVerified() == 0) {
                holder.tvUserStatus.setText("Pending");
                holder.tvUserStatus.setTextColor(Color.parseColor("#FFC107")); // Amber
            } else {
                holder.tvUserStatus.setText("Rejected");
                holder.tvUserStatus.setTextColor(Color.parseColor("#F44336")); // Red
            }
        } else {
            holder.tvUserStatus.setText("Aktif");
            holder.tvUserStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        // Load Avatar (Profile Image, NOT KTM)
        String imageUri = user.getProfileImage();
        if (imageUri != null && !imageUri.isEmpty() && !"-".equals(imageUri)) {
            try {
                android.net.Uri uri;
                if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                    uri = android.net.Uri.parse(imageUri);
                } else {
                    uri = android.net.Uri.fromFile(new java.io.File(imageUri));
                }
                holder.ivUserAvatar.setPadding(0, 0, 0, 0);
                holder.ivUserAvatar.setImageURI(uri);
                holder.ivUserAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                holder.ivUserAvatar.setPadding(15, 15, 15, 15);
                holder.ivUserAvatar.setImageResource(R.drawable.ic_nav_profile);
                holder.ivUserAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        } else {
            holder.ivUserAvatar.setPadding(15, 15, 15, 15);
            holder.ivUserAvatar.setImageResource(R.drawable.ic_nav_profile);
            holder.ivUserAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // Row Click for Details
        holder.itemView.setOnClickListener(v -> listener.onRowClick(user));

        holder.btnEdit.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit Profile");

            // Add Verify option only for Mahasiswa AND not already verified (1 = Verified)
            if ("MAHASISWA".equals(user.getRole()) && user.getIsVerified() != 1) {
                popup.getMenu().add("Verify KTM");
            }

            // Hide 'Change Role' for STAFF
            if (!"STAFF".equals(currentUserRole)) {
                popup.getMenu().add("Change Role");
            }

            popup.getMenu().add("Delete User");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Edit Profile".equals(title)) {
                    listener.onEditProfile(user);
                } else if ("Change Role".equals(title)) {
                    listener.onChangeRole(user);
                } else if ("Delete User".equals(title)) {
                    listener.onDelete(user);
                } else if ("Verify KTM".equals(title)) {
                    listener.onVerify(user);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvUserName, tvUserRole, tvUserStatus;
        ImageView btnEdit, ivUserAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // cbSelect removed
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
        }
    }
}

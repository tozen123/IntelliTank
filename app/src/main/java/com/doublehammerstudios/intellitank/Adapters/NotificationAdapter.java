package com.doublehammerstudios.intellitank.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doublehammerstudios.intellitank.NotificationModel;
import com.doublehammerstudios.intellitank.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationModel> notificationList;

    public NotificationAdapter(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }
    public long getNotificationTimestamp(int position) {
        return notificationList.get(position).getTimestamp();
    }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.textViewTitle.setText(notification.getTitle());
        holder.timestampTextView.setText(formatDateTime(notification.getTimestamp()));
        holder.textViewMessage.setText(notification.getMessage());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
    private String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
        return sdf.format(new Date(timestamp));
    }
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewMessage;
        TextView timestampTextView;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

    }
}

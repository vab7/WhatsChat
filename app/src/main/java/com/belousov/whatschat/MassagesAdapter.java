package com.belousov.whatschat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MassagesAdapter extends RecyclerView.Adapter {
    ArrayList<Messages> messages;

    int ITEM_SEND = 0;
    int ITEM_RECEIVE = 1;

    public MassagesAdapter(ArrayList<Messages> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.sender_chat_layout,
                    parent,
                    false
            );
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.receiver_chat_layout,
                    parent,
                    false
            );
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messages.get(position);

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.sendMessage.setText(message.getMessage());
            viewHolder.timeMessage.setText(message.getCurrentTime());
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.sendMessage1.setText(message.getMessage());
            viewHolder.timeMessage1.setText(message.getCurrentTime());
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messages.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static final class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView sendMessage, timeMessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            sendMessage = itemView.findViewById(R.id.sender_message);
            timeMessage = itemView.findViewById(R.id.time_message);
        }
    }

    public static final class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView sendMessage1, timeMessage1;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            sendMessage1 = itemView.findViewById(R.id.sender_message1);
            timeMessage1 = itemView.findViewById(R.id.time_message1);
        }
    }
}

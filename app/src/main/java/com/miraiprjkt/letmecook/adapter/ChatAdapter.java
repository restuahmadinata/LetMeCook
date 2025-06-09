package com.miraiprjkt.letmecook.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> chatMessages;

    // Definisikan tipe view untuk pesan keluar dan masuk
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    // Tentukan tipe view berdasarkan pengirim pesan
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).isFromUser()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gunakan layout item_chat_message yang sama untuk keduanya
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final LinearLayout layout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_chat_message);
            // Kita butuh parent layout untuk mengatur gravity (posisi kanan/kiri)
            layout = (LinearLayout) itemView;
        }

        void bind(ChatMessage chatMessage) {
            messageText.setText(chatMessage.getMessage());

            if (chatMessage.isFromUser()) {
                // Pesan dari pengguna (keluar)
                // Atur background ke gelembung 'sent'
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_sent);
                // Atur posisi ke kanan (end)
                layout.setGravity(Gravity.END);
            } else {
                // Pesan dari AI (masuk)
                // Atur background ke gelembung 'received'
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_received);
                // Atur posisi ke kiri (start)
                layout.setGravity(Gravity.START);
            }
        }
    }
}
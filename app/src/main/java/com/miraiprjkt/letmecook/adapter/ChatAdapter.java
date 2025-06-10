package com.miraiprjkt.letmecook.adapter;

import android.content.Context;
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
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final Markwon markwon;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.markwon = Markwon.create(context);
    }

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view, markwon);
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
        private final Markwon markwon;

        public ChatViewHolder(@NonNull View itemView, Markwon markwon) {
            super(itemView);
            this.messageText = itemView.findViewById(R.id.text_chat_message);
            this.layout = (LinearLayout) itemView;
            this.markwon = markwon;
        }

        void bind(ChatMessage chatMessage) {
            if (chatMessage.isFromUser()) {
                messageText.setText(chatMessage.getMessage());
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_sent);
                layout.setGravity(Gravity.END);
            } else {
                markwon.setMarkdown(messageText, chatMessage.getMessage());
                messageText.setBackgroundResource(R.drawable.bg_chat_bubble_received);
                layout.setGravity(Gravity.START);
            }
        }
    }
}

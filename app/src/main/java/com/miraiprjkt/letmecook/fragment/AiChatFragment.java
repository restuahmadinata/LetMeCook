package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.miraiprjkt.letmecook.BuildConfig;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.adapter.ChatAdapter;
import com.miraiprjkt.letmecook.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AiChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private EditText editTextChatInput;
    private ImageButton buttonSendChat;
    private ProgressBar progressBarChat;

    // Variabel baru untuk adapter dan list pesan
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;

    public AiChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextChatInput = view.findViewById(R.id.edit_text_chat_input);
        buttonSendChat = view.findViewById(R.id.button_send_chat);
        progressBarChat = view.findViewById(R.id.progress_bar_chat);

        setupChat();

        buttonSendChat.setOnClickListener(v -> handleSendEvent());
    }

    private void setupChat() {
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Tambahkan pesan sambutan dari AI
        addMessageToChat("Hello! I'm your cooking assistant. Ask me anything about recipes!", false);
    }

    private void handleSendEvent() {
        String userInput = editTextChatInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            addMessageToChat(userInput, true); // Tambahkan pesan pengguna ke UI
            callGeminiApi(userInput); // Panggil API
            editTextChatInput.setText(""); // Kosongkan input
        } else {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessageToChat(String message, boolean isFromUser) {
        // Tambahkan pesan ke list
        chatMessageList.add(new ChatMessage(message, isFromUser));
        // Beri tahu adapter bahwa ada item baru
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        // Scroll ke pesan terbaru
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
    }

    private void callGeminiApi(String query) {
        setLoading(true);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(query).build();
        Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                addMessageToChat(resultText, false); // Tambahkan respons AI ke UI
                setLoading(false);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Error: Failed to get response.", Toast.LENGTH_SHORT).show();
                setLoading(false);
            }
        }, mainExecutor);
    }

    private void setLoading(boolean isLoading) {
        progressBarChat.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSendChat.setEnabled(!isLoading);
        editTextChatInput.setEnabled(!isLoading);
    }
}
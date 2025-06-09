package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
import com.miraiprjkt.letmecook.utils.ChatHistoryManager;

import java.util.List;
import java.util.concurrent.Executor;

public class AiChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private EditText editTextChatInput;
    private ImageButton buttonSendChat;
    private ProgressBar progressBarChat;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private ChatHistoryManager chatHistoryManager;

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

        chatHistoryManager = new ChatHistoryManager(requireContext());

        recyclerViewChat = view.findViewById(R.id.recycler_view_chat);
        editTextChatInput = view.findViewById(R.id.edit_text_chat_input);
        buttonSendChat = view.findViewById(R.id.button_send_chat);
        progressBarChat = view.findViewById(R.id.progress_bar_chat);

        setupChat();
        setupMenu();

        buttonSendChat.setOnClickListener(v -> handleSendEvent());
    }

    private void setupChat() {
        chatMessageList = chatHistoryManager.loadChatHistory();
        if (chatMessageList.isEmpty()) {
            chatMessageList.add(new ChatMessage("Halo! Saya asisten masakmu. Tanyakan apa saja seputar resep!", false));
        }

        chatAdapter = new ChatAdapter(requireContext(), chatMessageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Method ini bisa dikosongkan karena menu sudah di-inflate oleh MainActivity
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete_chat) {
                    showDeleteConfirmationDialog();
                    return true; // Menandakan bahwa event klik sudah ditangani
                }
                return false; // Biarkan sistem yang menangani event lain
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Chat")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat chat?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    clearChatHistory();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void clearChatHistory() {
        chatHistoryManager.clearChatHistory();
        chatMessageList.clear();
        chatMessageList.add(new ChatMessage("Halo! Saya asisten masakmu. Tanyakan apa saja seputar resep!", false));
        chatAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Riwayat chat dibersihkan", Toast.LENGTH_SHORT).show();
    }

    private void handleSendEvent() {
        String userInput = editTextChatInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            addMessageToChat(userInput, true);
            callGeminiApi(userInput);
            editTextChatInput.setText("");
        } else {
            Toast.makeText(getContext(), "Silakan tulis pesan", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMessageToChat(String message, boolean isFromUser) {
        chatMessageList.add(new ChatMessage(message, isFromUser));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessageList.size() - 1);

        // Simpan riwayat setiap ada pesan baru
        chatHistoryManager.saveChatHistory(chatMessageList);
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
                addMessageToChat(resultText, false);
                setLoading(false);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                t.printStackTrace();
                addMessageToChat("Maaf, terjadi masalah saat menyambung. Coba lagi nanti.", false);
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

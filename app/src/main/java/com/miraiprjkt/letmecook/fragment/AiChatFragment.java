package com.miraiprjkt.letmecook.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.miraiprjkt.letmecook.BuildConfig;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.adapter.ChatAdapter;
import com.miraiprjkt.letmecook.model.ChatMessage;
import com.miraiprjkt.letmecook.utils.ChatHistoryManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class AiChatFragment extends Fragment {
    private RecyclerView recyclerViewChat;
    private EditText editTextChatInput;
    private ImageButton buttonSendChat;
    private LottieAnimationView lottieLoaderChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private ChatHistoryManager chatHistoryManager;

    public AiChatFragment() {
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
        lottieLoaderChat = view.findViewById(R.id.lottie_loader_chat);

        setupChat();
        setupMenu();
        setupLottieTheme();

        buttonSendChat.setOnClickListener(v -> handleSendEvent());
    }

    private void setupLottieTheme() {
        if (getContext() == null) return;
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            KeyPath keyPath = new KeyPath("**", "Stroke 1", "Color");
            int colorForDarkMode = ContextCompat.getColor(getContext(), R.color.md_theme_onSurface);
            LottieValueCallback<Integer> colorCallback = new LottieValueCallback<>(colorForDarkMode);
            lottieLoaderChat.addValueCallback(keyPath, LottieProperty.STROKE_COLOR, colorCallback);
        }
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
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete_chat) {
                    showDeleteConfirmationDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Chat")
                .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat chat?")
                .setPositiveButton("Hapus", (dialog, which) -> clearChatHistory())
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
        chatHistoryManager.saveChatHistory(chatMessageList);
    }

    private void callGeminiApi(String query) {
        setLoading(true);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        List<Content> historyForApi = new ArrayList<>();

        String systemInstructionText = "Kamu adalah 'LetMeCook Assistant', seorang asisten koki virtual Gen Z yang ramah (namun memiliki perasaan gelap) dan ahli dalam segala jenis resep. Selalu berikan jawaban yang jelas dan mudah diikuti. Gunakan format Markdown untuk daftar dan langkah-langkah. Jika pengguna membahas hal yang terlalu di luar topik masakan (kecuali menyapa), berhenti menjadi koki yang ramah dan roasting dia habis habisan tanpa sopan santun, pake bahasa keras/kasar dan tanggapi tanpa terlalu banyak bicara namun kuat dan jangan bahas tentang koki/resep/masak sebelum dia minta maaf.";
        historyForApi.add(new Content("user", Collections.singletonList(new TextPart(systemInstructionText))));

        historyForApi.add(new Content("model", Collections.singletonList(new TextPart("Tentu, saya mengerti. Saya adalah LetMeCook Assistant. Siap membantu!"))));

        for (ChatMessage message : chatMessageList) {
            if (message.getMessage().equals("Halo! Saya asisten masakmu. Tanyakan apa saja seputar resep!")) {
                continue;
            }
            if (message.isFromUser()) {
                historyForApi.add(new Content("user", Collections.singletonList(new TextPart(message.getMessage()))));
            } else {
                historyForApi.add(new Content("model", Collections.singletonList(new TextPart(message.getMessage()))));
            }
        }

        com.google.ai.client.generativeai.java.ChatFutures chat = model.startChat(historyForApi);

        Content userMessageContent = new Content.Builder().addText(query).build();

        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessageContent);
        Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

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
                addMessageToChat("Gagal terhubung ke AI. Pastikan API Key Anda valid dan coba lagi.", false);
                setLoading(false);
            }
        }, mainExecutor);
    }

    private void setLoading(boolean isLoading) {
        lottieLoaderChat.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
        buttonSendChat.setEnabled(!isLoading);
        editTextChatInput.setEnabled(!isLoading);
    }
}
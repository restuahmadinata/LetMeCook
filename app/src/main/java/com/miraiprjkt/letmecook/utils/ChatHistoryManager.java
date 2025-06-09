package com.miraiprjkt.letmecook.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miraiprjkt.letmecook.model.ChatMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryManager {

    private static final String PREFS_NAME = "ChatHistoryPrefs";
    private static final String KEY_CHAT_HISTORY = "ChatHistory";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public ChatHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveChatHistory(List<ChatMessage> chatMessages) {
        String json = gson.toJson(chatMessages);
        sharedPreferences.edit().putString(KEY_CHAT_HISTORY, json).apply();
    }

    public List<ChatMessage> loadChatHistory() {
        String json = sharedPreferences.getString(KEY_CHAT_HISTORY, null);
        if (json == null) {
            return new ArrayList<>(); // Kembalikan list kosong jika tidak ada riwayat
        }
        Type type = new TypeToken<ArrayList<ChatMessage>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void clearChatHistory() {
        sharedPreferences.edit().remove(KEY_CHAT_HISTORY).apply();
    }
}

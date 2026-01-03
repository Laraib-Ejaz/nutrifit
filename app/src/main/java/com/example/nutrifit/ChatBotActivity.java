package com.example.nutrifit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private ImageButton btnSend;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // API Setup - Model initialize
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "AIzaSyCMrwIovDyByPo8DH-ocvC3TllympvO5Ik");
        model = GenerativeModelFutures.from(gm);

        recyclerView = findViewById(R.id.recyclerView);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        // LayoutManager setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Is se naye messages niche se shuru honge
        recyclerView.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> {
            String question = editMessage.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENT_BY_ME);
                editMessage.setText("");
                callGemini(question);
            }
        });
    }

    private void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        });
    }

    private void callGemini(String question) {
        // 1. "Typing..." add karein
        addToChat("Typing...", Message.SENT_BY_BOT);

        String systemInstruction = "You are a professional fitness and nutrition coach for the 'NutriFit' app. " +
                "Provide helpful workout and diet advice. If the user asks something unrelated to fitness, " +
                "politely say that you can only help with health-related queries.";

        Content content = new Content.Builder()
                .addText(systemInstruction + "\n\nUser Question: " + question)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    // 2. "Typing..." remove karein aur list update karein
                    if (!messageList.isEmpty()) {
                        int lastIndex = messageList.size() - 1;
                        messageList.remove(lastIndex);
                        chatAdapter.notifyItemRemoved(lastIndex); // UI ko batana ke item remove hua
                    }

                    String botMsg = result.getText();
                    if (botMsg != null && !botMsg.isEmpty()) {
                        addToChat(botMsg, Message.SENT_BY_BOT);
                    } else {
                        addToChat("I'm sorry, I couldn't process that. Please try again.", Message.SENT_BY_BOT);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    if (!messageList.isEmpty()) {
                        int lastIndex = messageList.size() - 1;
                        messageList.remove(lastIndex);
                        chatAdapter.notifyItemRemoved(lastIndex);
                    }
                    addToChat("Failure: " + t.getMessage(), Message.SENT_BY_BOT);
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
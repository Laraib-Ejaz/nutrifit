package com.example.nutrifit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private ImageButton btnSend;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private GenerativeModelFutures model;

    // Firebase variables
    private FirebaseFirestore db;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // 1. Firebase aur User Session Setup
        db = FirebaseFirestore.getInstance();
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = userSession.getString("userEmail", "default_user");

        // 2. Gemini API Setup
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "api key");
        model = GenerativeModelFutures.from(gm);

        // 3. UI Initialization
        recyclerView = findViewById(R.id.recyclerView);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // 4. Purani Chat Load Karein
        loadChatHistory();

        btnSend.setOnClickListener(v -> {
            String question = editMessage.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENT_BY_ME);
                saveMessageToFirestore(question, Message.SENT_BY_ME); // Save to Firebase
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

    private void saveMessageToFirestore(String message, String sentBy) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("message", message);
        chatData.put("sentBy", sentBy);
        chatData.put("timestamp", System.currentTimeMillis());

        db.collection("Chats").document(currentUserEmail)
                .collection("Messages").add(chatData)
                .addOnFailureListener(e -> Log.e("FirestoreError", e.getMessage()));
    }

    private void loadChatHistory() {
        db.collection("Chats").document(currentUserEmail)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messageList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String msg = doc.getString("message");
                        String sender = doc.getString("sentBy");
                        messageList.add(new Message(msg, sender));
                    }
                    chatAdapter.notifyDataSetChanged();
                    if (messageList.size() > 0) {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void callGemini(String question) {
        addToChat("Typing...", Message.SENT_BY_BOT);

        String systemInstruction = "You are the 'NutriFit' AI Coach. Rules:\n" +
                "1. No # or ## headers.\n" +
                "2. Use simple bullet points (-).\n" +
                "3. Use bold (**) for keywords.\n" +
                "4. Be professional and health-focused.";

        Content content = new Content.Builder()
                .addText(systemInstruction + "\n\nUser: " + question)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    // Typing message remove karein
                    if (!messageList.isEmpty()) {
                        messageList.remove(messageList.size() - 1);
                        chatAdapter.notifyItemRemoved(messageList.size());
                    }

                    String botMsg = result.getText();
                    if (botMsg != null && !botMsg.isEmpty()) {
                        addToChat(botMsg, Message.SENT_BY_BOT);
                        saveMessageToFirestore(botMsg, Message.SENT_BY_BOT); // Save Bot Reply
                    } else {
                        addToChat("Error processing response.", Message.SENT_BY_BOT);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    if (!messageList.isEmpty()) {
                        messageList.remove(messageList.size() - 1);
                        chatAdapter.notifyItemRemoved(messageList.size());
                    }
                    addToChat("Connection Error: " + t.getMessage(), Message.SENT_BY_BOT);
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }
}

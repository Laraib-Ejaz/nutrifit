package com.example.nutrifit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiverActivi extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "workout_reminders";

        // 1. Notification par click karne se Dashboard khulne ka rasta
        Intent clickIntent = new Intent(context, dashboard.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // PendingIntent: Jo notification click hone par execute hoga
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 2. Android Oreo (8.0) aur usse upar ke liye Channel setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("NutriFit Workout Notifications");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 3. Notification Build karna
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.walking) // Check karein ke 'walking' icon drawable mein ho
                .setContentTitle("NutriFit: Time to Sweat! 💪")
                .setContentText("Time for your workout, get ready champ!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Notification click par app khulegi
                .setAutoCancel(true)             // Click ke baad notification gayab ho jayegi
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Sound aur Vibration on karega

        // 4. Notification dikhana
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}
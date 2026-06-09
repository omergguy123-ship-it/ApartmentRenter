package com.example.apartmentrenting;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "apartment_renter_reminders";

    /**
     * פונקציית onReceive המופעלת אוטומטית על ידי מערכת האנדרואיד
     * כאשר זמן ה-Alarm שנקבע ב-AlarmManager מגיע לסיומו.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. יצירת ערוץ התראות (Notification Channel) - חובה החל מגרסת Android 8.0 (API 26)
        createNotificationChannel(context);

        // 2. הגדרת ה-Intent שייפתח כאשר המשתמש ילחץ על ההתראה בוילון ההתראות.
        // אנו מכוונים אותו לפתוח את מסך הבית הראשי של האפליקציה (BrouseHosesOrUploadListing).
        Intent clickIntent = new Intent(context, BrouseHosesOrUploadListing.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // PendingIntent עוטף את ה-Intent ומאפשר למערכת ההפעלה להפעיל אותו בשם האפליקציה שלנו כשהמשתמש ילחץ עליה.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // 3. בניית אובייקט ההתראה באמצעות NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // סמל ההתראה
                .setContentTitle("ApartmentRenter Reminder") // כותרת
                .setContentText("You have active listings and booking requests to manage! Open your Hosting Dashboard.") // תוכן
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // עדיפות
                .setContentIntent(pendingIntent) // מה קורה בלחיצה
                .setAutoCancel(true); // מעלים את ההתראה לאחר לחיצה

        // 4. שליחת ההתראה באמצעות NotificationManagerCompat
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // שליחת ההתראה בפועל עם מזהה ייחודי (1001)
            notificationManager.notify(1001, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * פונקציה ליצירת ערוץ התראות (הכרחי בגרסאות אנדרואיד 8.0 ומעלה)
     * כדי לאפשר למשתמש לשלוט בקטגוריות ההתראה השונות של האפליקציה בהגדרות המכשיר.
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking Reminders";
            String description = "Reminders to check listings and incoming guest requests";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            
            // הגדרת ערוץ ההתראה
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // רישום הערוץ במערכת ההפעלה אנדרואיד
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

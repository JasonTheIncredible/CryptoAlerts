package com.example.project5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ThreadLocalRandom;

public class Project5MessagingService extends FirebaseMessagingService {

    private static final String TAG = "Project5MessagingService";
    private static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "DEFAULT_NOTIFICATION_CHANNEL_ID";

    @Override
    public void onNewToken(@NonNull final String token) {

        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        sharedPrefs.edit().putString("token", token).apply();

        updateTokenInFirebaseFromSharedPrefs();
    }

    public void updateTokenInFirebaseFromSharedPrefs() {

        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        String token = sharedPrefs.getString("token", "empty");

        // If user has a Google account, get email one way. Else, get email another way.
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        String email;
        if (googleAccount != null) {

            email = googleAccount.getEmail();
            if (email != null && !token.equals("empty")) {

                // Firebase does not allow ".", so replace them with ",".
                String userEmailFirebase = email.replace(".", ",");
                FirebaseDatabase.getInstance().getReference().child("Users").child(userEmailFirebase).child("Token").setValue("token", token);
            }
        }
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "onMessageReceived: New incoming message.");

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String notificationId = remoteMessage.getData().get("notification_id");
        sendMessageNotification(title, message, notificationId);
    }

    /**
     * Build a push notification for a chat message
     */
    private void sendMessageNotification(String title, String message, String notificationId) {

        Log.d(TAG, "sendDmNotification: building a DM notification");

        Intent intent = new Intent(getBaseContext(), SubsPage.class);
        intent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, ThreadLocalRandom.current().nextInt(3, 100000), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID);

        // Add properties to the builder.
        builder.setContentTitle(title)
                .setAutoCancel(true)
                .setSubText(message)
                .setSmallIcon(R.mipmap.ic_logo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {

            NotificationChannel channel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}

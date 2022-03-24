package com.github.brugapp.brug.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.github.brugapp.brug.MainActivity
import com.github.brugapp.brug.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCMMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it.title!!, it.body!!)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
//         If you want to send messages to this application instance or
//         manage this apps subscriptions on the server side, send the
//         FCM registration token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageTitle: String, messageBody: String) {
        sendNotification(this, messageTitle, messageBody)
    }

//    private val view_package = "com.github.brugapp.brug"
//    private fun notificationRemoteView(messageTitle: String, messageBody: String): RemoteViews {
//        val remoteView = RemoteViews(view_package, R.layout.notification)
//        remoteView.setTextViewText(R.id.notification_title, messageTitle)
//        remoteView.setTextViewText(R.id.notification_text, messageBody)
//        remoteView.setImageViewResource(R.id.unlost_logo, R.drawable.unlost_logo)
//
//        return remoteView
//    }

    companion object {
        fun sendNotification(context: Context, messageTitle: String, messageBody: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE)

            val channelId = context.getString(R.string.default_notification_channel_id)
            val channelName = context.getString(R.string.default_notification_channel_name)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val iconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.unlost_logo)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(iconBitmap)
                .setVibrate(longArrayOf(1000))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

        }
    }

}
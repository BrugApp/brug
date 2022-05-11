package com.github.brugapp.brug.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking

class MyFCMMessagingService(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore) : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(this, it.title!!, it.body!!)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.e("NEW TOKEN NOTIF", "Refreshed token: $token")
        if(firebaseAuth.currentUser != null){
            //TODO: CHANGE THE RUNBLOCKING INTO NON-BLOCKING CALL
            runBlocking {
                UserRepository.addNewDeviceTokenToUser(firebaseAuth.currentUser!!.uid, token, firestore)
            }
        }
    }


    companion object {
        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param context application context.
         * @param messageTitle FCM message title received.
         * @param messageBody FCM message body received.
         */
        fun sendNotification(context: Context, messageTitle: String, messageBody: String) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            initChannel(channelId, channelName, notificationManager)

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

        }

        private fun initChannel(
            channelId: String,
            channelName: String,
            notificationManager: NotificationManager
        ) {
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

}
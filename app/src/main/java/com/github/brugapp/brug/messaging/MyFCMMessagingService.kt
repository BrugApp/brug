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
import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.BuildConfig.FIREBASE_NOTIFICATION_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.FirebaseResponse
import com.github.brugapp.brug.data.UserRepository.addNewDeviceTokenToUser
import com.github.brugapp.brug.ui.ChatMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MyFCMMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.data.let {
            sendNotification(applicationContext, it["title"]!!, it["body"]!!)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.e("NEW TOKEN NOTIF", "Refreshed token: $token")
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val observableResponse = MutableLiveData<FirebaseResponse>()
        if(firebaseAuth.uid != null){
            addNewDeviceTokenToUser(firebaseAuth.currentUser!!.uid, token, observableResponse, firestore)
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
            val intent = Intent(context, ChatMenuActivity::class.java)
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


        // PART HANDLING SENDING NOTIFICATION MESSAGES
        private const val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
        var mClient: OkHttpClient = OkHttpClient()

        /**
         * Sends a notification message to a list of devices.
         *
         * @param recipients the list of device tokens to target
         * @param title the title of the notification
         * @param body the content of the notification
         *
         * @return the HTTP result of the operation
         */
        fun sendNotificationMessage(
            recipients: JSONArray?,
            title: String?,
            body: String?
        ): String? {
            try {
                val root = JSONObject()
                val data = JSONObject()
                data.put("body", body)
                data.put("title", title)
                root.put("data", data)
                root.put("registration_ids", recipients)
                val result = postToFCM(root.toString())
                Log.d("NOTIFICATION RESULT", "Result: $result")
                return result
            } catch (e: Exception) {
                Log.e("NOTIFICATION ERROR", e.message.toString())
            }
            return null
        }

        @Throws(IOException::class)
        private fun postToFCM(bodyString: String?): String {
            val serverKey = FIREBASE_NOTIFICATION_KEY
            val body: RequestBody = bodyString.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request: Request = Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=$serverKey")
                .build()
            val response: Response = mClient.newCall(request).execute()
            return response.body?.string() ?: "NO RESPONSE FOUND !!"
        }
    }

}
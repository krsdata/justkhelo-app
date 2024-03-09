package justkhelo.cricks.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.R
import justkhelo.cricks.SplashScreenActivity
import justkhelo.cricks.UpdateApplicationActivity
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.HardwareInfoManager
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, token)
        val userId = MyPreferences.getUserID(applicationContext)!!
        val notid = FirebaseInstanceId.getInstance()
            .getToken(getString(R.string.gcm_default_sender_id), "FCM")

        MyPreferences.setDeviceToken(this, token)

        if (!TextUtils.isEmpty(notid) && !TextUtils.isEmpty(userId)) {

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
            jsonRequest.addProperty("device_id", token)

            WebServiceClient(applicationContext).client.create(IApiMethod::class.java)
                .deviceNotification(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {
                    }
                })
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.let { message ->
            MyUtils.logd(TAG, "From: " + remoteMessage.from)
            if (applicationContext != null) {
                if (remoteMessage.data.size > 0) {
                    Log.e(
                        TAG,
                        "Data Payload: " + remoteMessage.data.toString()
                    )
                    try {
                        val gson = Gson()
                        val resultData = gson.toJson(remoteMessage.data)
                        val json = JSONObject(resultData)
                        // handleDataMessage(json)
                        var action = ""
                        var title = ""
                        var message = ""
                        val url =
                            URL("https://cdn.britannica.com/63/211663-050-A674D74C/Jonny-Bairstow-batting-semifinal-match-England-Australia-2019.jpg")
                        var image =
                            BitmapFactory.decodeStream(url.openConnection().getInputStream())

                        if (json.has(KEY_ACTION)) {
                            action = json.getString(KEY_ACTION)
                            try {
                                if (json.has(KEY_TITLE)) {
                                    title = json.getString(KEY_TITLE)
                                }
                            } catch (e: java.lang.Exception) {
                            }

                            try {
                                if (json.has(KEY_MESSAGE)) {
                                    message = json.getString(KEY_MESSAGE)
                                }
                            } catch (e: java.lang.Exception) {
                            }

                            when (action) {
                                "notify" ->
                                    notifyUsers(applicationContext, title, message)
                                "logout" ->
                                    MyPreferences.clear(applicationContext)
                                "update" ->
                                    updateApplicationRequired(
                                        applicationContext,
                                        title,
                                        message,
                                        json.getString(KEY_UPDATE_APK),
                                        json.getString(KEY_RELEASE_NOTE)
                                    )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception: " + e.message)
                    }
                }
            }
        }
    }

    private fun notifyUsers(context: Context, title: String, message: String) {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            return
        }
        val intent: Intent = getIntentNotify()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val stackBuilder =
            TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationId = 1
            val channelId = "Ninja11"
            val channelName = "Ninja11 Notify"
            val importance = NotificationManager.IMPORTANCE_HIGH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager.createNotificationChannel(mChannel)
            }
            val mBuilder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle())
            mBuilder.setContentIntent(resultPendingIntent)
            mBuilder.setAutoCancel(true)
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            val builder =
                NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
            notificationManager.notify(
                Companion.NOTIFICATION_ID,
                builder.build()
            )
        }
    }

    private fun getIntentNotify(): Intent {
        val intent = Intent(
            applicationContext,
            SplashScreenActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun updateApplicationRequired(
        context: Context,
        title: String,
        apkUpdateUrl: String,
        releaseNote: String,
        string: String
    ) {
        val intent: Intent = getIntentUpdateActivity(apkUpdateUrl, releaseNote)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val stackBuilder =
            TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationId = 1
            val channelId = "Ninja11Update"
            val channelName = "Ninja11 Update"
            val importance = NotificationManager.IMPORTANCE_HIGH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager.createNotificationChannel(mChannel)
            }
            val mBuilder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(title)
                    .setContentText(releaseNote)
            mBuilder.setContentIntent(resultPendingIntent)
            mBuilder.setAutoCancel(true)
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            val builder =
                NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(title)
                    .setContentText(releaseNote)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
            notificationManager.notify(
                Companion.NOTIFICATION_ID,
                builder.build()
            )
        }
    }

    private fun getIntentUpdateActivity(apkUpdateUrl: String, releaseNote: String): Intent {
        val intent = Intent(
            applicationContext,
            UpdateApplicationActivity::class.java
        )
        intent.putExtra(UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE, apkUpdateUrl)
        intent.putExtra(UpdateApplicationActivity.REQUEST_RELEASE_NOTE, releaseNote)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_RELEASE_NOTE = "release_note"
        const val KEY_MESSAGE = "message"
        const val KEY_UPDATE_APK = "apk_update_url"
        const val KEY_ACTION = "action"
        val TAG: String = MyFirebaseMessagingService::class.java.simpleName
        const val NOTIFICATION_ID = 1000
    }
}
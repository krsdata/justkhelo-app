package justkhelo.cricks.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import justkhelo.cricks.R;
import justkhelo.cricks.utils.BindingUtils;

public class NotificationUtils {

    private static final String APPUPDATE_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";
    private static final int APPUPDATE_REMINDER_PENDING_INTENT_ID = 3000;
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private final Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void showNotificationMessage(final String title, final String message, Intent intent, String imageUrl) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl != null && imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                Bitmap bitmap = getBitmapFromURL(imageUrl);
                if (bitmap != null) {
                    showBigNotification(bitmap, title, message, resultPendingIntent, alarmSound);
                } else {
                    showSmallNotification(title, message, resultPendingIntent, alarmSound);
                }
            }
        } else {
            showSmallNotification(title, message, resultPendingIntent, alarmSound);
        }
    }

    private void showSmallNotification(String title, String message, PendingIntent resultPendingIntent, Uri alarmSound) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(message);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    APPUPDATE_REMINDER_NOTIFICATION_CHANNEL_ID,
                    mContext.getString(R.string.notifications_admin_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, APPUPDATE_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColorized(true)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notification_icon))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(alarmSound)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(APPUPDATE_REMINDER_PENDING_INTENT_ID, notificationBuilder.build());

    }

    private void showBigNotification(Bitmap bitmap, String title, String message, PendingIntent resultPendingIntent, Uri alarmSound) {

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    APPUPDATE_REMINDER_NOTIFICATION_CHANNEL_ID,
                    mContext.getString(R.string.notifications_admin_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, APPUPDATE_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(bigPictureStyle)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notification_icon))
                .setContentText(message);

        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(BindingUtils.NOTIFICATION_ID_BIG_IMAGE, notification);
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
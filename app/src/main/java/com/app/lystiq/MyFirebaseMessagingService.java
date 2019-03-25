package com.app.lystiq;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.app.utils.Constants;

import org.json.JSONObject;

import java.util.Random;

/**
 * Created by hitasoft on 03/11/16.
 * <p>
 * This class is to Get a FCM Messages From FCM Server and Display a Push Notification.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload=" + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                JSONObject dataJson = json.getJSONObject("data");
                String message = stripHtml(dataJson.getString(Constants.TAG_MESSAGE));
                String type = dataJson.getString(Constants.TAG_TYPE);
                generateNotification(message, type);

                Log.v(TAG, "Received message=" + remoteMessage.getData().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * For removing the html tags from the given text
     **/
    public String stripHtml(String html) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return "" + Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return "" + Html.fromHtml(html);
        }
    }

    private void generateNotification(String message, String type) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        long when = System.currentTimeMillis();
        String title = getApplicationContext().getString(R.string.app_name);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.appicon);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        Intent notificationIntent = null;
        boolean stopNotification = false;

        if (type == null || type.equals("notification") || type.equals("admin")) {
            notificationIntent = new Intent(getApplicationContext(), com.app.lystiq.Notification.class);
        }else if(type.equalsIgnoreCase("exchange")){
            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")){
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), ExchangeActivity.class);
            }
        }
        else {
            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")){
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), MessageActivity.class);
            }
        }

        if (!stopNotification) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "my channel";// The user-visible name of the channel.
            int importance = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent =
                    PendingIntent.getActivity(this, uniqueInt, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notification=mBuilder.setContentIntent(intent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.notifyicon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(when)
                        .setChannelId(CHANNEL_ID)
                        .build();


                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }else {
                notification = mBuilder.setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.notifyicon)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setWhen(when)
                        .setContentText(message)
                        .setContentIntent(intent)
                        .build();
            }

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            if (notificationManager != null) {
                notificationManager.notify(m, notification);
            }
        }
    }
}
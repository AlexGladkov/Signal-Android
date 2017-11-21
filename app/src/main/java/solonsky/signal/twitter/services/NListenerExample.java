package solonsky.signal.twitter.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.NotificationHelper;

/**
 * Created by Alex Gladkov on 14.09.17.
 * all rights reserved
 */

public class NListenerExample extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    private final int notificationId = 666;
    private int notificationNumber = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    private void createNotification(String title, String text, long smallIcon, Bitmap largeIcon, String groupKey) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setLargeIcon(largeIcon == null ?
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_app) : largeIcon);

        Flags.NotificationTypes notificationType;

        mBuilder.setGroup(groupKey);
        mBuilder.setSubText(title);
        mBuilder.setNumber(++notificationNumber);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        String subTitle = "Generic";

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        switch (String.valueOf(smallIcon)) {
            case "2130838499":
                notificationHelper.createReplyNotification(text, "", "", "", "", largeIcon);
                break;
        }

//        Log.e(TAG, "title - " + title + " group key - " + groupKey);
//
//        if (smallIcon == 2130838499) {
//            notificationType = Flags.NotificationTypes.REPLY;
//            mBuilder.setSmallIcon(R.mipmap.reply);
//            subTitle = "Replied";
//        } else if (smallIcon == 2130838501) {
//            subTitle = "Retweeted";
//            notificationType = Flags.NotificationTypes.RT;
//            mBuilder.setSmallIcon(R.mipmap.rt);
//        } else if (smallIcon == 2130838497) {
//            subTitle = "Liked";
//            notificationType = Flags.NotificationTypes.FAV;
//            mBuilder.setSmallIcon(R.mipmap.like);
//
//            Intent intentReply = new Intent();
//            PendingIntent pendingReply = PendingIntent.getBroadcast(getApplicationContext(), Flags.PENDING_REPLY,
//                    intentReply, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.addAction(R.drawable.ic_icons_actions_reply, "Reply", pendingReply);
//
//            Intent intentFollow = new Intent();
//            PendingIntent pendingFollow = PendingIntent.getBroadcast(getApplicationContext(), Flags.PENDING_FOLLOW,
//                    intentFollow, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.addAction(R.drawable.ic_badges_activity_profile, "Follow", pendingFollow);
//        } else if (smallIcon == 2130838502) {
//            notificationType = Flags.NotificationTypes.QUOTED;
//            mBuilder.setSmallIcon(R.mipmap.generic);
//        } else if (smallIcon == 2130838494) {
//            subTitle = "Direct Message";
//            notificationType = Flags.NotificationTypes.DIRECT;
//            mBuilder.setSmallIcon(R.mipmap.dm);
//        } else {
//            notificationType = Flags.NotificationTypes.UNDEFINED;
//            mBuilder.setSmallIcon(R.mipmap.generic);
//        }
//
//        mBuilder.setContentTitle(title);
//        mBuilder.setContentText(text);
//        mBuilder.setAutoCancel(true);
//
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle(Html.fromHtml("<strong>" + subTitle + "</strong>"));
//        inboxStyle.addLine(Html.fromHtml(text));
//
//        mBuilder.setStyle(inboxStyle);
//        mBuilder.setColor(getApplicationContext().getResources().getColor(R.color.support_color));
//
//        Intent resultIntent = new Intent(this, LoggedActivity.class);
//        resultIntent.putExtra(Flags.NOTIFICATION_TYPE, notificationType);
//        resultIntent.putExtra(Flags.NOTIFICATION_USERNAME, groupKey);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(LoggedActivity.class);
//
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // notificationID allows you to update the notification later on.
//        mNotificationManager.notify(getString(R.string.app_name), notificationId, mBuilder.build());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("com.twitter.android")) {
            NListenerExample.this.cancelNotification(sbn.getKey());
            Bundle bundle = sbn.getNotification().extras;

            try {
                createNotification((String) bundle.get(Notification.EXTRA_TITLE), (String) bundle.get(Notification.EXTRA_TEXT),
                                (Integer) bundle.get(Notification.EXTRA_SMALL_ICON),
                        (Bitmap) bundle.get(Notification.EXTRA_LARGE_ICON), sbn.getGroupKey());
            } catch (NullPointerException e) {
                // Do nothing
            }

            Log.e(TAG, "**********  onNotificationPosted");
            Log.e(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

            if (bundle != null) {
                Log.e(TAG, "Sub tag - " + sbn.getTag());
                Log.e(TAG, "Sort key - " + bundle.get(Notification.EXTRA_SUB_TEXT));
                Log.e(TAG, "Group key - " + sbn.getGroupKey());
                Log.e(TAG, "Extra title - " + bundle.get(Notification.EXTRA_TITLE));
                Log.e(TAG, "Extra text - " + bundle.get(Notification.EXTRA_TEXT));
                Log.e(TAG, "Extra info text - " + bundle.get(Notification.EXTRA_INFO_TEXT));
                Log.e(TAG, "Extra large icon - " + bundle.get(Notification.EXTRA_LARGE_ICON));
                Log.e(TAG, "Extra small icon - " + bundle.get(Notification.EXTRA_SMALL_ICON));
                Log.e(TAG, "Extra template - " + bundle.get(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES));
            }

//            Intent i = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
//            i.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "\n");
//            sendBroadcast(i);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Do nothing
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("command").equals("clearall")) {
                NListenerExample.this.cancelAllNotifications();
            } else if (intent.getStringExtra("command").equals("list")) {
                Intent i1 = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event", "=====================");
                sendBroadcast(i1);
                int i = 1;
                for (StatusBarNotification sbn : NListenerExample.this.getActiveNotifications()) {
                    Intent i2 = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                    i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event", "===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}


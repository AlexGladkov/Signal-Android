package solonsky.signal.twitter.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import twitter4j.AsyncTwitter;
import twitter4j.DirectMessage;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by neura on 20.10.17.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationReceiver.class.getSimpleName();

    public NotificationReceiver() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Bundle extras = intent.getExtras();
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (extras != null) {
            String sender = (String) extras.getCharSequence(AppData.KEY_NOTIFICATION_SENDER);
            String receiver = (String) extras.getCharSequence(AppData.KEY_NOTIFICATION_RECEIVER);
            String type = (String) extras.get(AppData.KEY_NOTIFICATION_TYPE);
            final int notificationId = extras.getInt(AppData.KEY_NOTIFICATION_ID);

            Log.e(TAG, "type - " + type);

            final Handler handler = new Handler();
            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();

            switch (type) {
                case "Block":
                    asyncTwitter.addListener(new TwitterAdapter() {
                        @Override
                        public void createdBlock(User user) {
                            super.createdBlock(user);
                            NotificationManager mNotificationManager = (NotificationManager)
                                    context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(context.getString(R.string.app_name), notificationId);
                        }

                        @Override
                        public void onException(TwitterException te, TwitterMethod method) {
                            super.onException(te, method);
                            Log.e(TAG, "Error " + te.getLocalizedMessage());
                        }
                    });
                    asyncTwitter.createBlock(sender);
                    break;

                case "Follow":
                    asyncTwitter.addListener(new TwitterAdapter() {
                        @Override
                        public void createdFriendship(User user) {
                            super.createdFriendship(user);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationManager mNotificationManager = (NotificationManager)
                                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.cancel(context.getString(R.string.app_name), notificationId);
                                }
                            });
                        }
                    });
                    asyncTwitter.createFriendship(sender);
                    break;

                case "DM":
                    if (remoteInput != null) {
                        String id = (String) remoteInput.getCharSequence(AppData.KEY_TEXT_REPLY);
                        asyncTwitter.addListener(new TwitterAdapter() {
                            @Override
                            public void sentDirectMessage(DirectMessage message) {
                                super.sentDirectMessage(message);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        NotificationManager mNotificationManager = (NotificationManager)
                                                context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.cancel(context.getString(R.string.app_name), notificationId);
                                    }
                                });
                            }

                            @Override
                            public void onException(TwitterException te, TwitterMethod method) {
                                super.onException(te, method);
                                Log.e(TAG, "Error " + te.getLocalizedMessage());
                            }
                        });
                        asyncTwitter.sendDirectMessage(sender, id);
                    }
                    break;
            }
        } else {
            Log.e(TAG, "extras is null");
        }
    }
}

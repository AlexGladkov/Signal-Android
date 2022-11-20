package solonsky.signal.twitter.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.services.NotificationReceiver;

/**
 * Created by neura on 19.09.17.
 */

public class NotificationHelper {
    private Context mContext;
    private static int number = 0;
    private int NOTIFICATION_ID = 237;
    private final String TAG = NotificationHelper.class.getSimpleName();

    public NotificationHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void createLikeNotification(final String text, final String sender, final String senderScreenName,
                                       final String receiver, String avatar) {
        Picasso.get().load(avatar).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    NotificationCompat.Builder mBuilder = createNotification(R.mipmap.like,
                            R.string.notification_title_like, -1, text, sender, senderScreenName, receiver,
                            Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.FAV);

                    //Provide receiver class to handle the response
                    Intent replyIntent = new Intent(mContext, LoggedActivity.class);
                    replyIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                    replyIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.REPLY);
                    replyIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                    replyIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, -1);
                    PendingIntent replyPendingIntent = PendingIntent.getActivity(mContext, 0,
                            replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Setup action item
                    NotificationCompat.Action actionReply =
                            new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                    "Reply", replyPendingIntent).build();

                    mBuilder.addAction(actionReply);

                    Intent followIntent = new Intent(mContext, NotificationReceiver.class);
                    followIntent.putExtra(AppData.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
                    followIntent.putExtra(AppData.KEY_NOTIFICATION_SENDER, senderScreenName);
                    followIntent.putExtra(AppData.KEY_NOTIFICATION_RECEIVER, receiver);
                    followIntent.putExtra(AppData.KEY_NOTIFICATION_TYPE, "Follow");
                    PendingIntent followPendingIntent = PendingIntent.getBroadcast(
                            mContext, 1, followIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Setup action item
                    NotificationCompat.Action actionFollow =
                            new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                    "Follow", followPendingIntent)
                                    .build();

                    mBuilder.addAction(actionFollow);

                    NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                } catch (Exception e) {
                    //Do nothing
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                NotificationCompat.Builder mBuilder = createNotification(R.mipmap.like,
                        R.string.notification_title_like, -1, text, sender, senderScreenName, receiver,
                        BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_app), Flags.NotificationTypes.FAV);
                NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
            }


            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //Do nothing
            }
        });
    }

    public void createRetweetNotification(final String text, final String sender, final String senderScreenName,
                                          final String receiver, String avatar, final long statusId) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.rt,
                                R.string.notification_title_rt, statusId, text, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.RT);

                        Intent replyIntent = new Intent(mContext, LoggedActivity.class);
                        replyIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        replyIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.REPLY);
                        replyIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        replyIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, statusId);
                        PendingIntent replyPendingIntent = PendingIntent.getActivity(mContext, 0,
                                replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionReply =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                        "Reply", replyPendingIntent).build();

                        mBuilder.addAction(actionReply);

                        Intent profileIntent = new Intent(mContext, LoggedActivity.class);
                        profileIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        profileIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.UNDEFINED);
                        profileIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        profileIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, statusId);
                        PendingIntent profilePendingIntent = PendingIntent.getActivity(mContext, 1,
                                profileIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionProfile =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                        "Profile", profilePendingIntent).build();

                        mBuilder.addAction(actionProfile);

                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.rt,
                                R.string.notification_title_rt, -1, text, sender, senderScreenName, receiver,
                                BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_app), Flags.NotificationTypes.RT);
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }


                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createQuoteNotification(final String text, final String sender, final String senderScreenName,
                                        final String receiver, String avatar, final long statusId) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.quote,
                                R.string.notification_title_quote, statusId, "Commented: " + text, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.QUOTED);

                        Intent replyIntent = new Intent(mContext, LoggedActivity.class);
                        replyIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        replyIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.REPLY);
                        replyIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        replyIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, -1);
                        PendingIntent replyPendingIntent = PendingIntent.getActivity(mContext, 0,
                                replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionReply =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                        "Reply", replyPendingIntent).build();

                        mBuilder.addAction(actionReply);

                        Intent profileIntent = new Intent(mContext, LoggedActivity.class);
                        profileIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        profileIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.UNDEFINED);
                        profileIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        profileIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, -1);
                        PendingIntent profilePendingIntent = PendingIntent.getActivity(mContext, 1,
                                profileIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionProfile =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                        "Profile", profilePendingIntent).build();

                        mBuilder.addAction(actionProfile);

                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        if (senderScreenName != null) {
                            NotificationCompat.Builder mBuilder = createNotification(R.mipmap.quote,
                                    R.string.notification_title_quote, -1, text, sender, senderScreenName, receiver,
                                    BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_app), Flags.NotificationTypes.QUOTED);
                            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }


                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createMentionNotification(final String text, final String sender, final String senderScreenName,
                                          final String receiver, String avatar) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.reply,
                                R.string.notification_title_mention, -1, text, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.MENTION);

                        Intent replyIntent = new Intent(mContext, LoggedActivity.class);
                        replyIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        replyIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.REPLY);
                        replyIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        replyIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, -1);
                        PendingIntent replyPendingIntent = PendingIntent.getActivity(mContext, 0,
                                replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionReply =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                        "Reply", replyPendingIntent).build();

                        mBuilder.addAction(actionReply);

                        Intent followIntent = new Intent(mContext, NotificationReceiver.class);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_SENDER, senderScreenName);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_RECEIVER, receiver);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_TYPE, "Follow");
                        PendingIntent followPendingIntent = PendingIntent.getBroadcast(
                                mContext, 1, followIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionFollow =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                        "Follow", followPendingIntent)
                                        .build();

                        mBuilder.addAction(actionFollow);

                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.reply,
                                R.string.notification_title_mention, -1, text, sender, senderScreenName, receiver,
                                BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_app), Flags.NotificationTypes.MENTION);
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }



                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createDirectNotification(final String text, final String sender, final String senderScreenName,
                                         final String receiver, String avatar) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.dm,
                                R.string.notification_title_direct, -1, text, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.DIRECT);

                        //Provide receiver class to handle the response
                        Intent dmIntent = new Intent(mContext, NotificationReceiver.class);
                        dmIntent.putExtra(AppData.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
                        dmIntent.putExtra(AppData.KEY_NOTIFICATION_SENDER, senderScreenName);
                        dmIntent.putExtra(AppData.KEY_NOTIFICATION_RECEIVER, receiver);
                        dmIntent.putExtra(AppData.KEY_NOTIFICATION_TYPE, "DM");
                        PendingIntent dmPendintIntent = PendingIntent.getBroadcast(
                                mContext, 0, dmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        RemoteInput remoteInput = new RemoteInput.Builder(AppData.KEY_TEXT_REPLY)
                                .setLabel("Respond to Message")
                                .build();

                        //Setup action item
                        NotificationCompat.Action actionReply =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                        "Message", dmPendintIntent)
                                        .addRemoteInput(remoteInput)
                                        .build();

                        mBuilder.addAction(actionReply);

                        Intent blockIntent = new Intent(mContext, NotificationReceiver.class);
                        blockIntent.putExtra(AppData.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
                        blockIntent.putExtra(AppData.KEY_NOTIFICATION_SENDER, senderScreenName);
                        blockIntent.putExtra(AppData.KEY_NOTIFICATION_RECEIVER, receiver);
                        blockIntent.putExtra(AppData.KEY_NOTIFICATION_TYPE, "Block");
                        PendingIntent blockPendingIntent = PendingIntent.getBroadcast(
                                mContext, 1, blockIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionFollow =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                        "Block", blockPendingIntent)
                                        .build();

                        mBuilder.addAction(actionFollow);

                        mBuilder.setColor(ContextCompat.getColor(mContext, R.color.support_color));
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }



                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createReplyNotification(final String text, final String sender, final String senderScreenName,
                                        final String receiver, String avatar, Bitmap bitmap) {
        if (bitmap == null) {
            Picasso.get().load(avatar)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            NotificationCompat.Builder mBuilder = createNotification(R.mipmap.reply,
                                    R.string.notification_title_reply, -1, text, sender, senderScreenName, receiver,
                                    Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.REPLY);
                            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }


                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            //Do nothing
                        }
                    });
        } else {
            NotificationCompat.Builder mBuilder = createNotification(R.mipmap.reply,
                    R.string.notification_title_reply, -1, text, sender, senderScreenName, receiver,
                    bitmap, Flags.NotificationTypes.REPLY);
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
        }
    }

    public void createFollowNotification(final String sender, final String senderScreenName, final String receiver,
                                         String avatar) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.user,
                                R.string.notification_title_follow, -1, "Followed you", sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.FOLLOW);

                        Intent replyIntent = new Intent(mContext, LoggedActivity.class);
                        replyIntent.putExtra(Flags.NOTIFICATION_ID, NOTIFICATION_ID);
                        replyIntent.putExtra(Flags.NOTIFICATION_TYPE, Flags.NotificationTypes.REPLY);
                        replyIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
                        replyIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, -1);
                        PendingIntent replyPendingIntent = PendingIntent.getActivity(mContext, 0,
                                replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionReply =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_reply,
                                        "Reply", replyPendingIntent).build();

                        mBuilder.addAction(actionReply);

                        Intent followIntent = new Intent(mContext, NotificationReceiver.class);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_SENDER, senderScreenName);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_RECEIVER, receiver);
                        followIntent.putExtra(AppData.KEY_NOTIFICATION_TYPE, "Follow");
                        PendingIntent followPendingIntent = PendingIntent.getBroadcast(
                                mContext, 1, followIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        //Setup action item
                        NotificationCompat.Action actionFollow =
                                new NotificationCompat.Action.Builder(R.drawable.ic_badges_activity_profile,
                                        "Follow Back", followPendingIntent)
                                        .build();

                        mBuilder.addAction(actionFollow);

                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }



                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createListNotification(final String listName, final String sender, final String senderScreenName, final String receiver,
                                       String avatar) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.listed,
                                R.string.notification_title_list, -1, listName, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.LISTED);
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }


                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    public void createGenericNotification(final String text, final String sender, final String senderScreenName,
                                          final String receiver, String avatar) {
        Picasso.get().load(avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        NotificationCompat.Builder mBuilder = createNotification(R.mipmap.generic,
                                R.string.notification_title_generic, -1, text, sender, senderScreenName, receiver,
                                Utilities.getCircleBitmap(bitmap.copy(bitmap.getConfig(), true)), Flags.NotificationTypes.UNDEFINED);
                        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(mContext.getString(R.string.app_name), NOTIFICATION_ID, mBuilder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }



                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        //Do nothing
                    }
                });
    }

    private NotificationCompat.Builder createNotification(int smallIcon, int contentTitle, long statusId,
                                                          String text, String sender, String senderScreenName, String receiver,
                                                          Bitmap bitmap, Flags.NotificationTypes notificationTypes) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setLargeIcon(bitmap);
        mBuilder.setSmallIcon(smallIcon);

        mBuilder.setContentTitle(sender);
        mBuilder.setContentText(text);
        mBuilder.setSubText(receiver);
        mBuilder.setGroup(mContext.getString(R.string.app_name));
        mBuilder.setAutoCancel(true);

        if (AppData.userConfiguration.isSound() &&
                AppData.userConfiguration.isVibration() && AppData.appConfiguration.isSounds()) {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else if (AppData.userConfiguration.isSound() && AppData.appConfiguration.isSounds()) {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        } else if (AppData.userConfiguration.isVibration()) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
        mBuilder.setLights(0xffff5500, 1000, 4000);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(Html.fromHtml("<strong>" +
                sender + "</strong>"));
        inboxStyle.addLine(Html.fromHtml(text));

        Intent resultIntent = new Intent(mContext, LoggedActivity.class);
        resultIntent.putExtra(Flags.NOTIFICATION_ID, -1);
        resultIntent.putExtra(Flags.NOTIFICATION_TYPE, notificationTypes);
        resultIntent.putExtra(Flags.NOTIFICATION_USERNAME, senderScreenName);
        resultIntent.putExtra(Flags.NOTIFICATION_STATUS_ID, statusId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(LoggedActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                new Random().nextInt(10000), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setStyle(inboxStyle);
        mBuilder.setColor(mContext.getResources().getColor(R.color.support_color));
        NOTIFICATION_ID = NOTIFICATION_ID + 1;
        return mBuilder;
    }
}

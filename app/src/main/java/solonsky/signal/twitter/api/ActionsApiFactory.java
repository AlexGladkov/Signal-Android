package solonsky.signal.twitter.api;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 18.09.17.
 */

public class ActionsApiFactory {
    private static final String TAG = ActionsApiFactory.class.getSimpleName();

    public static void translate(String query, Activity mActivity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setPackage("com.google.android.apps.translate");

        Uri uri = new Uri.Builder()
                .scheme("http")
                .authority("translate.google.com")
                .path("/m/translate")
                .appendQueryParameter("q", query)
//                .appendQueryParameter("tl", "pl") // target language
//                .appendQueryParameter("sl", "fr") // source language
                .build();

        intent.setData(uri);

        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity.getApplicationContext(),
                    mActivity.getString(R.string.error_no_translate), Toast.LENGTH_SHORT).show();
        }
    }

    public static void follow(String screenName, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error follow - " + te.getLocalizedMessage());
            }

            @Override
            public void createdFriendship(twitter4j.User user) {
                super.createdFriendship(user);
                User followed = User.getFromUserInstance(user);
                if (!UsersData.getInstance().getFollowingList().contains(user.getId()))
                    UsersData.getInstance().getFollowingList().add(user.getId());
                if (!UsersData.getInstance().getUsersList().contains(followed))
                    UsersData.getInstance().getUsersList().add(0, followed);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.save);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_followed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.createFriendship(screenName);
    }

    public static void follow(long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error follow - " + te.getLocalizedMessage());
            }

            @Override
            public void createdFriendship(twitter4j.User user) {
                super.createdFriendship(user);
                User followed = User.getFromUserInstance(user);
                if (!UsersData.getInstance().getFollowingList().contains(user.getId()))
                    UsersData.getInstance().getFollowingList().add(user.getId());
                if (!UsersData.getInstance().getUsersList().contains(followed))
                    UsersData.getInstance().getUsersList().add(0, followed);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.save);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_followed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.createFriendship(userId);
    }

    public static void unfollow(final String screenName, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error unfollow - " + te.getLocalizedMessage());
            }

            @Override
            public void destroyedFriendship(twitter4j.User user) {
                super.destroyedFriendship(user);
                UsersData.getInstance().getFollowingList().remove(user.getId());
                for (User list : UsersData.getInstance().getUsersList()) {
                    if (TextUtils.equals(list.getScreenName().toLowerCase(), screenName.toLowerCase())) {
                        UsersData.getInstance().getUsersList().remove(list);
                        break;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.mute);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_unfollowed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.destroyFriendship(screenName);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FeedData.getInstance().getFeedStatuses().size() > 0) {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (!TextUtils.equals(statusModel.getUser().getScreenName().toLowerCase(), screenName.toLowerCase())) {
                            temp.add(statusModel);
                            hasUpdate = true;
                        }
                    }

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        FeedData.getInstance().getFeedStatuses().removeAll(temp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void unfollow(final long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error unfollow - " + te.getLocalizedMessage());
            }

            @Override
            public void destroyedFriendship(twitter4j.User user) {
                super.destroyedFriendship(user);
                UsersData.getInstance().getFollowingList().remove(user.getId());
                for (User list : UsersData.getInstance().getUsersList()) {
                    if (list.getId() == userId) {
                        UsersData.getInstance().getUsersList().remove(list);
                        break;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.mute);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_unfollowed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.destroyFriendship(userId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FeedData.getInstance().getFeedStatuses().size() > 0) {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (statusModel.getUser().getId() == userId) {
                            temp.add(statusModel);
                            hasUpdate = true;
                        }
                    }

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        FeedData.getInstance().getFeedStatuses().removeAll(temp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void mute(final String screenName, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error mute - " + te.getLocalizedMessage());
            }

            @Override
            public void createdMute(twitter4j.User user) {
                super.createdMute(user);
                RemoveModel removeModel = new RemoveModel(user.getId(), "@" + user.getScreenName());

                if (!MuteData.getInstance().getmUsersList().contains(removeModel)) {
                    MuteData.getInstance().getmUsersList().add(0, new RemoveModel(user.getId(), "@" + user.getScreenName()));
                    MuteData.getInstance().saveCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.mute);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        asyncTwitter.createMute(screenName);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FeedData.getInstance().getFeedStatuses().size() > 0) {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (!TextUtils.equals(statusModel.getUser().getScreenName().toLowerCase(), screenName.toLowerCase())) {
                            temp.add(statusModel);
                            hasUpdate = true;
                        }
                    }

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        FeedData.getInstance().getFeedStatuses().removeAll(temp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void mute(final long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error mute - " + te.getLocalizedMessage());
            }

            @Override
            public void createdMute(twitter4j.User user) {
                super.createdMute(user);
                RemoveModel removeModel = new RemoveModel(user.getId(), "@" + user.getScreenName());

                if (!MuteData.getInstance().getmUsersList().contains(removeModel)) {
                    MuteData.getInstance().getmUsersList().add(0, new RemoveModel(user.getId(), "@" + user.getScreenName()));
                    MuteData.getInstance().saveCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.mute);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        asyncTwitter.createMute(userId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FeedData.getInstance().getFeedStatuses().size() > 0) {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (statusModel.getUser().getId() == userId) {
                            temp.add(statusModel);
                            hasUpdate = true;
                        }
                    }

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        FeedData.getInstance().getFeedStatuses().removeAll(temp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void unmute(final String screenName, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error unmute - " + te.getLocalizedMessage());
            }

            @Override
            public void destroyedMute(twitter4j.User user) {
                super.destroyedMute(user);
                RemoveModel removeModel = new RemoveModel(user.getId(), "@" + user.getScreenName());

                if (MuteData.getInstance().getmUsersList().contains(removeModel)) {
                    MuteData.getInstance().getmUsersList().remove(removeModel);
                    MuteData.getInstance().saveCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, context.getString(R.string.success_unmute), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.destroyMute(screenName);
    }

    public static void unmute(final long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error unmute - " + te.getLocalizedMessage());
            }

            @Override
            public void destroyedMute(twitter4j.User user) {
                super.destroyedMute(user);
                RemoveModel removeModel = new RemoveModel(user.getId(), "@" + user.getScreenName());

                if (MuteData.getInstance().getmUsersList().contains(removeModel)) {
                    MuteData.getInstance().getmUsersList().remove(removeModel);
                    MuteData.getInstance().saveCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, context.getString(R.string.success_unmute), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.destroyMute(userId);
    }

    public static void block(final long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error block - " + te.getLocalizedMessage());
            }

            @Override
            public void createdBlock(twitter4j.User user) {
                super.createdBlock(user);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, context.getString(R.string.success_blocked), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.createBlock(userId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FeedData.getInstance().getFeedStatuses().size() > 0) {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (statusModel.getUser().getId() == userId) {
                            temp.add(statusModel);
                            hasUpdate = true;
                        }
                    }

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        FeedData.getInstance().getFeedStatuses().removeAll(temp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void unblock(final long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error unblock - " + te.getLocalizedMessage());
            }

            @Override
            public void destroyedBlock(final twitter4j.User user) {
                super.destroyedBlock(user);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        UsersData.getInstance().getBlockList().remove(user.getId());
                        Toast.makeText(context, context.getString(R.string.success_unblocked), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        asyncTwitter.destroyBlock(userId);
    }

    public static void report(long userId, final Context context) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error reporting - " + te.getLocalizedMessage());
            }

            @Override
            public void reportedSpam(twitter4j.User reportedSpammer) {
                super.reportedSpam(reportedSpammer);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, context.getString(R.string.success_blocked), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        asyncTwitter.reportSpam(userId);
    }

    public static void disableRetweet(final String screenName, final Context context) {
        final Handler handler = new Handler();
        if (FeedData.getInstance().getFeedStatuses().size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (statusModel.isRetweet() &&
                                !TextUtils.equals(statusModel.getUser().getScreenName().toLowerCase(), screenName.toLowerCase())) {
                            hasUpdate = true;
                            temp.add(statusModel);
                        }
                    }

                    FeedData.getInstance().getFeedStatuses().removeAll(temp);

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }).start();
        }

        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void lookedupUsers(ResponseList<twitter4j.User> users) {
                super.lookedupUsers(users);
                if (!MuteData.getInstance().getmRetweetsIds().contains(users.get(0).getId())) {
                    MuteData.getInstance().getmRetweetsIds().add(users.get(0).getId());
                    MuteData.getInstance().saveCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (MuteData.getInstance().isCacheLoaded()) {
                            Toast.makeText(context, context.getString(R.string.success_disabled), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        asyncTwitter.lookupUsers(screenName);
    }

    public static void disableRetweet(final long userId, Context applicationContext) {
        if (FeedData.getInstance().getFeedStatuses().size() > 0) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean hasUpdate = false;
                    ArrayList<StatusModel> temp = new ArrayList<>();
                    for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                        if (statusModel.isRetweet() && statusModel.getUser().getId() == userId) {
                            hasUpdate = true;
                            temp.add(statusModel);
                        }
                    }

                    FeedData.getInstance().getFeedStatuses().removeAll(temp);

                    if (hasUpdate && FeedData.getInstance().getUpdateHandler() != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                FeedData.getInstance().saveCache(TAG);
                            }
                        });
                    }
                }
            }).start();
        }

        if (MuteData.getInstance().isCacheLoaded()) {
            if (!MuteData.getInstance().getmRetweetsIds().contains(userId)) {
                MuteData.getInstance().getmRetweetsIds().add(userId);
                MuteData.getInstance().saveCache();
            }
        }

        Toast.makeText(applicationContext, applicationContext.getString(R.string.success_disabled), Toast.LENGTH_SHORT).show();
    }

    public static void enableRetweet(long userId, Context applicationContext) {
        if (MuteData.getInstance().isCacheLoaded()) {
            if (MuteData.getInstance().getmRetweetsIds().contains(userId)) {
                MuteData.getInstance().getmRetweetsIds().remove(userId);
                MuteData.getInstance().saveCache();
            }
            Toast.makeText(applicationContext, applicationContext.getString(R.string.success_enabled), Toast.LENGTH_SHORT).show();
        }
    }

    public static void enableRetweet(String screenName, final Context applicationContext) {
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void lookedupUsers(ResponseList<twitter4j.User> users) {
                super.lookedupUsers(users);

                if (MuteData.getInstance().isCacheLoaded()) {
                    if (MuteData.getInstance().getmRetweetsIds().contains(users.get(0).getId())) {
                        MuteData.getInstance().getmRetweetsIds().remove(users.get(0).getId());
                        MuteData.getInstance().saveCache();
                    }
                }
            }
        });

        asyncTwitter.lookupUsers(screenName);
        Toast.makeText(applicationContext, applicationContext.getString(R.string.success_enabled), Toast.LENGTH_SHORT).show();
    }
}

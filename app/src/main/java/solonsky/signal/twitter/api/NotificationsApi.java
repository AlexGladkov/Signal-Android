package solonsky.signal.twitter.api;

import android.os.Handler;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

import solonsky.signal.twitter.data.NotificationsAllData;
import solonsky.signal.twitter.data.NotificationsFollowData;
import solonsky.signal.twitter.data.NotificationsLikeData;
import solonsky.signal.twitter.data.NotificationsReplyData;
import solonsky.signal.twitter.data.NotificationsRetweetData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.models.NotificationModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 14.09.17.
 */

public class NotificationsApi {
    private static final String TAG = NotificationsApi.class.getSimpleName();

    public static void initData() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                    NotificationsAllData.getInstance().loadCache();
                }

                if (NotificationsReplyData.getInstance().getReplyList().size() == 0) {
                    NotificationsReplyData.getInstance().loadCache();
                }

                if (NotificationsRetweetData.getInstance().getRetweetList().size() == 0) {
                    NotificationsRetweetData.getInstance().loadCache();
                }

                if (NotificationsLikeData.getInstance().getLikesList().size() == 0) {
                    NotificationsLikeData.getInstance().loadCache();
                }

                if (NotificationsFollowData.getInstance().getFollowList().size() == 0) {
                    NotificationsFollowData.getInstance().loadCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        NotificationsLikeData.getInstance().getUpdateHandler().onUpdate();
                        NotificationsFollowData.getInstance().getUpdateHandler().onUpdate();
                        loadApi();
                    }
                });
            }
        }).start();
    }

    private static void loadApi() {
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        final boolean fromStart = NotificationsAllData.getInstance().getDataList().size() == 0;
        asyncTwitter.addListener(new TwitterAdapter() {
            int retweetsCount = 0;
            int retweetsSize = 0;

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error loading notifications " + te.getLocalizedMessage());
                if (method.equals(TwitterMethod.RETWEETS)) {
                    try {
                        NotificationsRetweetData.getInstance().saveCache();
                    } catch (ConcurrentModificationException e) {
                        Log.e(TAG, "Not this time Luke");
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotificationsRetweetData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                }
            }

            @Override
            public void gotRetweets(ResponseList<Status> retweets) {
                super.gotRetweets(retweets);
                for (Status status : retweets) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    final User convertedUser = User.getFromUserInstance(status.getUser());

                    NotificationDetailModel notificationDetailModel = NotificationDetailModel.getRetweetInstance(convertedUser.getId(),
                            convertedUser.getName(), "@" + convertedUser.getScreenName(), convertedUser.getOriginalProfileImageURL(),
                            convertedUser.getDescription(),
                            UsersData.getInstance().getFollowingList().contains(convertedUser.getId()) || convertedUser.getId() == AppData.ME.getId(),
                            !fromStart);

                    NotificationModel notificationModel = NotificationModel.getRetweetInstance(status.getId(),
                            convertedUser.getName(), status.getRetweetedStatus().getText(), convertedUser.getOriginalProfileImageURL(),
                            new LocalDateTime(status.getCreatedAt()), !fromStart);

                    notificationDetailModel.setUser(convertedUser);
                    notificationModel.setStatusModel(statusModel);

                    if (!NotificationsRetweetData.getInstance().getRetweetList().contains(notificationDetailModel))
                        NotificationsRetweetData.getInstance().getRetweetList().add(notificationDetailModel);
                    if (!NotificationsAllData.getInstance().getDataList().contains(notificationModel)) {
                        NotificationsAllData.getInstance().getDataList().add(notificationModel);
                        if (!fromStart)
                            NotificationsAllData.getInstance().incEntryCount();
                    }

                    Collections.sort(NotificationsAllData.getInstance().getDataList(), new Comparator<NotificationModel>() {
                        @Override
                        public int compare(NotificationModel o1, NotificationModel o2) {
                            return o2.getDateTime().compareTo(o1.getDateTime());
                        }
                    });

                    try {
                        NotificationsAllData.getInstance().saveCache(TAG);
                        NotificationsRetweetData.getInstance().saveCache();
                    } catch (ConcurrentModificationException e) {
                        Log.e(TAG, "Not this time Luke");
                    }
                }

                retweetsCount = retweetsCount + 1;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (retweetsSize == retweetsCount) {
                            NotificationsRetweetData.getInstance().getUpdateHandler().onUpdate();
                            NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                        }
                    }
                });
            }

            @Override
            public void gotRetweetsOfMe(ResponseList<Status> statuses) {
                super.gotRetweetsOfMe(statuses);
                retweetsSize = statuses.size();
                for (Status status : statuses) {
                    asyncTwitter.getRetweets(status.getId());
                }
            }

            @Override
            public void gotMentions(ResponseList<Status> statuses) {
                super.gotMentions(statuses);
                for (Status status : statuses) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    final User convertedUser = User.getFromUserInstance(status.getUser());

                    NotificationModel notificationModel;
                    if (status.getInReplyToStatusId() > -1) {
                        notificationModel = NotificationModel.getReplyInstance(
                                status.getId(), convertedUser.getName(), status.getText(),
                                convertedUser.getOriginalProfileImageURL(), new LocalDateTime(status.getCreatedAt()), !fromStart);
                    } else {
                        notificationModel = NotificationModel.getMentionInstance(
                                status.getId(), convertedUser.getName(), status.getText(),
                                convertedUser.getOriginalProfileImageURL(), new LocalDateTime(status.getCreatedAt()), !fromStart);
                    }

                    notificationModel.setStatusModel(statusModel);

                    if (!NotificationsAllData.getInstance().getDataList().contains(notificationModel)) {
                        NotificationsAllData.getInstance().getDataList().add(notificationModel);
                        if (!fromStart)
                            NotificationsAllData.getInstance().incEntryCount();
                    }

                    NotificationDetailModel notificationDetailModel;
                    if (status.getInReplyToStatusId() > -1) {
                        notificationDetailModel = NotificationDetailModel.getReplyInstance(convertedUser.getId(),
                                convertedUser.getName(), "@" + convertedUser.getScreenName(), convertedUser.getOriginalProfileImageURL(),
                                convertedUser.getDescription(), UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), !fromStart);
                    } else {
                        notificationDetailModel = NotificationDetailModel.getMentionedInstance(convertedUser.getId(),
                                convertedUser.getName(), "@" + convertedUser.getScreenName(), convertedUser.getOriginalProfileImageURL(),
                                convertedUser.getDescription(), UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), !fromStart);
                    }

                    notificationDetailModel.setUser(convertedUser);

                    if (!NotificationsReplyData.getInstance().getReplyList().contains(notificationDetailModel)) {
                        NotificationsReplyData.getInstance().getReplyList().add(notificationDetailModel);
                    }

                }

                try {
                    NotificationsAllData.getInstance().saveCache(TAG);
                    NotificationsReplyData.getInstance().saveCache();
                } catch (ConcurrentModificationException e) {
                    Log.e(TAG, "Not this time Luke");
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                        NotificationsReplyData.getInstance().getUpdateHandler().onUpdate();
                    }
                });
            }
        });

        asyncTwitter.getMentions(new Paging(1, 40));
        asyncTwitter.getRetweetsOfMe(new Paging(1, 10));
    }
}

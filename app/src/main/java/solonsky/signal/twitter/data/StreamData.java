package solonsky.signal.twitter.data;

import android.os.Handler;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.helpers.Validators;
import solonsky.signal.twitter.interfaces.NotificationListener;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.DirectMessage;
import solonsky.signal.twitter.models.DirectModel;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.models.NotificationModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.URLEntity;
import twitter4j.UserList;
import twitter4j.UserStreamAdapter;

/**
 * Created by neura on 10.09.17.
 */

public class StreamData {
    private static final String TAG = StreamData.class.getSimpleName();
    private static volatile StreamData instance;

    public static StreamData getInstance() {
        StreamData localInstance = instance;
        if (localInstance == null) {
            synchronized (StreamData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new StreamData();
                }
            }
        }
        return localInstance;
    }

    private TwitterStream twitterStream;
    private NotificationListener notificationListener;
    private boolean isStarted;
    private Handler handler;
    private Gson gson;

    private StreamData() {
        this.handler = new Handler();
        this.gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        this.notificationListener = null;
        this.twitterStream = Utilities.getTwitterStream();
        this.isStarted = false;
        this.twitterStream.addListener(new UserStreamAdapter() {
            @Override
            public void onStatus(final Status status) {
                super.onStatus(status);
                StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                statusModel.tuneModel(status);
                statusModel.linkClarify();

                User convertedUser = User.getFromUserInstance(status.getUser());

                if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotificationsAllData.getInstance().loadCache();
                        }
                    });
                }

                if (NotificationsRetweetData.getInstance().getRetweetList().size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotificationsRetweetData.getInstance().loadCache();
                        }
                    });
                }

                if (NotificationsReplyData.getInstance().getReplyList().size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NotificationsReplyData.getInstance().loadCache();
                        }
                    });
                }

                if (FeedData.getInstance().getFeedStatuses().size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            FeedData.getInstance().loadCache();
                        }
                    });
                }

                if (status.getInReplyToStatusId() > -1 && status.getInReplyToUserId() == AppData.ME.getId()) {
                    NotificationModel replyListModel = NotificationModel.getReplyInstance(status.getId(),
                            convertedUser.getName(), statusModel.getText(),
                            convertedUser.getOriginalProfileImageURL(), new LocalDateTime(), false);

                    replyListModel.setStatusModel(statusModel);

                    if (status.getUser().getId() != AppData.ME.getId()) {
                        NotificationsAllData.getInstance().getDataList().add(0, replyListModel);
                        NotificationsAllData.getInstance().saveCache(TAG);
                        MentionsData.Companion.getInstance().addItemToNew(statusModel, true);
                        MentionsData.Companion.getInstance().saveCache(TAG);

                        LoggedData.getInstance().setNewMention(true);
                    }

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsReplyData.getInstance().getReplyList())) {
                        NotificationDetailModel replyModel = NotificationDetailModel.getReplyInstance(
                                convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                convertedUser.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                        replyModel.setUser(convertedUser);
                        NotificationsReplyData.getInstance().getReplyList().add(0, replyModel);
                        NotificationsReplyData.getInstance().saveCache();
                    }

                    statusModel.setHighlighted(true);
                    FeedData.getInstance().addItemToNew(statusModel, true);
                    FeedData.getInstance().saveCache("Stream data reply");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationListener != null && (status.getUser().getId() != AppData.ME.getId()))
                                notificationListener.onCreateReplyNotification(status.getText(),
                                        status.getUser().getName(), status.getUser().getScreenName(),
                                        "@" + status.getInReplyToScreenName(),
                                        status.getUser().getOriginalProfileImageURL());

                            if (NotificationsReplyData.getInstance().getUpdateHandler() != null)
                                NotificationsReplyData.getInstance().getUpdateHandler().onUpdate();
                            if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            if (FeedData.getInstance().getUpdateHandler() != null)
                                FeedData.getInstance().getUpdateHandler().onAdd();
                            if (MentionsData.Companion.getInstance().getUpdateHandler() != null) {
                                MentionsData.Companion.getInstance().getUpdateHandler().onAdd();
                            }

                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                    return;
                }

                if (status.isRetweet()) {
                    if (status.getUser().getId() == AppData.ME.getId()) {
                        LoggedData.getInstance().setNewFeed(true);
                        FeedData.getInstance().addItemToNew(statusModel, true);
                        FeedData.getInstance().saveCache("Stream data regular");

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (FeedData.getInstance().getUpdateHandler() != null)
                                    FeedData.getInstance().getUpdateHandler().onAdd();
                                LoggedData.getInstance().getUpdateHandler().onUpdate();
                            }
                        });
                        return;
                    } else {
                        if (status.getRetweetedStatus().getUser().getId() == AppData.ME.getId()) {
                            NotificationModel retweetListModel = NotificationModel.getRetweetInstance(status.getId(),
                                    convertedUser.getName(), statusModel.getRetweetedStatus().getText(),
                                    convertedUser.getOriginalProfileImageURL(), new LocalDateTime(), true);

                            retweetListModel.setUser(convertedUser);
                            retweetListModel.setStatusModel(statusModel.getRetweetedStatus());

                            NotificationsAllData.getInstance().getDataList().add(0, retweetListModel);
                            NotificationsAllData.getInstance().incEntryCount();
                            NotificationsAllData.getInstance().saveCache(TAG);
                            LoggedData.getInstance().setNewActivity(true);

                            if (!Validators.hasDetail(status.getId(), NotificationsRetweetData.getInstance().getRetweetList())) {
                                NotificationDetailModel retweetModel = NotificationDetailModel.getRetweetInstance(
                                        convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                        convertedUser.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                        UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                                retweetListModel.setUser(convertedUser);
                                NotificationsRetweetData.getInstance().getRetweetList().add(0, retweetModel);
                                NotificationsRetweetData.getInstance().saveCache();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (notificationListener != null && AppData.userConfiguration.isRetweets())
                                        notificationListener.onCreateRetweetNotification(
                                                status.getRetweetedStatus().getText(),
                                                status.getUser().getName(), status.getUser().getScreenName(),
                                                "@" + status.getRetweetedStatus().getUser().getScreenName(),
                                                status.getUser().getOriginalProfileImageURL(), status.getId());

                                    if (NotificationsRetweetData.getInstance().getUpdateHandler() != null)
                                        NotificationsRetweetData.getInstance().getUpdateHandler().onUpdate();
                                    if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                        NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                                    LoggedData.getInstance().getUpdateHandler().onUpdate();
                                }
                            });
                        }
                    }
                }

                if (status.getText().toLowerCase().contains(AppData.ME.getScreenName().toLowerCase()) && !status.isRetweet()) {
                    NotificationModel mentionListModel = NotificationModel.getMentionInstance(status.getId(),
                            convertedUser.getName(), statusModel.getText(),
                            convertedUser.getOriginalProfileImageURL(), new LocalDateTime(), false);

                    mentionListModel.setStatusModel(statusModel);

                    if (status.getUser().getId() != AppData.ME.getId()) {
                        NotificationsAllData.getInstance().getDataList().add(0, mentionListModel);
                        NotificationsAllData.getInstance().saveCache(TAG);
                        MentionsData.Companion.getInstance().addItemToNew(statusModel, true);
                        MentionsData.Companion.getInstance().saveCache(TAG);

                        LoggedData.getInstance().setNewMention(true);
                    }

                    statusModel.setHighlighted(true);
                    FeedData.getInstance().addItemToNew(statusModel, true);
                    FeedData.getInstance().saveCache("Stream data mention");

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsReplyData.getInstance().getReplyList())) {
                        NotificationDetailModel mentionModel = NotificationDetailModel.getMentionedInstance(
                                convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                convertedUser.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                        mentionModel.setUser(convertedUser);
                        NotificationsReplyData.getInstance().getReplyList().add(0, mentionModel);
                        NotificationsReplyData.getInstance().saveCache();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            boolean isMention = (AppData.userConfiguration.getMentions() ==
                                    ConfigurationUserModel.Mentions.FROM_ALL) ||
                                    (AppData.userConfiguration.getMentions() == ConfigurationUserModel.Mentions.FROM_FOLLOW
                                            && UsersData.getInstance().getFollowingList().contains(status.getUser().getId()));

                            if (notificationListener != null && isMention && (status.getUser().getId() != AppData.ME.getId())) {
                                notificationListener.onCreateMentionNotification(status.getText(),
                                        status.getUser().getName(), status.getUser().getScreenName(),
                                        "@" + AppData.ME.getScreenName(),
                                        status.getUser().getOriginalProfileImageURL());
                            }

                            if (NotificationsReplyData.getInstance().getUpdateHandler() != null)
                                NotificationsReplyData.getInstance().getUpdateHandler().onUpdate();
                            if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            if (FeedData.getInstance().getUpdateHandler() != null)
                                FeedData.getInstance().getUpdateHandler().onAdd();
                            if (MentionsData.Companion.getInstance().getUpdateHandler() != null) {
                                MentionsData.Companion.getInstance().getUpdateHandler().onAdd();
                            }

                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });

                    return;
                }

                FeedData.getInstance().addItemToNew(statusModel, true);
                FeedData.getInstance().saveCache("Stream data regular");
                LoggedData.getInstance().setNewFeed(true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (FeedData.getInstance().getUpdateHandler() != null)
                            FeedData.getInstance().getUpdateHandler().onAdd();
                        LoggedData.getInstance().getUpdateHandler().onUpdate();
                    }
                });
            }

            @Override
            public void onUserListMemberAddition(final twitter4j.User addedMember, final twitter4j.User listOwner, final UserList list) {
                super.onUserListMemberAddition(addedMember, listOwner, list);
                if (addedMember.getId() == AppData.ME.getId()) {
                    User convertedUser = User.getFromUserInstance(listOwner);

                    if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsAllData.getInstance().loadCache();
                            }
                        });
                    }

                    if (NotificationsFollowData.getInstance().getFollowList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsFollowData.getInstance().loadCache();
                            }
                        });
                    }

                    NotificationModel followListModel = NotificationModel.getListInstance(convertedUser.getId(),
                            convertedUser.getName(), "@" + convertedUser.getScreenName(),
                            listOwner.getOriginalProfileImageURL(), new LocalDateTime(), true);

                    followListModel.setUser(convertedUser);
                    NotificationsAllData.getInstance().getDataList().add(0, followListModel);
                    NotificationsAllData.getInstance().incEntryCount();
                    NotificationsAllData.getInstance().saveCache(TAG);

                    LoggedData.getInstance().setNewActivity(true);

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsFollowData.getInstance().getFollowList())) {
                        NotificationDetailModel followModel = NotificationDetailModel.getListedInstance(
                                convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                listOwner.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                        followModel.setUser(convertedUser);
                        NotificationsFollowData.getInstance().getFollowList().add(0, followModel);
                        NotificationsFollowData.getInstance().saveCache();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationListener != null)
                                notificationListener.onCreateListedNotification(list.getFullName(),
                                        listOwner.getName(), listOwner.getScreenName(), "@" + addedMember.getScreenName(),
                                        listOwner.getOriginalProfileImageURL());

                            if (NotificationsFollowData.getInstance().getUpdateHandler() != null)
                                NotificationsFollowData.getInstance().getUpdateHandler().onUpdate();
                            if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                }
            }

            @Override
            public void onFollow(final twitter4j.User source, final twitter4j.User followedUser) {
                super.onFollow(source, followedUser);
                if (source.getId() != AppData.ME.getId()) {
                    User convertedUser = User.getFromUserInstance(source);

                    if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsAllData.getInstance().loadCache();
                            }
                        });
                    }

                    if (NotificationsFollowData.getInstance().getFollowList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsFollowData.getInstance().loadCache();
                            }
                        });
                    }

                    NotificationModel followListModel = NotificationModel.getFollowInstance(convertedUser.getId(),
                            convertedUser.getName(), "@" + convertedUser.getScreenName(),
                            source.getOriginalProfileImageURL(), new LocalDateTime(), true);

                    followListModel.setUser(convertedUser);
                    NotificationsAllData.getInstance().getDataList().add(0, followListModel);
                    NotificationsAllData.getInstance().incEntryCount();
                    NotificationsAllData.getInstance().saveCache(TAG);

                    LoggedData.getInstance().setNewActivity(true);

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsFollowData.getInstance().getFollowList())) {
                        NotificationDetailModel followModel = NotificationDetailModel.getFollowInstance(
                                convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                source.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                        followModel.setUser(convertedUser);
                        NotificationsFollowData.getInstance().getFollowList().add(0, followModel);
                        NotificationsFollowData.getInstance().saveCache();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationListener != null && App.getInstance().ismIsBackground())
                                notificationListener.onCreateFollowNotification(source.getName(),
                                        source.getScreenName(), "@" + followedUser.getScreenName(),
                                        source.getOriginalProfileImageURL());

                            if (NotificationsFollowData.getInstance().getUpdateHandler() != null)
                                NotificationsFollowData.getInstance().getUpdateHandler().onUpdate();
                            if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            if (LoggedData.getInstance().getUpdateHandler() != null)
                                LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                }
            }

            @Override
            public void onQuotedTweet(final twitter4j.User source, final twitter4j.User target, final Status quotingTweet) {
                super.onQuotedTweet(source, target, quotingTweet);
                if (source.getId() != AppData.ME.getId()) {
                    final StatusModel statusModel = gson.fromJson(gson.toJsonTree(quotingTweet), StatusModel.class);
                    statusModel.tuneModel(quotingTweet);
                    statusModel.linkClarify();

                    User convertedUser = User.getFromUserInstance(source);

                    if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsAllData.getInstance().loadCache();
                            }
                        });
                    }

                    if (NotificationsRetweetData.getInstance().getRetweetList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsRetweetData.getInstance().loadCache();
                            }
                        });
                    }

                    NotificationModel quoteListModel = NotificationModel.getQuoteInstance(statusModel.getId(),
                            source.getName(), quotingTweet.getText(),
                            source.getOriginalProfileImageURL(), new LocalDateTime(), true);

                    quoteListModel.setStatusModel(statusModel);
                    NotificationsAllData.getInstance().getDataList().add(0, quoteListModel);
                    NotificationsAllData.getInstance().incEntryCount();
                    NotificationsAllData.getInstance().saveCache(TAG);

                    LoggedData.getInstance().setNewActivity(true);

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsRetweetData.getInstance().getRetweetList())) {
                        NotificationDetailModel quoteModel = NotificationDetailModel.getQuotedInstance(
                                convertedUser.getId(), source.getName(), "@" + source.getScreenName(),
                                source.getOriginalProfileImageURL(), source.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(source.getId()), true);

                        quoteModel.setUser(convertedUser);
                        NotificationsRetweetData.getInstance().getRetweetList().add(0, quoteModel);
                        NotificationsRetweetData.getInstance().saveCache();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationListener != null && AppData.userConfiguration.isQuotes()) {
                                String text = quotingTweet.getText();
                                for (URLEntity urlEntity : quotingTweet.getURLEntities()) {
                                    text = text.replace(urlEntity.getURL(), "");
                                }
                                notificationListener.onCreateQuoteNotification(
                                        text, source.getName(), source.getScreenName(),
                                        "@" + target.getScreenName(), source.getOriginalProfileImageURL(),
                                        statusModel.getId());
                            }

                            if (NotificationsRetweetData.getInstance().getUpdateHandler() != null)
                                NotificationsRetweetData.getInstance().getUpdateHandler().onUpdate();
                            if (NotificationsAllData.getInstance().getUpdateHandler() != null)
                                NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            if (LoggedData.getInstance().getUpdateHandler() != null)
                                LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                }
            }

            @Override
            public void onDirectMessage(final twitter4j.DirectMessage directMessage) {
                super.onDirectMessage(directMessage);
                final DirectMessage convertedMessage = gson.fromJson(gson.toJsonTree(directMessage),
                        DirectMessage.class);
                convertedMessage.getRecipient().setBiggerProfileImageURL(directMessage.getRecipient().getBiggerProfileImageURL());
                convertedMessage.getRecipient().setOriginalProfileImageURL(directMessage.getRecipient().getOriginalProfileImageURL());
                convertedMessage.getSender().setBiggerProfileImageURL(directMessage.getSender().getBiggerProfileImageURL());
                convertedMessage.getSender().setOriginalProfileImageURL(directMessage.getSender().getOriginalProfileImageURL());

                long id = AppData.ME.getId() == convertedMessage.getSenderId() ?
                        convertedMessage.getRecipientId() : convertedMessage.getSenderId();
                solonsky.signal.twitter.models.User user = AppData.ME.getId() == convertedMessage.getSenderId() ?
                        convertedMessage.getRecipient() : convertedMessage.getSender();
                int entryCount = 0;
                for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                    if (directModel.getOtherId() == convertedMessage.getSenderId()
                            || directModel.getOtherId() == convertedMessage.getRecipientId()) {
                        entryCount = directModel.getMessageCount();
                        DirectData.getInstance().getmMessagesList().remove(
                                DirectData.getInstance().getmMessagesList().indexOf(directModel));
                        break;
                    }
                }

                DirectModel directModel = new DirectModel(
                        convertedMessage.getId(), id, user.getOriginalProfileImageURL(),
                        user.getName(), convertedMessage.getText(), new LocalDateTime(convertedMessage.getCreatedAt()), false);
                directModel.setMessageCount(entryCount + 1);
                DirectData.getInstance().getmMessagesList().add(0, directModel);
                LoggedData.getInstance().setNewMessage(true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (directMessage.getSenderId() != AppData.ME.getId()
                                && notificationListener != null && AppData.userConfiguration.isMessages()) {
                            notificationListener.onCreateDirectNotitifcation(
                                    directMessage.getText(),
                                    directMessage.getSender().getName(), directMessage.getSenderScreenName(),
                                    "@" + directMessage.getRecipient().getScreenName(),
                                    directMessage.getSender().getOriginalProfileImageURL());
                        }

                        DirectApi.getInstance().addNewItem(directMessage);
                        if (DirectData.getInstance().getUpdateHandler() != null)
                            DirectData.getInstance().getUpdateHandler().onUpdate();
                        if (LoggedData.getInstance().getUpdateHandler() != null)
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                    }
                });
            }

            @Override
            public void onFavorite(final twitter4j.User source, final twitter4j.User target, final Status favoritedStatus) {
                super.onFavorite(source, target, favoritedStatus);
                if (target.getId() == AppData.ME.getId()) {
                    User convertedUser = User.getFromUserInstance(source);
                    if (NotificationsLikeData.getInstance().getLikesList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsLikeData.getInstance().loadCache();
                            }
                        });
                    }

                    if (NotificationsAllData.getInstance().getDataList().size() == 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsAllData.getInstance().loadCache();
                            }
                        });
                    }

                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(favoritedStatus), StatusModel.class);
                    statusModel.tuneModel(favoritedStatus);
                    statusModel.linkClarify();

                    NotificationModel likeListModel = NotificationModel.getLikedInstance(convertedUser.getId(),
                            convertedUser.getName(), statusModel.getText(),
                            source.getOriginalProfileImageURL(), new LocalDateTime(), true);

                    likeListModel.setUser(convertedUser);
                    likeListModel.setStatusModel(statusModel);
                    NotificationsAllData.getInstance().getDataList().add(0, likeListModel);
                    NotificationsAllData.getInstance().incEntryCount();
                    NotificationsAllData.getInstance().saveCache(TAG);

                    LoggedData.getInstance().setNewActivity(true);

                    if (!Validators.hasDetail(convertedUser.getId(), NotificationsLikeData.getInstance().getLikesList())) {
                        NotificationDetailModel likeModel = NotificationDetailModel.getLikeInstance(
                                convertedUser.getId(), convertedUser.getName(), "@" + convertedUser.getScreenName(),
                                source.getOriginalProfileImageURL(), convertedUser.getDescription(),
                                UsersData.getInstance().getFollowingList().contains(convertedUser.getId()), true);

                        likeModel.setUser(convertedUser);
                        NotificationsLikeData.getInstance().getLikesList().add(0, likeModel);
                        NotificationsLikeData.getInstance().saveCache();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationListener != null && AppData.userConfiguration.isLikes()) {
                                Log.e(TAG, "listener called");
                                notificationListener.onCreateLikeNotification(favoritedStatus.getText(),
                                        source.getName(), source.getScreenName(), "@" + target.getScreenName(),
                                        source.getOriginalProfileImageURL());
                            }

                            NotificationsLikeData.getInstance().getUpdateHandler().onUpdate();
                            NotificationsAllData.getInstance().getUpdateHandler().onUpdate();
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    });
                } else {
                    if (!Validators.hasStatusModel(favoritedStatus.getId(), LikesData.getInstance().getLikesStatuses())) {
                        StatusModel statusModel = gson.fromJson(gson.toJsonTree(favoritedStatus), StatusModel.class);
                        statusModel.tuneModel(favoritedStatus);
                        statusModel.linkClarify();
                        statusModel.setFavorited(true);

                        if (LikesData.getInstance().getLikesStatuses().size() == 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LikesData.getInstance().loadCache();
                                }
                            });
                        }

                        LikesData.getInstance().getLikesStatuses().add(0, statusModel);
                        LikesData.getInstance().saveCache();
                        LoggedData.getInstance().setNewFavorite(true);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LikesData.getInstance().getUpdateHandler().onUpdate();
                                LoggedData.getInstance().getUpdateHandler().onUpdate();
                            }
                        });
                    }
                }
            }
        });
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void startStream() {
        if (!isStarted) {
            twitterStream.user();
            isStarted = true;
        }
    }

    public void endStream() {
        if (isStarted) {
            twitterStream.cleanUp();
            isStarted = false;
        }
    }

    public NotificationListener getNotificationListener() {
        return notificationListener;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    public static void setInstance(StreamData instance) {
        StreamData.instance = instance;
    }
}

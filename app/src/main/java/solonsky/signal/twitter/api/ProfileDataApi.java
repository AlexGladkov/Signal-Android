package solonsky.signal.twitter.api;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.android.exoplayer.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.models.StatusModel;
import twitter4j.AsyncTwitter;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 15.09.17.
 */

public class ProfileDataApi {
    private static final String TAG = ProfileDataApi.class.getSimpleName();
    private static volatile ProfileDataApi instance;
    private final Gson gson;

    public static ProfileDataApi getInstance() {
        ProfileDataApi localInstance = instance;
        if (localInstance == null) {
            synchronized (ProfileDataApi.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ProfileDataApi();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<StatusModel> tweets;
    private ArrayList<StatusModel> likes;
    private ArrayList<ImageModel> images;
    private UpdateHandler tweetHandler;
    private UpdateHandler likesHandler;
    private UpdateHandler mediaHandler;
    private String screenName;
    private final int MAX_STATUSES = 20;
    private final int MAX_MEDIA = 16;
    private boolean isLoadingTweets;
    private boolean isLoadingFavorites;
    private boolean isLoadingMedia;

    private ProfileDataApi() {
        this.gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        this.isLoadingTweets = false;
        this.isLoadingFavorites = false;
        this.isLoadingMedia = false;
        clear();
    }

    long maxId = 0;

    public void loadData() {
        if (TextUtils.isEmpty(screenName)) return;
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                isLoadingTweets = false;
                isLoadingFavorites = false;
                isLoadingMedia = false;
            }

            @Override
            public void gotUserTimeline(ResponseList<Status> statuses) {
                super.gotUserTimeline(statuses);
                tweets.clear();

                for (Status status : statuses) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    if (!tweets.contains(statusModel) && tweets.size() < MAX_STATUSES) {
                        tweets.add(statusModel);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tweetHandler != null)
                            tweetHandler.onUpdate();
                        isLoadingTweets = false;
                    }
                });
            }

            @Override
            public void gotFavorites(ResponseList<Status> statuses) {
                super.gotFavorites(statuses);
                likes.clear();

                for (Status status : statuses) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    if (!likes.contains(statusModel)) {
                        likes.add(statusModel);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (likesHandler != null)
                            likesHandler.onUpdate();
                        isLoadingFavorites = false;
                    }
                });
            }
        });

        if (!isLoadingTweets) {
            isLoadingTweets = true;
            asyncTwitter.getUserTimeline(screenName, new Paging(1, MAX_STATUSES * 2));
        }

        if (!isLoadingFavorites) {
            isLoadingFavorites = true;
            asyncTwitter.getFavorites(screenName, new Paging(1, MAX_STATUSES));
        }

        if (!isLoadingMedia) {
            isLoadingMedia = true;
            final AsyncTwitter mediaTwitter = Utilities.getAsyncTwitterMediaOnly();
            mediaTwitter.addListener(new TwitterAdapter() {
                @Override
                public void onException(TwitterException te, TwitterMethod method) {
                    super.onException(te, method);
                    Log.e(TAG, "Failure load " + method.name() + " reason " + te.getLocalizedMessage());
                    isLoadingMedia = false;
                }

                @Override
                public void gotUserTimeline(ResponseList<Status> statuses) {
                    super.gotUserTimeline(statuses);
                    for (Status status : statuses) {
                        for (twitter4j.MediaEntity mediaEntity : status.getMediaEntities()) {
                            images.add(new ImageModel(mediaEntity.getMediaURL()));
                        }
                    }

                    maxId = statuses.size() > 0 ? statuses.get(statuses.size() - 1).getId() : maxId;

                    if (images.size() < MAX_MEDIA) {
                        Paging paging = new Paging(1, 50);
                        paging.setMaxId(maxId);
                        mediaTwitter.getUserTimeline(screenName, paging);
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaHandler != null)
                                    mediaHandler.onUpdate();
                                isLoadingMedia = false;
                            }
                        });
                    }
                }
            });

            Paging paging = new Paging(1, 50);
            mediaTwitter.getUserTimeline(screenName, paging);
        }
    }

    public void clear() {
        this.tweets = new ArrayList<>();
        this.likes = new ArrayList<>();
        this.images = new ArrayList<>();
        this.screenName = "";
        this.likesHandler = null;
        this.tweetHandler = null;
        this.mediaHandler = null;
    }

    public ArrayList<StatusModel> getTweets() {
        return tweets;
    }

    public ArrayList<StatusModel> getLikes() {
        return likes;
    }

    public ArrayList<ImageModel> getImages() {
        return images;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public UpdateHandler getTweetHandler() {
        return tweetHandler;
    }

    public void setTweetHandler(UpdateHandler tweetHandler) {
        this.tweetHandler = tweetHandler;
    }

    public UpdateHandler getLikesHandler() {
        return likesHandler;
    }

    public void setLikesHandler(UpdateHandler likesHandler) {
        this.likesHandler = likesHandler;
    }

    public UpdateHandler getMediaHandler() {
        return mediaHandler;
    }

    public void setMediaHandler(UpdateHandler mediaHandler) {
        this.mediaHandler = mediaHandler;
    }
}

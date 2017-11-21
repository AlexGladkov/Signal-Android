package solonsky.signal.twitter.data;

import android.os.Handler;
import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.activities.ChatSelectActivity;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.activities.MuteAddActivity;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.User;
import twitter4j.AsyncTwitter;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 12.09.17.
 */

public class UsersData {
    private static final String TAG = UsersData.class.getSimpleName();
    private static volatile UsersData instance;

    public static UsersData getInstance() {
        UsersData localInstance = instance;
        if (localInstance == null) {
            synchronized (UsersData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UsersData();
                }
            }
        }
        return localInstance;
    }

    private UsersData() {
        this.usersList = new ArrayList<>();
        this.blockList = new ArrayList<>();
        this.followersList = new ArrayList<>();
        this.followingList = new ArrayList<>();
    }

    public void clear() {
        this.usersList.clear();
        this.followersList.clear();
        this.followingList.clear();
        this.blockList.clear();
    }

    public void init() {
        if (usersList.size() == 0) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Type resultType = new TypeToken<List<User>>() {
                    }.getType();
                    Type followersType = new TypeToken<List<Long>>() {
                    }.getType();

                    Reservoir.getAsync(Cache.Followers + String.valueOf(AppData.ME.getId()), followersType,
                            new ReservoirGetCallback<List<Long>>() {
                                @Override
                                public void onSuccess(List<Long> longs) {
                                    followersList.addAll(longs);
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });

                    Reservoir.getAsync(Cache.Block + String.valueOf(AppData.ME.getId()), followersType,
                            new ReservoirGetCallback<List<Long>>() {
                                @Override
                                public void onSuccess(List<Long> longs) {
                                    blockList.addAll(longs);
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });

                    Reservoir.getAsync(Cache.Friends + String.valueOf(AppData.ME.getId()), followersType,
                            new ReservoirGetCallback<List<Long>>() {
                                @Override
                                public void onSuccess(List<Long> longs) {
                                    followingList.addAll(longs);
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });

                    Reservoir.getAsync(Cache.Users + String.valueOf(AppData.ME.getId()), resultType,
                            new ReservoirGetCallback<List<User>>() {
                        @Override
                        public void onSuccess(List<User> users) {
                            usersList.addAll(users);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadUsers();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error loading cache from users " + e.getLocalizedMessage());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadUsers();
                                }
                            });
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * Load users from server for further using in parts of app
     *
     * @see ComposeActivity - using in mentions section
     * @see MuteAddActivity - using in user section
     * @see ChatSelectActivity - using in search section
     */
    private void loadUsers() {
        final ArrayList<Long> userIds = new ArrayList<>();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error loading users " + te.getLocalizedMessage());
            }

            @Override
            public void lookedupUsers(ResponseList<twitter4j.User> users) {
                super.lookedupUsers(users);
                for (twitter4j.User user : users) {
                    User userModel = User.getFromUserInstance(user);
                    usersList.add(userModel);
                }

                saveUsersList();
            }

            @Override
            public void gotFriendsIDs(IDs ids) {
                super.gotFriendsIDs(ids);
                for (long id : ids.getIDs()) {
                    userIds.add(id);
                    if (!followingList.contains(id)) followingList.add(id);
                }

                saveFollowingList();

                if (ids.getNextCursor() == 0) {
                    asyncTwitter.getFollowersIDs(-1);
                } else {
                    asyncTwitter.getFriendsIDs(ids.getNextCursor());
                }
            }

            @Override
            public void gotBlockIDs(IDs blockingUsersIDs) {
                super.gotBlockIDs(blockingUsersIDs);
                for (long id : blockingUsersIDs.getIDs()) {
                    blockList.add(id);
                }

                saveBlockList();

                if (blockingUsersIDs.hasNext()) {
                    asyncTwitter.getBlocksIDs(blockingUsersIDs.getNextCursor());
                }
            }

            @Override
            public void gotFollowersIDs(IDs ids) {
                super.gotFollowersIDs(ids);
                for (long id : ids.getIDs()) {
                    userIds.add(id);
                    if (!followersList.contains(id)) followersList.add(id);
                }

                saveFollowersList();

                if (ids.getNextCursor() == 0) {
                    long[] array = new long[userIds.size()];
                    int length = array.length > USERS_MAX_VALUE ? USERS_MAX_VALUE : array.length;
                    for (int i = 0; i < length; i++) {
                        array[i] = userIds.get(i);
                    }

                    asyncTwitter.lookupUsers(array);
                } else {
                    asyncTwitter.getFollowersIDs(ids.getNextCursor());
                }
            }
        });
        asyncTwitter.getFriendsIDs(-1);
        asyncTwitter.getBlocksIDs(-1);
    }

    private List<User> usersList;
    private List<Long> followingList;
    private List<Long> followersList;
    private List<Long> blockList;
    private int USERS_MAX_VALUE = 100;

    public void saveBlockList() {
        try {
            Reservoir.put(Cache.Block + String.valueOf(AppData.ME.getId()), usersList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving users " + e.getLocalizedMessage());
        }
    }

    public void saveUsersList() {
        try {
            Reservoir.put(Cache.Users + String.valueOf(AppData.ME.getId()), usersList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving users " + e.getLocalizedMessage());
        }
    }

    public void saveFollowingList() {
        try {
            Reservoir.put(Cache.Friends + String.valueOf(AppData.ME.getId()), followingList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving friends " + e.getLocalizedMessage());
        }
    }

    public void saveFollowersList() {
        try {
            Reservoir.put(Cache.Followers + String.valueOf(AppData.ME.getId()), followersList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving followers " + e.getLocalizedMessage());
        }
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }

    public List<Long> getFollowingList() {
        return followingList;
    }

    public void setFollowingList(List<Long> followingList) {
        this.followingList = followingList;
    }

    public List<Long> getFollowersList() {
        return followersList;
    }

    public void setFollowersList(List<Long> followersList) {
        this.followersList = followersList;
    }

    public static void setInstance(UsersData instance) {
        UsersData.instance = instance;
    }

    public List<Long> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<Long> blockList) {
        this.blockList = blockList;
    }
}

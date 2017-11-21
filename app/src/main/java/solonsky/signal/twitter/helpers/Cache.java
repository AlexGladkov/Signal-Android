package solonsky.signal.twitter.helpers;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.models.SearchModel;

/**
 * Created by neura on 30.07.17.
 */

public class Cache {
    private static final String TAG = Cache.class.getSimpleName();
    public static final String UsersConfigurations = "UsersConfigurationsCache";
    public static final String Me = "MeCache";
    public static final String Feed = "FeedCache";
    public static final String Logged = "Logged";
    public static final String Shares = "SharesCache";
    public static final String Mentions = "MentionsCache";
    public static final String Mute = "MuteCache";
    public static final String Likes = "LikesCache";
    public static final String Directs = "DirectsCache";
    public static final String DirectsList = "DirectsListCache";
    public static final String NotificationsAll = "NotificationsAllCache";
    public static final String NotificationsLike = "NotificationsLikeCache";
    public static final String NotificationsFollow = "NotificationsFollowCache";
    public static final String NotificationsRetweet = "NotificationsRetweetCache";
    public static final String NotificationsReply = "NotificationsReplyCache";
    public static final String RecentSearch = "RecentSearchCache";
    public static final String Users = "UsersCache";
    public static final String Friends = "FriendsCache";
    public static final String Followers = "FollowersCache";
    public static final String SavedSearch = "SavedSearch";
    public static final String Block = "BlockCache";

    public static final ArrayList<String> trends = new ArrayList<>();
    public static final String HomeTimeline = "HomeTimelineCache";

    public static void saveRecentSearch(final String searchQuery) {
        Type resultType = new TypeToken<List<SearchModel>>() {}.getType();
        Reservoir.getAsync(RecentSearch + String.valueOf(AppData.ME.getId()), resultType,
                new ReservoirGetCallback<List<SearchModel>>() {
            @Override public void onFailure(Exception e) {
                ArrayList<SearchModel> searchModels = new ArrayList<>();
                searchModels.add(new SearchModel(0, 0, searchQuery, false));
                try {
                    Reservoir.put(RecentSearch + String.valueOf(AppData.ME.getId()), searchModels);
                } catch (IOException te) {
                    Log.e(TAG, "Error create recent " + te.getLocalizedMessage());
                }
            }

            @Override public void onSuccess(List<SearchModel> searchModels) {
                boolean hasSearch = false;

                for (SearchModel searchModel : searchModels) {
                    if (searchModel.getTitle().toLowerCase().equals(searchQuery.toLowerCase())) {
                        hasSearch = true;
                    }
                }

                if (!hasSearch) searchModels.add(new SearchModel(0, 0, searchQuery, false));
                try {
                    Reservoir.put(RecentSearch + String.valueOf(AppData.ME.getId()), searchModels);
                } catch (IOException e) {
                    Log.e(TAG, "Error create recent " + e.getLocalizedMessage());
                }
            }
        });
    }
}

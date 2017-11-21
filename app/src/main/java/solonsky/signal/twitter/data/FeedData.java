package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.interfaces.UpdateAddHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 09.09.17.
 */

public class FeedData {
    private static final String TAG = FeedData.class.getSimpleName();
    private static volatile FeedData instance;

    public static FeedData getInstance() {
        FeedData localInstance = instance;
        if (localInstance == null) {
            synchronized (FeedData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FeedData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<StatusModel> feedStatuses;
    private ArrayList<StatusModel> newStatuses;
    private UpdateAddHandler updateHandler;
    private final int MAX_FEED = 250;
    private long topId = 0;
    private int entryCount;
    private int cacheEntryCount;
    private int scrollPosition;
    private int scrollTop;

    private FeedData() {
        this.feedStatuses = new ArrayList<>();
        this.newStatuses = new ArrayList<>();
        this.topId = 0;
        this.entryCount = 0;
        this.cacheEntryCount = 0;
        this.scrollPosition = 0;
        this.scrollTop = 0;
        this.updateHandler = null;
    }

    public void addItemToNew(StatusModel statusModel, boolean isChangePosition) {
        if (!Utilities.validateTweet(statusModel)) return;
        statusModel.setNewStatus(isChangePosition);

        if (!newStatuses.contains(statusModel))
            newStatuses.add(0, statusModel);

        if (isChangePosition) {
            entryCount = cacheEntryCount + newStatuses.size();
        }
    }

    public void loadCache() {
        try {
            FeedData feedData = Reservoir.get(Cache.Feed + String.valueOf(AppData.ME.getId()), FeedData.class);
            entryCount = feedData.getEntryCount();

            for (StatusModel statusModel : feedData.getFeedStatuses()) {
                if (!Utilities.validateTweet(statusModel)) continue;
                feedStatuses.add(statusModel);
            }

            setScrollTop(feedData.getScrollTop());
            setScrollPosition(feedData.getScrollPosition());

            if (feedData.getFeedStatuses().size() > 0)
                topId = feedData.getFeedStatuses().get(0).getId();
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Failure loading feed " + e.getLocalizedMessage());
        }
    }

    public void saveCache(String source) {
        if (FeedData.getInstance().getFeedStatuses().size() > 0) {
            try {
                Reservoir.put(Cache.Feed + String.valueOf(AppData.ME.getId()), this);
            } catch (IOException | NullPointerException | ConcurrentModificationException e) {
                Log.e(TAG, "Error caching feed " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Trying to save empty array - " + source);
        }
    }

    public void clear() {
        this.feedStatuses.clear();
        this.newStatuses.clear();
        this.entryCount = 0;
        this.scrollPosition = 0;
        this.scrollTop = 0;
        this.updateHandler = null;
    }

    public void setTopId(long topId) {
        this.topId = topId;
    }

    public long getTopId() {
        return topId;
    }

    public ArrayList<StatusModel> getFeedStatuses() {
        return feedStatuses;
    }

    public void setFeedStatuses(ArrayList<StatusModel> feedStatuses) {
        this.feedStatuses = feedStatuses;
    }

    public UpdateAddHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(UpdateAddHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public int getScrollTop() {
        return scrollTop;
    }

    public void setScrollTop(int scrollTop) {
        this.scrollTop = scrollTop;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public void incEntryCount() {
        this.entryCount = entryCount + 1;
    }

    public void decEntryCount() {
        this.entryCount = entryCount - 1;
    }

    public static void setInstance(FeedData instance) {
        FeedData.instance = instance;
    }

    public int getCacheEntryCount() {
        return cacheEntryCount;
    }

    public ArrayList<StatusModel> getNewStatuses() {
        return newStatuses;
    }

    public void setCacheEntryCount(int cacheEntryCount) {
        this.cacheEntryCount = cacheEntryCount;
    }

    public void updateEntryCount() {
        this.entryCount = cacheEntryCount + newStatuses.size();
    }

    public void addNewItems() {
        Collections.reverse(newStatuses);
        for (StatusModel statusModel : newStatuses) {
            if (!feedStatuses.contains(statusModel))
                this.feedStatuses.add(0, statusModel);
        }

        this.newStatuses.clear();
    }

    public void clearNew() {
        for (StatusModel statusModel : feedStatuses) {
            statusModel.setNewStatus(false);
        }
    }
}

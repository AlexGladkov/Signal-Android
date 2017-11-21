package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.models.RemoveModel;

/**
 * Created by neura on 10.09.17.
 */

public class MuteData {
    private static final String TAG = MuteData.class.getSimpleName();
    private static volatile MuteData instance;

    public static MuteData getInstance() {
        MuteData localInstance = instance;
        if (localInstance == null) {
            synchronized (MuteData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new MuteData();
                }
            }
        }
        return localInstance;
    }

    private boolean isCacheLoaded;
    private ArrayList<RemoveModel> mUsersList;
    private ArrayList<RemoveModel> mKeywordsList;
    private ArrayList<RemoveModel> mHashtagsList;
    private ArrayList<RemoveModel> mClientsList;
    private ArrayList<Long> mRetweetsIds;

    private MuteData() {
        isCacheLoaded = false;
        mUsersList = new ArrayList<>();
        mKeywordsList = new ArrayList<>();
        mHashtagsList = new ArrayList<>();
        mClientsList = new ArrayList<>();
        mRetweetsIds = new ArrayList<>();
    }

    public void clear() {
        this.mUsersList.clear();
        this.mKeywordsList.clear();
        this.mHashtagsList.clear();
        this.mClientsList.clear();
        this.mRetweetsIds.clear();
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.Mute + String.valueOf(AppData.ME.getId()), this);
        } catch (IOException | NullPointerException | ConcurrentModificationException e) {
            Log.e(TAG, "Error saving mutes - " + e.getLocalizedMessage());
        }
    }

    public void loadCache() {
        try {
            MuteData muteData = Reservoir.get(Cache.Mute + String.valueOf(AppData.ME.getId()), MuteData.class);
            this.mUsersList.addAll(muteData.getmUsersList());
            this.mClientsList.addAll(muteData.getmClientsList());
            this.mHashtagsList.addAll(muteData.getmHashtagsList());
            this.mKeywordsList.addAll(muteData.getmKeywordsList());
            this.mRetweetsIds.addAll(muteData.getmRetweetsIds());
            this.isCacheLoaded = true;
        } catch (IOException | NullPointerException | ConcurrentModificationException e) {
            this.isCacheLoaded = true;
            Log.e(TAG, "Error loading mutes - " + e.getLocalizedMessage());
        }
    }

    public ArrayList<RemoveModel> getmUsersList() {
        return mUsersList;
    }

    public ArrayList<RemoveModel> getmKeywordsList() {
        return mKeywordsList;
    }

    public ArrayList<RemoveModel> getmHashtagsList() {
        return mHashtagsList;
    }

    public ArrayList<RemoveModel> getmClientsList() {
        return mClientsList;
    }

    public static void setInstance(MuteData instance) {
        MuteData.instance = instance;
    }

    public boolean isCacheLoaded() {
        return isCacheLoaded;
    }

    public void setCacheLoaded(boolean cacheLoaded) {
        isCacheLoaded = cacheLoaded;
    }

    public ArrayList<Long> getmRetweetsIds() {
        return mRetweetsIds;
    }

    public void setmRetweetsIds(ArrayList<Long> mRetweetsIds) {
        this.mRetweetsIds = mRetweetsIds;
    }
}

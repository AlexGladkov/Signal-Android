package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.interfaces.UpdateHandler;

/**
 * Created by neura on 11.09.17.
 */

public class LoggedData {
    private static final String TAG = LoggedData.class.getSimpleName();
    private static volatile LoggedData instance;

    public static LoggedData getInstance() {
        LoggedData localInstance = instance;
        if (localInstance == null) {
            synchronized (LoggedData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new LoggedData();
                }
            }
        }
        return localInstance;
    }

    private LoggedData() {
        this.isNewFeed = false;
        this.isNewMention = false;
        this.isNewActivity = false;
        this.isNewMessage = false;
        this.isNewFavorite = false;
        this.isCacheLoaded = false;
        this.updateHandler = new UpdateHandler() {
            @Override
            public void onUpdate() {

            }
        };
    }

    private UpdateHandler updateHandler;
    private boolean isCacheLoaded;
    private boolean isNewFeed;
    private boolean isNewMention;
    private boolean isNewActivity;
    private boolean isNewMessage;
    private boolean isNewFavorite;

    public void saveCache() {
        try {
            Reservoir.put(Cache.Logged + String.valueOf(AppData.ME.getId()), this);
        } catch (IOException e) {
            Log.e(TAG, "Error saving cache - " + e.getLocalizedMessage());
        }
    }

    public void loadCache() {
        try {
            LoggedData loggedData = Reservoir.get(Cache.Logged + String.valueOf(AppData.ME.getId()), LoggedData.class);
            Log.e(TAG, "isNewMessage - " + loggedData.isNewMessage);
            setNewActivity(loggedData.isNewActivity());
            setNewFavorite(loggedData.isNewFavorite());
            setNewMention(loggedData.isNewMention());
            setNewFeed(loggedData.isNewFeed());
            setNewMessage(loggedData.isNewMessage());
            this.isCacheLoaded = true;
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading cache - " + e.getLocalizedMessage());
        }
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public boolean isNewFeed() {
        return isNewFeed;
    }

    public void setNewFeed(boolean newFeed) {
        isNewFeed = newFeed;
    }

    public boolean isNewMention() {
        return isNewMention;
    }

    public void setNewMention(boolean newMention) {
        isNewMention = newMention;
    }

    public boolean isNewActivity() {
        return isNewActivity;
    }

    public void setNewActivity(boolean newActivity) {
        isNewActivity = newActivity;
    }

    public boolean isNewMessage() {
        return isNewMessage;
    }

    public void setNewMessage(boolean newMessage) {
        isNewMessage = newMessage;
    }

    public boolean isNewFavorite() {
        return isNewFavorite;
    }

    public void setNewFavorite(boolean newFavorite) {
        isNewFavorite = newFavorite;
    }

    public boolean isCacheLoaded() {
        return isCacheLoaded;
    }

    public void setCacheLoaded(boolean cacheLoaded) {
        isCacheLoaded = cacheLoaded;
    }
}

package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;
import java.util.ArrayList;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 09.09.17.
 */

public class LikesData {
    private static final String TAG = LikesData.class.getSimpleName();
    private static volatile LikesData instance;

    public static LikesData getInstance() {
        LikesData localInstance = instance;
        if (localInstance == null) {
            synchronized (LikesData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new LikesData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<StatusModel> likesStatuses;
    private UpdateHandler updateHandler;
    private int entryCount;
    private int scrollPosition;
    private int scrollTop;

    private LikesData() {
        this.likesStatuses = new ArrayList<>();
        this.entryCount = 0;
        this.scrollPosition = 0;
        this.scrollTop = 0;
        this.updateHandler = new UpdateHandler() {
            @Override
            public void onUpdate() {

            }
        };
    }

    public void clear() {
        this.likesStatuses.clear();
        this.entryCount = 0;
        this.scrollPosition = 0;
        this.scrollTop = 0;
    }

    public void loadCache() {
        try {
            LikesData likesData = Reservoir.get(Cache.Likes + String.valueOf(AppData.ME.getId()), LikesData.class);
            getLikesStatuses().addAll(likesData.getLikesStatuses());
            setScrollPosition(likesData.getScrollPosition());
            setScrollTop(likesData.getScrollTop());
            setEntryCount(likesData.getEntryCount());
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading likes " + e.getLocalizedMessage());
        }
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.Likes + String.valueOf(AppData.ME.getId()), this);
        } catch (IOException e) {
            Log.e(TAG, "Error saving mentions " + e.getLocalizedMessage());
        }
    }

    public ArrayList<StatusModel> getLikesStatuses() {
        return likesStatuses;
    }

    public void setLikesStatuses(ArrayList<StatusModel> likesStatuses) {
        this.likesStatuses = likesStatuses;
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

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
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

    public static void setInstance(LikesData instance) {
        LikesData.instance = instance;
    }
}

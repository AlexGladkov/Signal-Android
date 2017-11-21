package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationModel;

/**
 * Created by neura on 11.09.17.
 */

public class NotificationsAllData {
    private static final String TAG = NotificationsAllData.class.getSimpleName();
    private static volatile NotificationsAllData instance;

    public static NotificationsAllData getInstance() {
        NotificationsAllData localInstance = instance;
        if (localInstance == null) {
            synchronized (NotificationsAllData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NotificationsAllData();
                }
            }
        }
        return localInstance;
    }

    private int entryCount = 0;
    private ArrayList<NotificationModel> dataList;
    private UpdateHandler updateHandler;

    private NotificationsAllData() {
        this.dataList = new ArrayList<>();
        this.updateHandler = new UpdateHandler() {
            @Override
            public void onUpdate() {

            }
        };
    }

    public void clear() {
        this.dataList.clear();
    }

    public void loadCache() {
        Type resultType = new TypeToken<List<NotificationModel>>() {
        }.getType();
        try {
            List<NotificationModel> notificationModels = Reservoir.get(Cache.NotificationsAll +
                    String.valueOf(AppData.ME.getId()), resultType);
            getDataList().addAll(notificationModels);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading notifications all " + e.getLocalizedMessage());
        }
    }

    public void saveCache(String cacheTAG) {
        if (dataList.size() > 0) {
            try {
                Reservoir.put(Cache.NotificationsAll + String.valueOf(AppData.ME.getId()), dataList);
            } catch (IOException | ConcurrentModificationException | NullPointerException e) {
                Log.e(TAG, "Error saving cache all " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Trying to save empty all notifications from " + cacheTAG);
        }
    }

    public ArrayList<NotificationModel> getDataList() {
        return dataList;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static void setInstance(NotificationsAllData instance) {
        NotificationsAllData.instance = instance;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public void incEntryCount() {
        this.entryCount = entryCount + 1;
        Log.e(TAG, "Notifications entry inced to - " + entryCount);
    }

    public void decEntryCount() {
        this.entryCount = entryCount - 1;
        if (entryCount < 0) entryCount = 0;
    }

    public void clearNew() {
        this.entryCount = 0;
        for (NotificationModel notificationModel : dataList) {
            notificationModel.setHighlighted(false);
        }
        saveCache(TAG);
    }
}

package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationDetailModel;

/**
 * Created by neura on 10.09.17.
 */

public class NotificationsFollowData {
    private static final String TAG = NotificationsFollowData.class.getSimpleName();
    private static volatile NotificationsFollowData instance;

    public static NotificationsFollowData getInstance() {
        NotificationsFollowData localInstance = instance;
        if (localInstance == null) {
            synchronized (NotificationsFollowData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NotificationsFollowData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<NotificationDetailModel> followList;
    private UpdateHandler updateHandler;

    private NotificationsFollowData() {
        this.followList = new ArrayList<>();
    }

    public void clear() {
        this.followList.clear();
    }

    public void loadCache() {
        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
        }.getType();
        try {
            List<NotificationDetailModel> notificationModels = Reservoir.get(Cache.NotificationsFollow +
                    String.valueOf(AppData.ME.getId()), resultType);
            getFollowList().addAll(notificationModels);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading notifications all " + e.getLocalizedMessage());
        }
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.NotificationsFollow + AppData.ME.getId(), followList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving notifications follow " + e.getLocalizedMessage());
        }
    }

    public ArrayList<NotificationDetailModel> getFollowList() {
        return followList;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static void setInstance(NotificationsFollowData instance) {
        NotificationsFollowData.instance = instance;
    }
}

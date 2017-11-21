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
 * Created by neura on 11.09.17.
 */

public class NotificationsRetweetData {
    private static final String TAG = NotificationsRetweetData.class.getSimpleName();
    private static volatile NotificationsRetweetData instance;

    public static NotificationsRetweetData getInstance() {
        NotificationsRetweetData localInstance = instance;
        if (localInstance == null) {
            synchronized (NotificationsRetweetData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NotificationsRetweetData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<NotificationDetailModel> retweetList;
    private UpdateHandler updateHandler;

    private NotificationsRetweetData() {
        this.retweetList = new ArrayList<>();
    }

    public void clear() {
        this.retweetList.clear();
    }

    public void loadCache() {
        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
        }.getType();
        try {
            List<NotificationDetailModel> notificationModels = Reservoir.get(Cache.NotificationsRetweet +
                    String.valueOf(AppData.ME.getId()), resultType);
            for (NotificationDetailModel notificationModel : notificationModels) {
                if (!retweetList.contains(notificationModel)) {
                    retweetList.add(notificationModel);
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading notifications all " + e.getLocalizedMessage());
        }
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.NotificationsRetweet + String.valueOf(AppData.ME.getId()), retweetList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving notifications " + e.getLocalizedMessage());
        }
    }

    public ArrayList<NotificationDetailModel> getRetweetList() {
        return retweetList;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static void setInstance(NotificationsRetweetData instance) {
        NotificationsRetweetData.instance = instance;
    }
}

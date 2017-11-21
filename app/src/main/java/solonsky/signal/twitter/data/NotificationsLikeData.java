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

public class NotificationsLikeData {
    private static final String TAG = NotificationsLikeData.class.getSimpleName();
    private static volatile NotificationsLikeData instance;

    public static NotificationsLikeData getInstance() {
        NotificationsLikeData localInstance = instance;
        if (localInstance == null) {
            synchronized (NotificationsLikeData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NotificationsLikeData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<NotificationDetailModel> likesList;
    private UpdateHandler updateHandler;

    private NotificationsLikeData() {
        this.likesList = new ArrayList<>();
        this.setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {

            }
        });
    }

    public void clear() {
        this.likesList.clear();
    }

    public void loadCache() {
        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
        }.getType();
        try {
            List<NotificationDetailModel> notificationModels = Reservoir.get(Cache.NotificationsLike +
                    String.valueOf(AppData.ME.getId()), resultType);
            likesList.addAll(notificationModels);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading notifications like " + e.getLocalizedMessage());
        }
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.NotificationsLike + String.valueOf(AppData.ME.getId()), likesList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving notifications like " + e.getLocalizedMessage());
        }
    }

    public ArrayList<NotificationDetailModel> getLikesList() {
        return likesList;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static void setInstance(NotificationsLikeData instance) {
        NotificationsLikeData.instance = instance;
    }
}

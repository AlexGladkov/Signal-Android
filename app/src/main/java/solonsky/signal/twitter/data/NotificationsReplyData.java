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

public class NotificationsReplyData {
    private static final String TAG = NotificationsReplyData.class.getSimpleName();
    private static volatile NotificationsReplyData instance;

    public static NotificationsReplyData getInstance() {
        NotificationsReplyData localInstance = instance;
        if (localInstance == null) {
            synchronized (NotificationsReplyData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new NotificationsReplyData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<NotificationDetailModel> replyList;
    private UpdateHandler updateHandler;

    private NotificationsReplyData() {
        this.replyList = new ArrayList<>();
        this.updateHandler = new UpdateHandler() {
            @Override
            public void onUpdate() {

            }
        };
    }

    public void clear() {
        this.replyList.clear();
    }

    public void loadCache() {
        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
        }.getType();
        try {
            List<NotificationDetailModel> notificationModels = Reservoir.get(Cache.NotificationsReply +
                    String.valueOf(AppData.ME.getId()), resultType);
            getReplyList().addAll(notificationModels);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error loading notifications all " + e.getLocalizedMessage());
        }
    }

    public void saveCache() {
        try {
            Reservoir.put(Cache.NotificationsReply + String.valueOf(AppData.ME.getId()), replyList);
        } catch (IOException e) {
            Log.e(TAG, "Error saving notifications reply " + e.getLocalizedMessage());
        }
    }

    public ArrayList<NotificationDetailModel> getReplyList() {
        return replyList;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public static void setInstance(NotificationsReplyData instance) {
        NotificationsReplyData.instance = instance;
    }
}

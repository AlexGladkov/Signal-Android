package solonsky.signal.twitter.data;

import solonsky.signal.twitter.interfaces.ProfileRefreshHandler;
import solonsky.signal.twitter.interfaces.UpdateHandler;

/**
 * Created by neura on 11.10.17.
 */

public class ProfileRefreshData {
    private static volatile ProfileRefreshData instance;
    private ProfileRefreshHandler updateHandler;

    public static ProfileRefreshData getInstance() {
        ProfileRefreshData localInstance = instance;
        if (localInstance == null) {
            synchronized (ProfileRefreshData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ProfileRefreshData();
                }
            }
        }
        return localInstance;
    }

    public ProfileRefreshHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(ProfileRefreshHandler updateHandler) {
        this.updateHandler = updateHandler;
    }
}

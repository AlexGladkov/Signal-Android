package solonsky.signal.twitter.data;

import java.util.ArrayList;

import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.DirectModel;

/**
 * Created by neura on 10.09.17.
 */

public class DirectData {
    private static volatile DirectData instance;

    public static DirectData getInstance() {
        DirectData localInstance = instance;
        if (localInstance == null) {
            synchronized (DirectData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DirectData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<DirectModel> mMessagesList;
    private int entryCount;
    private UpdateHandler updateHandler;

    private DirectData() {
        mMessagesList = new ArrayList<>();
        entryCount = 0;
    }

    public void clear() {
        mMessagesList.clear();
        entryCount = 0;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public ArrayList<DirectModel> getmMessagesList() {
        return mMessagesList;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public void setUpdateHandler(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    public static void setInstance(DirectData instance) {
        DirectData.instance = instance;
    }
}

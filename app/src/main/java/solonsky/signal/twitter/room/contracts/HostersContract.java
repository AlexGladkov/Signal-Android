package solonsky.signal.twitter.room.contracts;

import android.arch.persistence.room.Room;

import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by agladkov on 01.02.18.
 */

public class HostersContract {
    public final static String TIMESTAMP = "timestamp";
    public final static String USER_ID = "user_id";
    public final static String GET = "SELECT * FROM " + RoomContract.HOSTERS_TABLE;
    public final static String GET_DATE = "SELECT * FROM " + RoomContract.HOSTERS_TABLE +
            " ORDER BY " + TIMESTAMP + " DESC";
    public final static String GET_LAST = "SELECT * FROM " + RoomContract.HOSTERS_TABLE +
            " ORDER BY " + TIMESTAMP + " DESC LIMIT 1";
}

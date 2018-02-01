package solonsky.signal.twitter.room.contracts;

import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by agladkov on 01.02.18.
 */

public class HostersContract {
    public final static String GET = "SELECT * FROM " + RoomContract.HOSTERS_TABLE;
}

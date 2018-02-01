package solonsky.signal.twitter.room.contracts;

import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by agladkov on 01.02.18.
 */

public class UsersContract {
    public final static String GET_USERS = "SELECT * FROM " + RoomContract.USER_TABLE + " WHERE id IS :id";
}

package solonsky.signal.twitter.room;

/**
 * Created by sunwi on 22.01.2018.
 */

public class RoomContract {
    // DB names
    public final static String DB_NAME = "signal_db";

    // Table names
    public final static String USER_TABLE = "user_table";
    public final static String USER_ID_TABLE = "user_id_table";
    public final static String SETTINGS_TABLE = "settings_table";
    public final static String HOSTERS_TABLE = "hosters_table";
    public final static String CONFIGURATION_TABLE = "configuration_table";

    // User queries
    public final static String USER_GET_ALL = "SELECT * FROM " + USER_TABLE;
    public final static String USER_GET_BY_SCREENNAME = "SELECT * FROM " + USER_TABLE + " WHERE screen_name" +
            " LIKE :screenName";
    public final static String USER_GET_BY_NAME = "SELECT * FROM " + USER_TABLE + " WHERE name " +
            "LIKE :name";
    public final static String USER_GET_BY_ID = "SELECT * FROM " + USER_ID_TABLE + " WHERE id " +
            "LIKE :id";
}
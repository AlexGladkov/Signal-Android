package solonsky.signal.twitter.helpers;

import android.location.Location;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.FeedModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.models.UserModel;
import twitter4j.DirectMessage;

/**
 * Created by neura on 21.05.17.
 * Helper class for contains global app data
 */

public class AppData {
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final String KEY_NOTIFICATION_ID = "key_notification_id";
    public static final String KEY_NOTIFICATION_SENDER = "key_notification_sender";
    public static final String KEY_NOTIFICATION_RECEIVER = "key_notification_receiver";
    public static final String KEY_NOTIFICATION_TYPE = "key_notification_type";

    public static final String USERS_NOTIFICATION_MENTIONS = "From All";
    public static ArrayList<DirectMessage> CURRENT_DIRECT = new ArrayList<>();
    public static final String notificationFilter = "ru.arktos.agladkov.signal";

    public static String DM_SELECTED_USER;
    public static long DM_OTHER_ID;
    public static int DM_POSITION = 0;

    public static String MEDIA_URL = "";
    public static Flags.MEDIA_TYPE MEDIA_TYPE;

    public static int DIVIDER_SHORT = 0;
    public static int DIVIDER_LONG = 1;
    public static int DIVIDER_NONE = 2;

    public static long lastSwitchTime = 0;

    public static final int LINK_LENGTH = 50;

    public static int TWITTER_RESTRICT = 280;
    public static int LOGGED_BACK_STATE_COUNT = 0;

    public final static int UI_STATE_NO_ITEMS = 0;
    public final static int UI_STATE_LOADING = 1;
    public final static int UI_STATE_VISIBLE = 2;

    public static final int FEED_TYPE_TEXT = 0;
    public static final int FEED_TYPE_IMAGE = 1;
    public static final int FEED_TYPE_VIDEO = 2;
    public static final int FEED_TYPE_GIF = 3;
    public static final int FEED_TYPE_QUOTE = 4;

    public static final int SETTINGS_TYPE_SWITCH = 0;
    public static final int SETTINGS_TYPE_TEXT = 1;

    public static final int CHAT_ME = 0;
    public static final int CHAT_NOT_ME = 1;

    public static String SELECTED_USER = "";

    public static UserModel NOTIFICATIONS_CURRENT_USER;

    public static int TOOLBAR_LOGGED_MAIN = 0;
    public static int TOOLBAR_LOOGED_CHOOSE = 1;
    public static int TOOLBAR_LOGGED_SEARCH = 2;
    public static int TOOLBAR_LOGGED_PROFILE = 3;
    public static int TOOLBAR_LOOGED_MUTE = 4;
    public static int TOOLBAR_LOGGED_DM = 5;
    public static int TOOLBAR_LOGGED_SIMPLE = 6;
    public static int TOOLBAR_LOGGED_SEARCHED = 7;

    public static FeedModel CURRENT_TWEET_MODEL = null;
    public static StatusModel CURRENT_STATUS_MODEL;
    public static User CURRENT_USER;
    public static String CURRENT_SCREEN_NAME;
    public static long CURRENT_USER_ID = 0;

    public static boolean IS_YOUR_PROFILE = true;

    public static String TWEET_CURRENT_SIZE = "14pt";

    public static ArrayList<ConfigurationUserModel> configurationUserModels = new ArrayList<>();
    public static ConfigurationModel appConfiguration = null;
    public static ConfigurationUserModel userConfiguration = null;

    public static String CLIENT_TOKEN = "";
    public static String CLIENT_SECRET = "";
    public static final String CONSUMER_KEY = "l1gr060JG9HxLDZJyCIynxFKq";
    public static final String CONSUMER_SECRET = "TDRUT8php7ipQpQB0vdWmz59RnRFeXfRomCYSGwvzkF5na8HSW";

    public static User ME = null;

    public static String COMPOSE_LINK = "";
    public static String COMPOSE_MENTION = "";
    public static String COMPOSE_HASHTAG = "";
    public static String searchQuery = "";

    @Nullable
    public static Location currentLocation = null;
}

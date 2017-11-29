package solonsky.signal.twitter.helpers;

import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 24.07.17.
 */

public class Flags {
    public static final int PENDING_REPLY = 1;
    public static final int PENDING_FOLLOW = 2;

    public static final String PROFILE_DATA = "ProfileData";
    public static final String PROFILE_ID = "ProfileId";
    public static final String PROFILE_SCREEN_NAME = "ProfileScreenName";

    public enum Dialogs {
        HASH, USER, MEDIA, LINK
    }

    public enum NotificationTypes {
        RT(0), REPLY(1), FAV(2), MENTION(3), FOLLOW(4), LISTED(5), QUOTED(6), UNDEFINED(7), DIRECT(8);

        private final int value;

        NotificationTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Directions {
        FROM_RIGHT, FADE
    }

    public static final String MEDIA_PHOTO = "photo";
    public static final String MEDIA_GIF = "animated_gif";
    public static final String MEDIA_VIDEO = "video";
    public static final String MEDIA_YOUTUBE = "youtube";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String NOTIFICATION_USERNAME = "notificationUsername";
    public static final String NOTIFICATION_STATUS_ID = "notificationStatusId";

    public static int DIVIDER_SHORT = 0;
    public static int DIVIDER_LONG = 1;
    public static int DIVIDER_NONE = 2;

    public static boolean needsToRedrawFeed = false;
    public static boolean needsToRedrawMentions = false;
    public static boolean needsToRedrawFavorites = false;
    public static boolean needsToRedrawDirect = false;

    public static boolean isSearchUser = false;
    public static String searchUserQuery = "";
    public static boolean homeUser = false;

    public enum STYLE {
        TINY, SMALL, REGULAR, BIG, HUGE
    }

    public enum STATUS_TYPE {
        COUNTER, TITLE, ARROW
    }

    public enum MEDIA_TYPE {
        GIF, IMAGE, VIDEO, YOUTUBE
    }

    public enum UserSource {
        screenName, data, id
    }

    public static boolean isSearchSaved = false;
    public static boolean isUpdated = false;
    public static boolean DM_IS_NEW = true; // True if DM has highlight
    public static StatusModel SENT_STATUS = null; // When tweet sent show tweet in feed without reload

    public static boolean DELETE_THREAD = false; // Flags to start deleting direct thread

    public static UserSource userSource = UserSource.screenName;
    public static Directions userDirection;

    /* Compose block */
    public static int CURRENT_COMPOSE = 0;
    public static int COMPOSE_NONE = 0;
    public static int COMPOSE_REPLY = 1;
    public static int COMPOSE_QUOTE = 2;
    public static int COMPOSE_LINK = 3;
    public static int COMPOSE_MENTION = 4;
    public static int COMPOSE_HASHTAG = 5;
}

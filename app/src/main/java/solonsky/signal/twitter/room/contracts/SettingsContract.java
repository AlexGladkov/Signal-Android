package solonsky.signal.twitter.room.contracts;

import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by agladkov on 31.01.18.
 */

public class SettingsContract {
    public final static String GET = "SELECT * FROM " + RoomContract.SETTINGS_TABLE;

    public final static String FONT_SIZE = "fontSize"; // Tweet font size (in dp)
    public final static String THUMBNAILS = "thumbnails"; // Tweet image preview (small, big)
    public final static String DARK_MODE = "darkMode"; // Client dark mode

    public final static String REAL_NAMES = "realNames"; // show real name if true
    public final static String ROUND_AVATARS = "roundAvatars"; // show round avatars if true
    public final static String RELATIVE_DATES = "relativeDates"; // show relative dates if true

    // Timeline group
    public final static String STATIC_TOP_BARS = "staticTopBars"; // hide toolbar if false
    public final static String STATIC_BOTTOM_BARS = "staticBottomBars"; // hide bottom bar if false
    public final static String GROUP_DIALOGS = "groupDialogs"; // show tweets separate if false
    public final static String SHOW_MENTIONS = "showMentions"; // show mentions in feed if true
    public final static String SHOW_RETWEETS = "showRetweets"; // show retweets in feed if true
    public final static String TWEET_MARKER = "tweetMarker"; // show tweet marker if true
    public final static String STREAM_ON_WI_FI = "streamOnWifi"; // stream with wifi if true

    // Gestures group
    public final static String SHORT_TAP = "shortTap"; // short tap action
    public final static String LONG_TAP = "longTap"; // long tap action
    public final static String DOUBLE_TAP = "doubleTap"; // double tap action

    // Advanced group
    public final static String DIM_MEDIA = "dimMediaAtNight"; // dim media at night if true
    public final static String GROUP_PUSH = "groupPushNotifications"; // group push notifications if true
    public final static String PIN_TO_TOP = "pinToTopOnStreaming"; // TODO
    public final static String SOUNDS = "sounds"; // TODO
}

package solonsky.signal.twitter.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import solonsky.signal.twitter.room.models.SettingsEntity;

/**
 * Created by neura on 26.07.17.
 */

public class ConfigurationModel {
    public final static int FONT_TINY = 0;
    public final static int FONT_SMALL = 1;
    public final static int FONT_REGULAR = 2;
    public final static int FONT_BIG = 3;
    public final static int FONT_HUGE = 4;

    public final static int THUMB_BIG = 0;
    public final static int THUMB_SMALL = 1;
    public final static int THUMB_OFF = 2;

    public final static int DARK_ALWAYS = 0;
    public final static int DARK_AT_NIGHT = 1;
    public final static int DARK_OFF = 2;

    public final static int TAP_SHOW_ACTIONS = 0;
    public final static int TAP_VIEW_DETAILS = 1;
    public final static int TAP_OPEN_MEDIA = 2;
    public final static int TAP_GO_TO_LINK = 3;

    public final static int TAP_LAST_SHARING = 4;
    public final static int TAP_READ_LATER = 5;
    public final static int TAP_TRANSLATE = 6;
    public final static int TAP_SHARE = 7;

    public final static int TAP_REPLY = 8;
    public final static int TAP_QUOTE = 9;
    public final static int TAP_RETWEET = 10;
    public final static int TAP_LIKE = 11;

    // Appearance group
    private int fontSize; // Tweet font size (in dp)
    private int thumbnails; // Tweet image preview (small, big)
    private int darkMode; // Client dark mode

    private boolean realNames; // show real name if true
    private boolean roundAvatars; // show round avatars if true
    private boolean relativeDates; // show relative dates if true

    // Timeline group
    private boolean staticTopBars; // hide toolbar if false
    private boolean staticBottomBar; // hide bottom bar if false
    private boolean groupDialogs; // show tweets separate if false
    private boolean showMentions; // show mentions in feed if true
    private boolean showRetweets; // show retweets in feed if true
    private boolean tweetMarker; // show tweet marker if true
    private boolean streamOnWifi; // stream with wifi if true

    // Gestures group
    private int shortTap; // short tap action
    private int longTap; // long tap action
    private int doubleTap; // double tap action

    // Advanced group
    private boolean dimMediaAtNight; // dim media at night if true
    private boolean groupPushNotifications; // group push notifications if true
    private boolean pinToTopOnStreaming; // TODO
    private boolean sounds; // TODO

    public ConfigurationModel(SettingsEntity settingsEntity) {
        this.fontSize = settingsEntity.getFontSize();
        this.thumbnails = settingsEntity.getThumbnails();
        this.darkMode = settingsEntity.getDarkMode();
        this.realNames = settingsEntity.isRealNames();
        this.roundAvatars = settingsEntity.isRoundAvatars();
        this.relativeDates = settingsEntity.isRelativeDates();
        this.staticTopBars = settingsEntity.isStaticTopBars();
        this.staticBottomBar = settingsEntity.isStaticBottomBar();
        this.groupDialogs = settingsEntity.isGroupDialogs();
        this.showMentions = settingsEntity.isShowMentions();
        this.showRetweets = settingsEntity.isShowRetweets();
        this.tweetMarker = settingsEntity.isTweetMarker();
        this.streamOnWifi = settingsEntity.isStreamOnWifi();
        this.shortTap = settingsEntity.getShortTap();
        this.longTap = settingsEntity.getLongTap();
        this.doubleTap = settingsEntity.getDoubleTap();
        this.dimMediaAtNight = settingsEntity.isDimMediaAtNight();
        this.groupPushNotifications = settingsEntity.isGroupPushNotifications();
        this.pinToTopOnStreaming = settingsEntity.isPinToTopOnStreaming();
        this.sounds = settingsEntity.isSounds();
    }

    private ConfigurationModel(int fontSize, int thumbnails, int darkMode, boolean realNames, boolean roundAvatars,
                              boolean relativeDates, boolean staticTopBars, boolean staticBottomBar,
                              boolean groupDialogs, boolean showMentions, boolean showRetweets,
                              boolean tweetMarker, boolean streamOnWifi, int shortTap, int longTap,
                              int doubleTap, boolean dimMediaAtNight, boolean groupPushNotifications,
                              boolean pinToTopOnStreaming, boolean sounds) {
        this.fontSize = fontSize;
        this.thumbnails = thumbnails;
        this.darkMode = darkMode;
        this.realNames = realNames;
        this.roundAvatars = roundAvatars;
        this.relativeDates = relativeDates;
        this.staticTopBars = staticTopBars;
        this.staticBottomBar = staticBottomBar;
        this.groupDialogs = groupDialogs;
        this.showMentions = showMentions;
        this.showRetweets = showRetweets;
        this.tweetMarker = tweetMarker;
        this.streamOnWifi = streamOnWifi;
        this.shortTap = shortTap;
        this.longTap = longTap;
        this.doubleTap = doubleTap;
        this.dimMediaAtNight = dimMediaAtNight;
        this.groupPushNotifications = groupPushNotifications;
        this.pinToTopOnStreaming = pinToTopOnStreaming;
        this.sounds = sounds;
    }

    /**
     * Create configuration with default settings
     * @return ConfigurationModel
     */
    public static ConfigurationModel defaultSettings() {
        return new ConfigurationModel(FONT_REGULAR, THUMB_SMALL,
                DARK_AT_NIGHT, true, true, true, true, true, true, true, true, true, true,
                TAP_SHOW_ACTIONS, TAP_LAST_SHARING, TAP_REPLY, true, true, true , true);
    }

    /**
     * Create model from json
     * @param input - input data
     * @return tuned model
     */
    public static ConfigurationModel createFromJson(JsonObject input) {
        return new Gson().fromJson(input, ConfigurationModel.class);
    }

    public JsonObject exportConfiguration() {
        JsonObject configuration = new JsonObject();

        configuration.addProperty("fontSize", fontSize);
        configuration.addProperty("thumbnails", thumbnails);
        configuration.addProperty("darkMode", darkMode);
        configuration.addProperty("realNames", realNames);
        configuration.addProperty("roundAvatars", roundAvatars);
        configuration.addProperty("relativeDates", relativeDates);
        configuration.addProperty("staticTopBars", staticTopBars);
        configuration.addProperty("staticBottomBar", staticBottomBar);
        configuration.addProperty("groupDialogs", groupDialogs);
        configuration.addProperty("showMentions", showMentions);
        configuration.addProperty("showRetweets", showRetweets);
        configuration.addProperty("tweetMarker", tweetMarker);
        configuration.addProperty("streamOnWifi", streamOnWifi);
        configuration.addProperty("shortTap", shortTap);
        configuration.addProperty("longTap", longTap);
        configuration.addProperty("doubleTap", doubleTap);
        configuration.addProperty("dimMediaAtNight", dimMediaAtNight);
        configuration.addProperty("groupPushNotifications", groupPushNotifications);
        configuration.addProperty("pinToTopOnStreaming", pinToTopOnStreaming);
        configuration.addProperty("sounds", sounds);

        return configuration;
    }

    public int getFontSize() {
        return fontSize;
    }
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getThumbnails() {
        return thumbnails;
    }
    public void setThumbnails(int thumbnails) {
        this.thumbnails = thumbnails;
    }

    public int getDarkMode() {
        return darkMode;
    }
    public void setDarkMode(int darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isRealNames() {
        return realNames;
    }
    public void setRealNames(boolean realNames) {
        this.realNames = realNames;
    }

    public boolean isRoundAvatars() {
        return roundAvatars;
    }
    public void setRoundAvatars(boolean roundAvatars) {
        this.roundAvatars = roundAvatars;
    }

    public boolean isRelativeDates() {
        return relativeDates;
    }
    public void setRelativeDates(boolean relativeDates) {
        this.relativeDates = relativeDates;
    }

    public boolean isStaticTopBars() {
        return staticTopBars;
    }
    public void setStaticTopBars(boolean staticTopBars) {
        this.staticTopBars = staticTopBars;
    }

    public boolean isStaticBottomBar() {
        return staticBottomBar;
    }
    public void setStaticBottomBar(boolean staticBottomBar) {
        this.staticBottomBar = staticBottomBar;
    }

    public boolean isGroupDialogs() {
        return groupDialogs;
    }
    public void setGroupDialogs(boolean groupDialogs) {
        this.groupDialogs = groupDialogs;
    }

    public boolean isShowMentions() {
        return showMentions;
    }
    public void setShowMentions(boolean showMentions) {
        this.showMentions = showMentions;
    }

    public boolean isShowRetweets() {
        return showRetweets;
    }
    public void setShowRetweets(boolean showRetweets) {
        this.showRetweets = showRetweets;
    }

    public boolean isTweetMarker() {
        return tweetMarker;
    }
    public void setTweetMarker(boolean tweetMarker) {
        this.tweetMarker = tweetMarker;
    }

    public boolean isStreamOnWifi() {
        return streamOnWifi;
    }
    public void setStreamOnWifi(boolean streamOnWifi) {
        this.streamOnWifi = streamOnWifi;
    }

    public int getShortTap() {
        return shortTap;
    }
    public void setShortTap(int shortTap) {
        this.shortTap = shortTap;
    }

    public int getLongTap() {
        return longTap;
    }
    public void setLongTap(int longTap) {
        this.longTap = longTap;
    }

    public int getDoubleTap() {
        return doubleTap;
    }
    public void setDoubleTap(int doubleTap) {
        this.doubleTap = doubleTap;
    }

    public boolean isDimMediaAtNight() {
        return dimMediaAtNight;
    }
    public void setDimMediaAtNight(boolean dimMediaAtNight) {
        this.dimMediaAtNight = dimMediaAtNight;
    }

    public boolean isGroupPushNotifications() {
        return groupPushNotifications;
    }
    public void setGroupPushNotifications(boolean groupPushNotifications) {
        this.groupPushNotifications = groupPushNotifications;
    }

    public boolean isPinToTopOnStreaming() {
        return pinToTopOnStreaming;
    }
    public void setPinToTopOnStreaming(boolean pinToTopOnStreaming) {
        this.pinToTopOnStreaming = pinToTopOnStreaming;
    }

    public boolean isSounds() {
        return sounds;
    }
    public void setSounds(boolean sounds) {
        this.sounds = sounds;
    }
}

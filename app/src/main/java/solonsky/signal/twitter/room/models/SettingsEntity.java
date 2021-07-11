package solonsky.signal.twitter.room.models;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import solonsky.signal.twitter.helpers.Locales;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.room.RoomContract;
import solonsky.signal.twitter.room.contracts.SettingsContract;

/**
 * Created by agladkov on 31.01.18.
 */

@Entity(tableName = RoomContract.SETTINGS_TABLE)
public class SettingsEntity {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = SettingsContract.FONT_SIZE) private int fontSize;
    @ColumnInfo(name = SettingsContract.THUMBNAILS) private int thumbnails;
    @ColumnInfo(name = SettingsContract.DARK_MODE) private int darkMode;

    @ColumnInfo(name = SettingsContract.REAL_NAMES) private boolean realNames; // show real name if true
    @ColumnInfo(name = SettingsContract.ROUND_AVATARS) private boolean roundAvatars; // show round avatars if true
    @ColumnInfo(name = SettingsContract.RELATIVE_DATES) private boolean relativeDates; // show relative dates if true

    // Timeline group
    @ColumnInfo(name = SettingsContract.STATIC_TOP_BARS) private boolean staticTopBars; // hide toolbar if false
    @ColumnInfo(name = SettingsContract.STATIC_BOTTOM_BARS) private boolean staticBottomBar; // hide bottom bar if false
    @ColumnInfo(name = SettingsContract.GROUP_DIALOGS) private boolean groupDialogs; // show tweets separate if false
    @ColumnInfo(name = SettingsContract.SHOW_MENTIONS) private boolean showMentions; // show mentions in feed if true
    @ColumnInfo(name = SettingsContract.SHOW_RETWEETS) private boolean showRetweets; // show retweets in feed if true
    @ColumnInfo(name = SettingsContract.TWEET_MARKER) private boolean tweetMarker; // show tweet marker if true
    @ColumnInfo(name = SettingsContract.STREAM_ON_WI_FI) private boolean streamOnWifi; // stream with wifi if true

    // Gestures group
    @ColumnInfo(name = SettingsContract.SHORT_TAP) private int shortTap; // short tap action
    @ColumnInfo(name = SettingsContract.LONG_TAP) private int longTap; // long tap action
    @ColumnInfo(name = SettingsContract.DOUBLE_TAP) private int doubleTap; // double tap action

    // Advanced group
    @ColumnInfo(name = SettingsContract.DIM_MEDIA) private boolean dimMediaAtNight; // dim media at night if true
    @ColumnInfo(name = SettingsContract.GROUP_PUSH) private boolean groupPushNotifications; // group push notifications if true
    @ColumnInfo(name = SettingsContract.PIN_TO_TOP) private boolean pinToTopOnStreaming; // TODO
    @ColumnInfo(name = SettingsContract.SOUNDS) private boolean sounds; // TODO
    @ColumnInfo(name = SettingsContract.LOCALE) private String locale; // Localization

    public SettingsEntity(long id, int fontSize, int thumbnails, int darkMode, boolean realNames,
                          boolean roundAvatars, boolean relativeDates, boolean staticTopBars,
                          boolean staticBottomBar, boolean groupDialogs, boolean showMentions,
                          boolean showRetweets, boolean tweetMarker, boolean streamOnWifi, int
                                  shortTap, int longTap, int doubleTap, boolean dimMediaAtNight,
                          boolean groupPushNotifications, boolean pinToTopOnStreaming, boolean sounds) {
        this.id = id;
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
        this.locale = Locales.English.getValue();
    }

    public SettingsEntity(ConfigurationModel configurationModel, long id) {
        this.id = id;
        this.fontSize = configurationModel.getFontSize();
        this.thumbnails = configurationModel.getThumbnails();
        this.darkMode = configurationModel.getDarkMode();
        this.realNames = configurationModel.isRealNames();
        this.roundAvatars = configurationModel.isRoundAvatars();
        this.relativeDates = configurationModel.isRelativeDates();
        this.staticTopBars = configurationModel.isStaticTopBars();
        this.staticBottomBar = configurationModel.isStaticBottomBar();
        this.groupDialogs = configurationModel.isGroupDialogs();
        this.showMentions = configurationModel.isShowMentions();
        this.showRetweets = configurationModel.isShowRetweets();
        this.tweetMarker = configurationModel.isTweetMarker();
        this.streamOnWifi = configurationModel.isStreamOnWifi();
        this.shortTap = configurationModel.getShortTap();
        this.longTap = configurationModel.getLongTap();
        this.doubleTap = configurationModel.getDoubleTap();
        this.dimMediaAtNight = configurationModel.isDimMediaAtNight();
        this.groupPushNotifications = configurationModel.isGroupPushNotifications();
        this.pinToTopOnStreaming = configurationModel.isPinToTopOnStreaming();
        this.sounds = configurationModel.isSounds();
        this.locale = configurationModel.getLocale();
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

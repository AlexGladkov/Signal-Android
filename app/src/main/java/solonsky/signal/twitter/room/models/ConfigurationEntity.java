package solonsky.signal.twitter.room.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.ArrayList;

import solonsky.signal.twitter.helpers.SerializationUtils;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.room.RoomContract;

/**
 * Created by agladkov on 05.02.18.
 */
@Entity(tableName = RoomContract.CONFIGURATION_TABLE)
public class ConfigurationEntity {

    @PrimaryKey private long userId;

    @ColumnInfo(name = "conf_user") private byte[] user;

    @ColumnInfo(name = "conf_consumer_key") private String consumerKey;
    @ColumnInfo(name = "conf_consumer_secret") private String consumerSecret;
    @ColumnInfo(name = "conf_client_token") private String clientToken;
    @ColumnInfo(name = "conf_client_secret") private String clientSecret;

    @ColumnInfo(name = "conf_bottom_ids") private byte[] bottomIds;
    @ColumnInfo(name = "conf_mute_keywords") private byte[] muteKeywords;
    @ColumnInfo(name = "conf_mute_hashtags") private byte[] muteHashtags;
    @ColumnInfo(name = "conf_mute_clients") private byte[] muteClients;

    @ColumnInfo(name = "conf_tab_position") private int tabPosition = 0;

    @ColumnInfo(name = "conf_mentions") private byte[] mentions;

    @ColumnInfo(name = "conf_is_messages") private boolean isMessages;
    @ColumnInfo(name = "conf_is_likes") private boolean isLikes;
    @ColumnInfo(name = "conf_is_retweets") private boolean isRetweets;
    @ColumnInfo(name = "conf_is_quotes") private boolean isQuotes;
    @ColumnInfo(name = "conf_is_followers") private boolean isFollowers;
    @ColumnInfo(name = "conf_is_lists") private boolean isLists;
    @ColumnInfo(name = "conf_is_sound") private boolean isSound;
    @ColumnInfo(name = "conf_is_vibration") private boolean isVibration;

    public static ConfigurationEntity createFromModel(ConfigurationUserModel configurationModel) {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();

        try {
            configurationEntity.user = SerializationUtils.serialize(configurationModel.getUser());
            configurationEntity.bottomIds = SerializationUtils.serialize(configurationModel.getBottomIds());
            configurationEntity.muteKeywords = SerializationUtils.serialize(configurationModel.getMuteKeywords());
            configurationEntity.muteClients = SerializationUtils.serialize(configurationModel.getMuteClients());
            configurationEntity.muteHashtags = SerializationUtils.serialize(configurationModel.getMuteHashtags());
            configurationEntity.mentions = SerializationUtils.serialize(configurationModel.getMentions());
        } catch (IOException e) {
            configurationEntity.user = null;
            configurationEntity.bottomIds = null;
            configurationEntity.muteKeywords = null;
            configurationEntity.muteHashtags = null;
            configurationEntity.mentions = null;
        }

        configurationEntity.consumerKey = configurationModel.getConsumerKey();
        configurationEntity.consumerSecret = configurationModel.getConsumerSecret();
        configurationEntity.clientToken = configurationModel.getClientToken();
        configurationEntity.clientSecret = configurationModel.getClientSecret();

        configurationEntity.tabPosition = configurationModel.getTabPosition();

        configurationEntity.isMessages = configurationModel.isMessages();
        configurationEntity.isLikes = configurationModel.isLikes();
        configurationEntity.isRetweets = configurationModel.isRetweets();
        configurationEntity.isQuotes = configurationModel.isQuotes();
        configurationEntity.isFollowers = configurationModel.isFollowers();
        configurationEntity.isLists = configurationModel.isLists();
        configurationEntity.isSound = configurationModel.isSound();
        configurationEntity.isVibration = configurationModel.isVibration();

        return configurationEntity;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public byte[] getUser() {
        return user;
    }

    public void setUser(byte[] user) {
        this.user = user;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public byte[] getBottomIds() {
        return bottomIds;
    }

    public void setBottomIds(byte[] bottomIds) {
        this.bottomIds = bottomIds;
    }

    public byte[] getMuteKeywords() {
        return muteKeywords;
    }

    public void setMuteKeywords(byte[] muteKeywords) {
        this.muteKeywords = muteKeywords;
    }

    public byte[] getMuteHashtags() {
        return muteHashtags;
    }

    public void setMuteHashtags(byte[] muteHashtags) {
        this.muteHashtags = muteHashtags;
    }

    public byte[] getMuteClients() {
        return muteClients;
    }

    public void setMuteClients(byte[] muteClients) {
        this.muteClients = muteClients;
    }

    public int getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public byte[] getMentions() {
        return mentions;
    }

    public void setMentions(byte[] mentions) {
        this.mentions = mentions;
    }

    public boolean isMessages() {
        return isMessages;
    }

    public void setMessages(boolean messages) {
        isMessages = messages;
    }

    public boolean isLikes() {
        return isLikes;
    }

    public void setLikes(boolean likes) {
        isLikes = likes;
    }

    public boolean isRetweets() {
        return isRetweets;
    }

    public void setRetweets(boolean retweets) {
        isRetweets = retweets;
    }

    public boolean isQuotes() {
        return isQuotes;
    }

    public void setQuotes(boolean quotes) {
        isQuotes = quotes;
    }

    public boolean isFollowers() {
        return isFollowers;
    }

    public void setFollowers(boolean followers) {
        isFollowers = followers;
    }

    public boolean isLists() {
        return isLists;
    }

    public void setLists(boolean lists) {
        isLists = lists;
    }

    public boolean isSound() {
        return isSound;
    }

    public void setSound(boolean sound) {
        isSound = sound;
    }

    public boolean isVibration() {
        return isVibration;
    }

    public void setVibration(boolean vibration) {
        isVibration = vibration;
    }
}

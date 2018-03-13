package solonsky.signal.twitter.models;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.google.gson.JsonArray;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Locales;
import solonsky.signal.twitter.room.models.ConfigurationEntity;

/**
 * Created by neura on 30.07.17.
 */

public class ConfigurationUserModel {
    private static final String TAG = ConfigurationUserModel.class.getSimpleName();

    public static ConfigurationUserModel createFromEntity(@NotNull ConfigurationEntity configurationEntity) {
        ConfigurationUserModel configurationUserModel = new ConfigurationUserModel();
        configurationUserModel.consumerKey = configurationEntity.getConsumerKey();
        configurationUserModel.consumerSecret = configurationEntity.getConsumerSecret();
        configurationUserModel.clientToken = configurationEntity.getClientToken();
        configurationUserModel.clientSecret = configurationEntity.getClientSecret();

        configurationUserModel.bottomIds = configurationEntity.getBottomIds();
        configurationUserModel.user = configurationEntity.getUser();
        configurationUserModel.mentions = configurationEntity.getMentions();
        configurationUserModel.muteClients = configurationEntity.getMuteClients();
        configurationUserModel.muteHashtags = configurationEntity.getMuteHashtags();
        configurationUserModel.muteKeywords = configurationEntity.getMuteKeywords();

        configurationUserModel.tabPosition = configurationEntity.getTabPosition();

        configurationUserModel.isMessages = configurationEntity.isMessages();
        configurationUserModel.isLikes = configurationEntity.isLikes();
        configurationUserModel.isRetweets = configurationEntity.isRetweets();
        configurationUserModel.isQuotes = configurationEntity.isQuotes();
        configurationUserModel.isFollowers = configurationEntity.isFollowers();
        configurationUserModel.isLists = configurationEntity.isLists();
        configurationUserModel.isSound = configurationEntity.isSound();
        configurationUserModel.isVibration = configurationEntity.isVibration();

        return configurationUserModel;
    }

    public enum Mentions {
        FROM_ALL("From All"), FROM_FOLLOW("From people you follow"), OFF("Off");

        private final String text;

        Mentions(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private User user;

    private String consumerKey;
    private String consumerSecret;
    private String clientToken;
    private String clientSecret;

    private ArrayList<Integer> bottomIds;
    private JsonArray muteKeywords;
    private JsonArray muteHashtags;
    private JsonArray muteClients;

    private int tabPosition = 0;

    private Mentions mentions;

    private boolean isMessages;
    private boolean isLikes;
    private boolean isRetweets;
    private boolean isQuotes;
    private boolean isFollowers;
    private boolean isLists;
    private boolean isSound;
    private boolean isVibration;

    public ConfigurationUserModel() {

    }

    public ConfigurationUserModel(User user, String consumerKey,
                                  String consumerSecret, String clientToken,
                                  String clientSecret, ArrayList<Integer> bottomIds, JsonArray muteClients,
                                  JsonArray muteHashtags, JsonArray muteKeywords, Mentions mentions,
                                  boolean isMessages, boolean isLikes, boolean isRetweets,
                                  boolean isQuotes, boolean isFollowers, boolean isLists, boolean isSound,
                                  boolean isVibration) {
        this.user = user;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.clientToken = clientToken;
        this.clientSecret = clientSecret;
        this.bottomIds = bottomIds;
        this.muteHashtags = muteHashtags;
        this.muteKeywords = muteKeywords;
        this.muteClients = muteClients;
        this.mentions = mentions;

        this.isMessages = isMessages;
        this.isLikes = isLikes;
        this.isRetweets = isRetweets;
        this.isQuotes = isQuotes;
        this.isFollowers = isFollowers;
        this.isLists = isLists;
        this.isSound = isSound;
        this.isVibration = isVibration;
    }

    public static ConfigurationUserModel getDefaultInstance(User user, String consumerKey, String consumerSecret,
                                                            String clientToken, String clientSecret) {
        ArrayList<Integer> bottomIds = new ArrayList<>();
        bottomIds.add(0);
        bottomIds.add(1);
        bottomIds.add(2);
        bottomIds.add(3);
        bottomIds.add(4);
        return new ConfigurationUserModel(user, consumerKey, consumerSecret, clientToken, clientSecret,
                bottomIds, new JsonArray(), new JsonArray(), new JsonArray(), Mentions.FROM_ALL, true, true, true, true,
                true, true, true, true);
    }

    public static void saveCache() {
        if (AppData.configurationUserModels == null || AppData.userConfiguration == null) return;
        for (ConfigurationUserModel configurationUserModel : AppData.configurationUserModels) {
            if (configurationUserModel.getUser().getId() == AppData.userConfiguration.getUser().getId()) {
                configurationUserModel.setTabPosition(AppData.userConfiguration.getTabPosition());
                configurationUserModel.setBottomIds(AppData.userConfiguration.getBottomIds());
            }
        }

        saveData();
    }

    public static void saveData() {
        try {
            Reservoir.put(Cache.UsersConfigurations, AppData.configurationUserModels);
        } catch (IOException e) {
            Log.e(TAG, "Error caching config");
        }
    }

    public int getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public ArrayList<Integer> getBottomIds() {
        return bottomIds;
    }

    public void setBottomIds(ArrayList<Integer> bottomIds) {
        this.bottomIds = bottomIds;
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

    public JsonArray getMuteKeywords() {
        return muteKeywords;
    }

    public void setMuteKeywords(JsonArray muteKeywords) {
        this.muteKeywords = muteKeywords;
    }

    public JsonArray getMuteHashtags() {
        return muteHashtags;
    }

    public void setMuteHashtags(JsonArray muteHashtags) {
        this.muteHashtags = muteHashtags;
    }

    public JsonArray getMuteClients() {
        return muteClients;
    }

    public void setMuteClients(JsonArray muteClients) {
        this.muteClients = muteClients;
    }

    public Mentions getMentions() {
        return mentions;
    }

    public void setMentions(Mentions mentions) {
        this.mentions = mentions;
    }
}

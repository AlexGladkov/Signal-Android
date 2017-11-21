package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Created by neura on 26.06.17.
 */

public class StatusModel extends BaseObservable {
    private final String TAG = StatusModel.class.getSimpleName();
    private long id;
    private long currentUserRetweetId;
    private long favoriteCount;
    private long inReplyToStatusId;
    private long quotedStatusId;
    private long retweetCount;

    private Flags.MEDIA_TYPE statusMediaType;
    private Flags.MEDIA_TYPE quoteMediaType;

    private Date createdAt;

//    private String elapsingTime;
    private String lang;
    private String source;
    private String text;
    private String rtText;
    private JsonObject geoLocation;
    private JsonObject place;
    @SerializedName("inReplyToScreenName")
    private String inReplyToScreenName;

    private JsonArray hashtagEntities;
    private JsonArray mediaEntities;
    private JsonArray urlEntities;
    private JsonArray userMentionEntities;

    private User user;
    private StatusModel retweetedStatus;
    private StatusModel quotedStatus;

    private boolean isFavorited;
    private boolean isRetweetedByMe;
    private boolean isRetweeted;
    private boolean isRetweet;
    private boolean isReplyStart = false;
    private boolean isReplyEnd = false;
    private boolean isNewStatus = false;

    // Custom fields
    private String mediaType;
    private int divideState = Flags.DIVIDER_SHORT;
    private boolean isExpand = false;
    private boolean isHighlighted = false;

    public interface StatusClickHandler {

        void onShareClick(View v);

        void onLikeClick(View v);

        void onRetweetClick(View v);

        void onReplyClick(View v);

        void onMoreClick(View v);

        void onContentClick(View v);

        void onProfileClick(View v);

        void onQuoteClick(View v);

        void onQuoteUserClick(View v);

        void onQuoteContentClick(View v);

        boolean longUserClick(View v);

        boolean longContentClick(View v);

        boolean longQuoteContentClick(View v);
    }

    public StatusModel(long id, long currentUserRetweetId, long favoriteCount, long inReplyToStatusId,
                       long quotedStatusId, long retweetCount, Date createdAt, String lang,
                       String source, String text, String inReplyToScreenName, JsonArray hashtagEntities, JsonArray mediaEntities,
                       JsonArray urlEntities, JsonArray userMentionEntities, User user, StatusModel retweetedStatus,
                       boolean isFavorited, boolean isRetweeted, boolean isRetweet, boolean isRetweetedByMe) {
        this.mediaType = Flags.MEDIA_PHOTO;
        this.id = id;
        this.currentUserRetweetId = currentUserRetweetId;
        this.favoriteCount = favoriteCount;
        this.inReplyToStatusId = inReplyToStatusId;
        this.quotedStatusId = quotedStatusId;
        this.retweetCount = retweetCount;
        this.createdAt = createdAt;
        this.lang = lang;
        this.source = source;
        this.text = text;
        this.rtText = "";
        this.inReplyToScreenName = inReplyToScreenName;
        this.hashtagEntities = hashtagEntities;
        this.mediaEntities = mediaEntities;
        this.urlEntities = urlEntities;
        this.userMentionEntities = userMentionEntities;
        this.user = user;
        this.retweetedStatus = retweetedStatus;
        this.isFavorited = isFavorited;
        this.isRetweeted = isRetweeted;
        this.isRetweet = isRetweet;
        this.isRetweetedByMe = isRetweetedByMe;
        this.isReplyStart = false;
        this.isReplyEnd = false;
        this.divideState = Flags.DIVIDER_SHORT;
        this.isExpand = true;
        this.isHighlighted = false;
        this.statusMediaType = Flags.MEDIA_TYPE.IMAGE;
        this.quoteMediaType = Flags.MEDIA_TYPE.IMAGE;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StatusModel)) return false;
        if (obj == this) return true;

        StatusModel statusModel = (StatusModel) obj;
        return this.id == statusModel.getId();
    }

    public void parseYoutubeTest() {
        for (JsonElement urlEntity : this.getUrlEntities()) {
            if (urlEntity.getAsJsonObject().get("expandedURL").getAsString().contains("youtu")) {
                boolean hasUrl = false;
                for (JsonElement mediaEntity : this.getMediaEntities()) {
                    if (mediaEntity.getAsJsonObject().get("displayURL").getAsString().toLowerCase()
                            .equals(urlEntity.getAsJsonObject().get("expandedURL").getAsString().toLowerCase())) {
                        hasUrl = true;
                        break;
                    }
                }

                if (!hasUrl) {
                    JsonObject urlJson = (JsonObject) urlEntity;
                    String url = urlJson.get("expandedURL").getAsString().split("/")[3];
                    if (url.contains("&")) {
                        url = url.split("&")[0];
                    }

                    if (url.contains("?")) {
                        url = url.replace("?", " ?");
                        url = url.split(" ?")[0];
                    }

                    String previewUrl = "http://img.youtube.com/vi/[video_id]/0.jpg"
                            .replace("[video_id]", url);

                    previewUrl = previewUrl.replace("watch?v=", "");

                    Log.e(TAG, "previewUrl - " + previewUrl);
                    Log.e(TAG, "expanded - " + urlJson.get("expandedURL").getAsString());

//                    newMedia.addProperty("displayURL", urlJson.get("expandedURL").getAsString());
//                    newMedia.addProperty("expandedURL", urlJson.get("expandedURL").getAsString());
//                    newMedia.addProperty("mediaURL", urlJson.get("expandedURL").getAsString());
//                    newMedia.addProperty("mediaURLHttps", previewUrl);
//                    newMedia.addProperty("type", "youtube");
//                    newMedia.addProperty("url", urlJson.get("expandedURL").getAsString());
//                    this.getMediaEntities().add(newMedia);
                }
            }
        }
    }

    public void parseYoutube() {
        for (JsonElement urlEntity : this.getUrlEntities()) {
            if (urlEntity.getAsJsonObject().get("expandedURL").getAsString().contains("youtu")) {
                boolean hasUrl = false;
                for (JsonElement mediaEntity : this.getMediaEntities()) {
                    if (mediaEntity.getAsJsonObject().get("displayURL").getAsString().toLowerCase()
                            .equals(urlEntity.getAsJsonObject().get("expandedURL").getAsString().toLowerCase())) {
                        hasUrl = true;
                        break;
                    }
                }

                setText(getText().replace(urlEntity.getAsJsonObject().get("displayURL").getAsString(), ""));

                if (!hasUrl) {
                    JsonObject urlJson = (JsonObject) urlEntity;
                    JsonObject newMedia = new JsonObject();
//                this.getUrlEntities().remove(urlEntity);
                    String previewUrl = "http://img.youtube.com/vi/[video_id]/0.jpg"
                            .replace("[video_id]", urlJson.get("expandedURL").getAsString().split("/")[3]);

                    previewUrl = previewUrl.replace("watch?v=", "");

                    newMedia.addProperty("displayURL", urlJson.get("expandedURL").getAsString());
                    newMedia.addProperty("expandedURL", urlJson.get("expandedURL").getAsString());
                    newMedia.addProperty("mediaURL", urlJson.get("expandedURL").getAsString());
                    newMedia.addProperty("mediaURLHttps", previewUrl);
                    newMedia.addProperty("type", "youtube");
                    newMedia.addProperty("url", urlJson.get("expandedURL").getAsString());
                    this.getMediaEntities().add(newMedia);
                }
            }
        }
    }

    public void tuneModel(Status status) {
        setRetweet(status.isRetweet());
        setRetweetedByMe(status.isRetweetedByMe());
        setUser(User.getFromUserInstance(status.getUser()));
        Log.e(TAG, "geo - " + geoLocation);
        parseYoutube();

        if (status.getQuotedStatus() != null) {
            quotedStatus.setUser(User.getFromUserInstance(status.getQuotedStatus().getUser()));
        }

        if (status.isRetweet() || status.isRetweetedByMe()) {
            setRtText(Utilities.getRtText(status.isRetweet(), status.isRetweetedByMe(),
                    status.getRetweetCount(), AppData.appConfiguration.isRealNames() ?
                            status.getUser().getName() : status.getUser().getScreenName()));

            if (isRetweet()) {
                retweetedStatus.setUser(User.getFromUserInstance(status.getRetweetedStatus().getUser()));
                for (int j = 0; j < getRetweetedStatus().getMediaEntities().size(); j++) {
                    JsonObject mediaEntity = (JsonObject) getRetweetedStatus().getMediaEntities().get(j);
                    getRetweetedStatus().setText(getRetweetedStatus()
                            .getText().replace(mediaEntity.get("url").getAsString(),
                                    AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_OFF ?
                                            mediaEntity.get("url").getAsString() : ""));
                }

                if (getRetweetedStatus().getUrlEntities().size() > 0) {
                    for (int j = 0; j < getRetweetedStatus().getUrlEntities().size(); j++) {
                        JsonObject urlEntity = (JsonObject) getRetweetedStatus().getUrlEntities().get(j);
                        getRetweetedStatus().setText(getRetweetedStatus()
                                .getText().replace(urlEntity.get("url").getAsString(),
                                        urlEntity.get("expandedURL").getAsString()));
                    }
                }
            }
        } else {
            if (getUrlEntities().size() > 0) {
                for (int j = 0; j < getUrlEntities().size(); j++) {
                    JsonObject urlEntity = (JsonObject) getUrlEntities().get(j);
                    setText(getText().replace(urlEntity.get("url").getAsString(), urlEntity.get("expandedURL").getAsString()));
                }
            }

            if (getMediaEntities().size() > 0) {
                for (int j = 0; j < getMediaEntities().size(); j++) {
                    JsonObject mediaEntity = (JsonObject) getMediaEntities().get(j);
                    setText(getText().replace(mediaEntity.get("url").getAsString(),
                            AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_OFF ?
                                    mediaEntity.get("url").getAsString() : ""));
                }
            }
        }
    }

    public void linkClarify() {
        if (!isRetweet() && !(quotedStatusId > -1)) {
            for (JsonElement jsonElement : urlEntities) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                text = text.replace(jsonObject.get("expandedURL").getAsString(), jsonObject.get("displayURL").getAsString());
            }
        } else if (quotedStatusId > -1 && quotedStatus != null) {

            String parsedText;
            if (retweetedStatus != null) {
                parsedText = retweetedStatus.getText();
            } else {
                parsedText = text;
            }

            String[] words = parsedText.split("\\s+");
            for (String word : words) {
                if (word.contains("status/" + quotedStatusId)) {
                    if (retweetedStatus != null) {
                        retweetedStatus.setText(retweetedStatus.getText().replace(word, ""));
                    } else {
                        text = text.replace(word, "");
                    }
                }
            }

            for (JsonElement jsonElement : quotedStatus.getUrlEntities()) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                quotedStatus.setText(quotedStatus.getText().replace(jsonObject.get("url").getAsString(),
                        jsonObject.get("displayURL").getAsString()));
            }

            for (JsonElement jsonElement : quotedStatus.getMediaEntities()) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                quotedStatus.setText(quotedStatus.getText().replace(jsonObject.get("url").getAsString(), ""));
            }
        } else if (isRetweet() && retweetedStatus != null) {
            for (JsonElement jsonElement : retweetedStatus.getUrlEntities()) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                retweetedStatus.setText(retweetedStatus.getText().replace(jsonObject.get("expandedURL").getAsString(),
                        jsonObject.get("displayURL").getAsString()));
            }

            for (JsonElement jsonElement : retweetedStatus.getMediaEntities()) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                retweetedStatus.setText(retweetedStatus.getText().replace(jsonObject.get("url").getAsString(), ""));
            }
        }
    }

    public boolean isNewStatus() {
        return isNewStatus;
    }

    public void setNewStatus(boolean newStatus) {
        isNewStatus = newStatus;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public void setInReplyToScreenName(String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
    }

    @Bindable
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
        notifyPropertyChanged(BR.mediaType);
    }

    @Bindable
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public long getCurrentUserRetweetId() {
        return currentUserRetweetId;
    }

    public void setCurrentUserRetweetId(long currentUserRetweetId) {
        this.currentUserRetweetId = currentUserRetweetId;
        notifyPropertyChanged(BR.currentUserRetweetId);
    }

    @Bindable
    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
        notifyPropertyChanged(BR.favoriteCount);
    }

    @Bindable
    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
        notifyPropertyChanged(BR.inReplyToStatusId);
    }

    @Bindable
    public long getQuotedStatusId() {
        return quotedStatusId;
    }

    public void setQuotedStatusId(long quotedStatusId) {
        this.quotedStatusId = quotedStatusId;
        notifyPropertyChanged(BR.quotedStatusId);
    }

    @Bindable
    public long getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(long retweetCount) {
        this.retweetCount = retweetCount;
        notifyPropertyChanged(BR.retweetCount);
    }

    @Bindable
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        notifyPropertyChanged(BR.createdAt);
    }

    public String getRtText() {
        return rtText;
    }

    public void setRtText(String rtText) {
        this.rtText = rtText;
    }

    public boolean isReplyStart() {
        return isReplyStart;
    }

    public void setReplyStart(boolean replyStart) {
        isReplyStart = replyStart;
    }

    public boolean isReplyEnd() {
        return isReplyEnd;
    }

    public void setReplyEnd(boolean replyEnd) {
        isReplyEnd = replyEnd;
    }

    @Bindable
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        notifyPropertyChanged(BR.lang);
    }

    @Bindable
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
        notifyPropertyChanged(BR.source);
    }

    @Bindable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public JsonArray getHashtagEntities() {
        return hashtagEntities;
    }

    public void setHashtagEntities(JsonArray hashtagEntities) {
        this.hashtagEntities = hashtagEntities;
        notifyPropertyChanged(BR.hashtagEntities);
    }

    @Bindable
    public JsonArray getMediaEntities() {
        return mediaEntities;
    }

    public void setMediaEntities(JsonArray mediaEntities) {
        this.mediaEntities = mediaEntities;
        notifyPropertyChanged(BR.mediaEntities);
    }

    @Bindable
    public JsonArray getUrlEntities() {
        return urlEntities;
    }

    public void setUrlEntities(JsonArray urlEntities) {
        this.urlEntities = urlEntities;
        notifyPropertyChanged(BR.urlEntities);
    }

    @Bindable
    public JsonArray getUserMentionEntities() {
        return userMentionEntities;
    }

    public void setUserMentionEntities(JsonArray userMentionEntities) {
        this.userMentionEntities = userMentionEntities;
        notifyPropertyChanged(BR.userMentionEntities);
    }

    @Bindable
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
    }

    @Bindable
    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
        notifyPropertyChanged(BR.favorited);
    }

    @Bindable
    public boolean isRetweeted() {
        return isRetweeted;
    }

    public void setRetweeted(boolean retweeted) {
        isRetweeted = retweeted;
        notifyPropertyChanged(BR.retweeted);
    }

    @Bindable
    public boolean isRetweetedByMe() {
        return isRetweetedByMe;
    }

    public void setRetweetedByMe(boolean retweetedByMe) {
        isRetweetedByMe = retweetedByMe;
        notifyPropertyChanged(BR.retweetedByMe);
    }

    @Bindable
    public boolean isRetweet() {
        return isRetweet;
    }

    public void setRetweet(boolean retweet) {
        isRetweet = retweet;
        notifyPropertyChanged(BR.retweet);
    }

    @Bindable
    public int getDivideState() {
        return divideState;
    }

    public void setDivideState(int divideState) {
        this.divideState = divideState;
        notifyPropertyChanged(BR.divideState);
    }

    @Bindable
    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
        notifyPropertyChanged(BR.expand);
    }

    @Bindable
    public StatusModel getRetweetedStatus() {
        return retweetedStatus;
    }

    public void setRetweetedStatus(StatusModel retweetedStatus) {
        this.retweetedStatus = retweetedStatus;
        notifyPropertyChanged(BR.retweetedStatus);
    }

    @Bindable
    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
    }

    @Bindable
    public StatusModel getQuotedStatus() {
        return quotedStatus;
    }

    public void setQuotedStatus(StatusModel quotedStatus) {
        this.quotedStatus = quotedStatus;
        notifyPropertyChanged(BR.quotedStatus);
    }

    public JsonObject getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(JsonObject geoLocation) {
        this.geoLocation = geoLocation;
    }

    public JsonObject getPlace() {
        return place;
    }

    public void setPlace(JsonObject place) {
        this.place = place;
    }
}

package solonsky.signal.twitter.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.room.models.UserEntity;

/**
 * Created by neura on 26.06.17.
 */

public class User extends BaseObservable implements Parcelable {
    public static final String TAG = User.class.getSimpleName();

    public interface UserClickHandler {
        void onItemClick(View v);
    }

    private long id;
    private long favouritesCount;
    private long followersCount;
    private long friendsCount;
    private long listedCount;
    private long statusesCount;

    private String createdAt;
    private String description;
    private String lang;
    private String location;
    private String name;
    private String screenName;
    private String url;

    private String biggerProfileImageURL;
    private String originalProfileImageURL;

    private String profileBackgroundImageUrl;
    private String profileBannerImageUrl;
    private String profileImageUrl;
    private String profileLinkColor;

    private JsonArray descriptionUrlEntities;
    private JsonObject urlEntity;

    private boolean isContributorsEnabled;
    private boolean isDefaultProfile;
    private boolean isDefaultProfileImage;
    private boolean isFollowRequestSent;
    private boolean isGeoEnabled;
    @SerializedName("isProtected")
    private boolean hasProtected;
    private boolean isVerified;
    private boolean profileBackgroundTiled;

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(favouritesCount);
        dest.writeLong(followersCount);
        dest.writeLong(friendsCount);
        dest.writeLong(listedCount);
        dest.writeLong(statusesCount);

        dest.writeString(createdAt);
        dest.writeString(description);
        dest.writeString(lang);
        dest.writeString(location);
        dest.writeString(name);
        dest.writeString(screenName);
        dest.writeString(url);

        dest.writeString(biggerProfileImageURL);
        dest.writeString(originalProfileImageURL);

        dest.writeString(profileBackgroundImageUrl);
        dest.writeString(profileBannerImageUrl);
        dest.writeString(profileImageUrl);
        dest.writeString(profileLinkColor);

        dest.writeString(descriptionUrlEntities == null ? null : descriptionUrlEntities.toString());
        dest.writeString(urlEntity == null ? null : urlEntity.toString());

        dest.writeInt(isContributorsEnabled ? 1 : 0);
        dest.writeInt(isDefaultProfile ? 1 : 0);
        dest.writeInt(isDefaultProfileImage ? 1 : 0);
        dest.writeInt(isFollowRequestSent ? 1 : 0);
        dest.writeInt(isGeoEnabled ? 1 : 0);
        dest.writeInt(hasProtected ? 1 : 0);
        dest.writeInt(isVerified ? 1 : 0);
        dest.writeInt(profileBackgroundTiled ? 1 : 0);
    }

    public static User getFromUserInstance(twitter4j.User source) {
        Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        User convertedUser = gson.fromJson(gson.toJsonTree(source), User.class);
        convertedUser.setBiggerProfileImageURL(source.getBiggerProfileImageURL());
        convertedUser.setOriginalProfileImageURL(source.getOriginalProfileImageURL());
        convertedUser.setDescriptionUrlEntities(gson.toJsonTree(source.getDescriptionURLEntities()).getAsJsonArray());
        return convertedUser;
    }

    public User(Parcel parcel) {
        this.id = parcel.readLong();
        this.favouritesCount = parcel.readLong();
        this.followersCount = parcel.readLong();
        this.friendsCount = parcel.readLong();
        this.listedCount = parcel.readLong();
        this.statusesCount = parcel.readLong();

        this.createdAt = parcel.readString();
        this.description = parcel.readString();
        this.lang = parcel.readString();
        this.location = parcel.readString();
        this.name = parcel.readString();
        this.screenName = parcel.readString();
        this.url = parcel.readString();

        this.biggerProfileImageURL = parcel.readString();
        this.originalProfileImageURL = parcel.readString();

        this.profileBackgroundImageUrl = parcel.readString();
        this.profileBannerImageUrl = parcel.readString();
        this.profileImageUrl = parcel.readString();
        this.profileLinkColor = parcel.readString();

        this.descriptionUrlEntities = new Gson().fromJson(parcel.readString(), JsonArray.class);
        this.urlEntity = new Gson().fromJson(parcel.readString(), JsonObject.class);

        this.isContributorsEnabled = parcel.readInt() == 1;
        this.isDefaultProfile = parcel.readInt() == 1;
        this.isDefaultProfileImage = parcel.readInt() == 1;
        this.isFollowRequestSent = parcel.readInt() == 1;
        this.isGeoEnabled = parcel.readInt() == 1;
        this.hasProtected = parcel.readInt() == 1;
        this.isVerified = parcel.readInt() == 1;
        this.profileBackgroundTiled = parcel.readInt() == 1;
    }

    public User() {

    }

    public User(User user) {
        this.id = user.id;
        this.favouritesCount = user.favouritesCount;
        this.followersCount = user.followersCount;
        this.friendsCount = user.friendsCount;
        this.listedCount = user.listedCount;
        this.statusesCount = user.statusesCount;
        this.createdAt = user.createdAt;
        this.description = user.description;
        this.lang = user.lang;
        this.location = user.location;
        this.name = user.name;
        this.screenName = user.screenName;
        this.url = user.url;
        this.biggerProfileImageURL = user.biggerProfileImageURL;
        this.profileBackgroundImageUrl = user.profileBackgroundImageUrl;
        this.profileBannerImageUrl = user.profileBannerImageUrl;
        this.profileImageUrl = user.profileImageUrl;
        this.profileLinkColor = user.profileLinkColor;
        this.descriptionUrlEntities = user.descriptionUrlEntities;
        this.urlEntity = user.urlEntity;
        this.isContributorsEnabled = user.isContributorsEnabled;
        this.isDefaultProfile = user.isDefaultProfile;
        this.isDefaultProfileImage = user.isDefaultProfileImage;
        this.isFollowRequestSent = user.isFollowRequestSent;
        this.isGeoEnabled = user.isGeoEnabled;
        this.hasProtected = user.isHasProtected();
        this.isVerified = user.isVerified;
        this.profileBackgroundTiled = user.profileBackgroundTiled;
    }

    @NotNull
    public static User createInstance(@NotNull UserEntity userEntity) {
        User user = new User();
        user.setId(userEntity.getId());

        user.setStatusesCount(userEntity.getStatusesCount());
        user.setFavouritesCount(userEntity.getFavouritesCount());
        user.setFollowersCount(userEntity.getFollowersCount());
        user.setFriendsCount(userEntity.getFriendsCount());
        user.setListedCount(userEntity.getListedCount());

        user.setOriginalProfileImageURL(userEntity.getOriginalProfileImageURL());
        user.setBiggerProfileImageURL(userEntity.getBiggerProfileImageURL());
        user.setProfileImageUrl(userEntity.getProfileImageUrl());
        user.setProfileBannerImageUrl(userEntity.getProfileBannerImageUrl());
        user.setProfileBackgroundImageUrl(userEntity.getProfileBackgroundImageUrl());

        user.setLocation(userEntity.getLocation());
        user.setCreatedAt(userEntity.getCreatedAt());

        user.setScreenName(userEntity.getScreenName());
        user.setName(userEntity.getName());
        user.setDescription(userEntity.getDescription());

        user.setContributorsEnabled(userEntity.isContributorsEnabled());
        user.setDefaultProfile(userEntity.isDefaultProfile());
        user.setVerified(userEntity.isVerified());
        user.setProtected(userEntity.isHasProtected());
        user.setDefaultProfileImage(userEntity.isDefaultProfileImage());

        user.setDescriptionUrlEntities(new JsonArray());
        user.setUrlEntity(new JsonObject());
        user.setUrl("");

        return user;
    }

    public User(long id, long favouritesCount, long followersCount, long friendsCount, long listedCount,
                long statusesCount, String createdAt, String description, String lang, String location,
                String name, String screenName, String url, String biggerProfileImageURL, String profileBackgroundImageUrl,
                String profileBannerImageUrl, String profileImageUrl, String profileLinkColor,
                JsonArray descriptionUrlEntities, JsonObject urlEntity, boolean isContributorsEnabled, boolean isDefaultProfile,
                boolean isDefaultProfileImage, boolean isFollowRequestSent, boolean isGeoEnabled,
                boolean isProtected, boolean isVerified, boolean profileBackgroundTiled) {
        this.id = id;
        this.favouritesCount = favouritesCount;
        this.followersCount = followersCount;
        this.friendsCount = friendsCount;
        this.listedCount = listedCount;
        this.statusesCount = statusesCount;
        this.createdAt = createdAt;
        this.description = description;
        this.lang = lang;
        this.location = location;
        this.name = name;
        this.screenName = screenName;
        this.url = url;
        this.biggerProfileImageURL = biggerProfileImageURL;
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
        this.profileBannerImageUrl = profileBannerImageUrl;
        this.profileImageUrl = profileImageUrl;
        this.profileLinkColor = profileLinkColor;
        this.descriptionUrlEntities = descriptionUrlEntities;
        this.urlEntity = urlEntity;
        this.isContributorsEnabled = isContributorsEnabled;
        this.isDefaultProfile = isDefaultProfile;
        this.isDefaultProfileImage = isDefaultProfileImage;
        this.isFollowRequestSent = isFollowRequestSent;
        this.isGeoEnabled = isGeoEnabled;
        this.hasProtected = isProtected;
        this.isVerified = isVerified;
        this.profileBackgroundTiled = profileBackgroundTiled;
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
    public long getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(long favouritesCount) {
        this.favouritesCount = favouritesCount;
        notifyPropertyChanged(BR.favouritesCount);
    }

    @Bindable
    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
        notifyPropertyChanged(BR.followersCount);
    }

    @Bindable
    public long getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(long friendsCount) {
        this.friendsCount = friendsCount;
        notifyPropertyChanged(BR.friendsCount);
    }

    @Bindable
    public long getListedCount() {
        return listedCount;
    }

    public void setListedCount(long listedCount) {
        this.listedCount = listedCount;
        notifyPropertyChanged(BR.listedCount);
    }

    @Bindable
    public long getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(long statusesCount) {
        this.statusesCount = statusesCount;
        notifyPropertyChanged(BR.statusesCount);
    }

    @Bindable
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        notifyPropertyChanged(BR.createdAt);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
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
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        notifyPropertyChanged(BR.location);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
        notifyPropertyChanged(BR.screenName);
    }

    @Bindable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        notifyPropertyChanged(BR.url);
    }

    @Bindable
    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
        notifyPropertyChanged(BR.profileBackgroundImageUrl);
    }

    @Bindable
    public String getProfileBannerImageUrl() {
        return profileBannerImageUrl;
    }

    public void setProfileBannerImageUrl(String profileBannerImageUrl) {
        this.profileBannerImageUrl = profileBannerImageUrl;
        notifyPropertyChanged(BR.profileBannerImageUrl);
    }

    @Bindable
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        notifyPropertyChanged(BR.profileImageUrl);
    }

    @Bindable
    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    public void setProfileLinkColor(String profileLinkColor) {
        this.profileLinkColor = profileLinkColor;
        notifyPropertyChanged(BR.profileLinkColor);
    }

    @Bindable
    public JsonArray getDescriptionUrlEntities() {
        return descriptionUrlEntities;
    }

    public void setDescriptionUrlEntities(JsonArray descriptionUrlEntities) {
        this.descriptionUrlEntities = descriptionUrlEntities;
        notifyPropertyChanged(BR.descriptionUrlEntities);
    }

    @Bindable
    public boolean isContributorsEnabled() {
        return isContributorsEnabled;
    }

    public void setContributorsEnabled(boolean contributorsEnabled) {
        isContributorsEnabled = contributorsEnabled;
        notifyPropertyChanged(BR.contributorsEnabled);
    }

    @Bindable
    public boolean isDefaultProfile() {
        return isDefaultProfile;
    }

    public void setDefaultProfile(boolean defaultProfile) {
        isDefaultProfile = defaultProfile;
        notifyPropertyChanged(BR.defaultProfile);
    }

    @Bindable
    public boolean isDefaultProfileImage() {
        return isDefaultProfileImage;
    }

    public void setDefaultProfileImage(boolean defaultProfileImage) {
        isDefaultProfileImage = defaultProfileImage;
        notifyPropertyChanged(BR.defaultProfileImage);
    }

    @Bindable
    public boolean isFollowRequestSent() {
        return isFollowRequestSent;
    }

    public void setFollowRequestSent(boolean followRequestSent) {
        isFollowRequestSent = followRequestSent;
        notifyPropertyChanged(BR.followRequestSent);
    }

    @Bindable
    public boolean isGeoEnabled() {
        return isGeoEnabled;
    }

    public void setGeoEnabled(boolean geoEnabled) {
        isGeoEnabled = geoEnabled;
        notifyPropertyChanged(BR.geoEnabled);
    }

    @Bindable
    public boolean isHasProtected() {
        return hasProtected;
    }

    public void setProtected(boolean hasProtected) {
        hasProtected = hasProtected;
        notifyPropertyChanged(BR.hasProtected);
    }

    @Bindable
    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
        notifyPropertyChanged(BR.verified);
    }

    @Bindable
    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTiled;
    }

    public void setProfileBackgroundTiled(boolean profileBackgroundTiled) {
        this.profileBackgroundTiled = profileBackgroundTiled;
        notifyPropertyChanged(BR.profileBackgroundTiled);
    }

    @Bindable
    public String getBiggerProfileImageURL() {
        return biggerProfileImageURL;
    }

    public void setBiggerProfileImageURL(String biggerProfileImageURL) {
        this.biggerProfileImageURL = biggerProfileImageURL;
        notifyPropertyChanged(BR.biggerProfileImageURL);
    }

    public JsonObject getUrlEntity() {
        return urlEntity;
    }

    public void setUrlEntity(JsonObject urlEntity) {
        this.urlEntity = urlEntity;
    }

    public String getOriginalProfileImageURL() {
        return originalProfileImageURL;
    }

    public void setOriginalProfileImageURL(String originalProfileImageURL) {
        this.originalProfileImageURL = originalProfileImageURL;
    }


}

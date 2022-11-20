package solonsky.signal.twitter.room.models;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import solonsky.signal.twitter.room.RoomContract;
import twitter4j.User;

/**
 * Created by sunwi on 22.01.2018.
 */
@Entity(tableName = RoomContract.USER_TABLE)
public class UserEntity {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "favorites_count") private long favouritesCount;
    @ColumnInfo(name = "followers_count") private long followersCount;
    @ColumnInfo(name = "friends_count") private long friendsCount;
    @ColumnInfo(name = "listed_count") private long listedCount;
    @ColumnInfo(name = "statuses_count") private long statusesCount;

    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "screen_name") private String screenName;
    @ColumnInfo(name = "bigger_profile_image_url") private String biggerProfileImageURL;
    @ColumnInfo(name = "original_profile_image_url") private String originalProfileImageURL;

    @ColumnInfo(name = "created_at") private String createdAt;
    @ColumnInfo(name = "description") private String description;
    @ColumnInfo(name = "lang") private String lang;
    @ColumnInfo(name = "location") private String location;

    @ColumnInfo(name = "profile_background_image_url") private String profileBackgroundImageUrl;
    @ColumnInfo(name = "profile_banner_image_url") private String profileBannerImageUrl;
    @ColumnInfo(name = "profile_image_url") private String profileImageUrl;
    @ColumnInfo(name = "profile_link_color") private String profileLinkColor;

    @ColumnInfo(name = "is_contributors_enabled") private boolean isContributorsEnabled;
    @ColumnInfo(name = "is_default_profile") private boolean isDefaultProfile;
    @ColumnInfo(name = "is_default_profile_image") private boolean isDefaultProfileImage;
    @ColumnInfo(name = "is_follow_request_sent") private boolean isFollowRequestSent;
    @ColumnInfo(name = "is_geo_enabled") private boolean isGeoEnabled;
    @ColumnInfo(name = "has_protected") private boolean hasProtected;
    @ColumnInfo(name = "is_verified") private boolean isVerified;
    @ColumnInfo(name = "profile_background_tiled") private boolean profileBackgroundTiled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(long favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(long friendsCount) {
        this.friendsCount = friendsCount;
    }

    public long getListedCount() {
        return listedCount;
    }

    public void setListedCount(long listedCount) {
        this.listedCount = listedCount;
    }

    public long getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(long statusesCount) {
        this.statusesCount = statusesCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getBiggerProfileImageURL() {
        return biggerProfileImageURL;
    }

    public void setBiggerProfileImageURL(String biggerProfileImageURL) {
        this.biggerProfileImageURL = biggerProfileImageURL;
    }

    public String getOriginalProfileImageURL() {
        return originalProfileImageURL;
    }

    public void setOriginalProfileImageURL(String originalProfileImageURL) {
        this.originalProfileImageURL = originalProfileImageURL;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
    }

    public String getProfileBannerImageUrl() {
        return profileBannerImageUrl;
    }

    public void setProfileBannerImageUrl(String profileBannerImageUrl) {
        this.profileBannerImageUrl = profileBannerImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    public void setProfileLinkColor(String profileLinkColor) {
        this.profileLinkColor = profileLinkColor;
    }

    public boolean isContributorsEnabled() {
        return isContributorsEnabled;
    }

    public void setContributorsEnabled(boolean contributorsEnabled) {
        isContributorsEnabled = contributorsEnabled;
    }

    public boolean isDefaultProfile() {
        return isDefaultProfile;
    }

    public void setDefaultProfile(boolean defaultProfile) {
        isDefaultProfile = defaultProfile;
    }

    public boolean isDefaultProfileImage() {
        return isDefaultProfileImage;
    }

    public void setDefaultProfileImage(boolean defaultProfileImage) {
        isDefaultProfileImage = defaultProfileImage;
    }

    public boolean isFollowRequestSent() {
        return isFollowRequestSent;
    }

    public void setFollowRequestSent(boolean followRequestSent) {
        isFollowRequestSent = followRequestSent;
    }

    public boolean isGeoEnabled() {
        return isGeoEnabled;
    }

    public void setGeoEnabled(boolean geoEnabled) {
        isGeoEnabled = geoEnabled;
    }

    public boolean isHasProtected() {
        return hasProtected;
    }

    public void setHasProtected(boolean hasProtected) {
        this.hasProtected = hasProtected;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isProfileBackgroundTiled() {
        return profileBackgroundTiled;
    }

    public void setProfileBackgroundTiled(boolean profileBackgroundTiled) {
        this.profileBackgroundTiled = profileBackgroundTiled;
    }

    @NotNull
    public static UserEntity createInstance(@NotNull User fromApi) {
        UserEntity entity = new UserEntity();
        entity.createdAt = fromApi.getCreatedAt().toString();

        entity.id = fromApi.getId();
        entity.biggerProfileImageURL = fromApi.getBiggerProfileImageURL();
        entity.originalProfileImageURL = fromApi.getOriginalProfileImageURL();

        entity.favouritesCount = fromApi.getFavouritesCount();
        entity.followersCount = fromApi.getFollowersCount();
        entity.statusesCount = fromApi.getStatusesCount();
        entity.listedCount = fromApi.getListedCount();

        entity.description = fromApi.getDescription();
        entity.createdAt = new DateTime(fromApi.getCreatedAt()).toString("dd.MM.yyyy HH:mm:ss");
        entity.location = fromApi.getLocation();

        entity.isVerified = fromApi.isVerified();

        entity.name = fromApi.getName();
        entity.screenName = fromApi.getScreenName();
        return entity;
    }
}


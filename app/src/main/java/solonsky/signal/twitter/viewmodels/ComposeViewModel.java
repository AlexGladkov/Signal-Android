package solonsky.signal.twitter.viewmodels;

import android.Manifest;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.location.Location;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import solonsky.signal.twitter.adapters.ImageHorizontalAdapter;
import solonsky.signal.twitter.adapters.SimpleHorizontalAdapter;
import solonsky.signal.twitter.adapters.UserHorizontalAdapter;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Permission;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.models.SimpleModel;
import solonsky.signal.twitter.models.UserModel;
import twitter4j.AsyncTwitter;
import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by neura on 22.05.17.
 * View model class for
 *
 * @link .ComposeActivity
 */
public class ComposeViewModel extends BaseObservable {
    public static int LOCATION_ON = 0;
    public static int LOCATION_OFF = 1;
    public static int LOCATION_DISABLED = 2;

    /* Mentions block */
    private ArrayList<UserModel> mentionsSource;
    private ArrayList<UserModel> mentionsFiltered;
    private UserHorizontalAdapter mentionsAdapter;

    /* HashTags block */
    private ArrayList<SimpleModel> hashtagsSource;
    private ArrayList<SimpleModel> hashtagsFiltered;
    private SimpleHorizontalAdapter hashtagsAdapter;

    /* Images block */
    private List<File> imagesSource;
    private ArrayList<ImageModel> imagesFiltered;
    private ImageHorizontalAdapter imagesAdapter;

    private final String TAG = ComposeViewModel.class.getSimpleName();
    private int locationState;
    private int mediaCount;
    private boolean isEnabled;
    private boolean isMore;
    private boolean isFragment;

    private boolean hasGif = false;
    private boolean hasVideo = false;
    private boolean fromDraft = false;

    private boolean isShowMentions;
    private boolean isShowHashtags;
    private boolean isShowQuote;
    private boolean isShowReply;
    private boolean isShowPhoto;

    private boolean isText;
    private boolean isLibrary;

    private ListConfig listConfig;
    private ListConfig imageConfig;
    private ListConfig hashtagsConfig;
    private ListConfig mentionsConfig;

    private String title;
    private String tweetText;
    private String currentTweetText;
    private String avatarUrl;
    private String location;
    private String clientToken;
    private String clientSecret;
    private AppCompatActivity mActivity;

    public interface ComposeClickHandler {
        void onUserClick(View view);

        void onSendClick(View view);

        void onGeoClick(View view);

        void onMentionsClick(View view);

        void onCameraClick(View view);

        void onHashClick(View view);

        void onDraftsClick(View view);

        void onBackClick(View view);

        boolean onCameraLongClick(View view);
    }

    public ComposeViewModel(String title, String avatarUrl, String location, AppCompatActivity mActivity,
                            ArrayList<UserModel> mentionsSource) {
        this.title = title;
        this.mActivity = mActivity;
        this.avatarUrl = avatarUrl;
        this.location = location;
        this.currentTweetText = "";
        this.isEnabled = false;
        this.isMore = false;
        this.isFragment = false;
        this.isShowMentions = false;
        this.isShowHashtags = false;
        this.isShowQuote = false;
        this.isShowReply = false;
        this.isShowPhoto = false;
        this.isLibrary = true;
        this.listConfig = null;
        this.mediaCount = 0;
        this.locationState = LOCATION_OFF;

        this.hashtagsSource = new ArrayList<>();
        this.hashtagsFiltered = new ArrayList<>();
        this.hashtagsAdapter = new SimpleHorizontalAdapter(hashtagsFiltered, mActivity.getApplicationContext(),
                hashClickHandler);

        this.mentionsSource = mentionsSource;
        this.mentionsFiltered = new ArrayList<>();
        this.mentionsAdapter = new UserHorizontalAdapter(mentionsFiltered, mActivity.getApplicationContext(),
                mActivity, mentionsClickHandler);

        this.imagesSource = new ArrayList<>();
        this.imagesFiltered = new ArrayList<>();
        this.imagesAdapter = new ImageHorizontalAdapter(imagesFiltered, mActivity,
                imageClickHandler);

        this.clientSecret = AppData.CLIENT_SECRET;
        this.clientToken = AppData.CLIENT_TOKEN;

        imageConfig = new ListConfig.Builder(imagesAdapter)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(false)
                .setLayoutManagerProvider(new ListConfig.SimpleHorizontalLayoutManagerProvider())
                .build(mActivity.getApplicationContext());

        mentionsAdapter.setHasStableIds(true);
        mentionsConfig = new ListConfig.Builder(mentionsAdapter)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(false)
                .setLayoutManagerProvider(new ListConfig.SimpleHorizontalLayoutManagerProvider())
                .build(mActivity.getApplicationContext());

        hashtagsConfig = new ListConfig.Builder(hashtagsAdapter)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(false)
                .setLayoutManagerProvider(new ListConfig.SimpleHorizontalLayoutManagerProvider())
                .build(mActivity.getApplicationContext());

        loadHashtags();
        loadRecentMedia();
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isChanged = true;
        int length = s.toString().replaceAll(Patterns.WEB_URL.pattern(), "").length();
        setMore(AppData.TWITTER_RESTRICT < length);
        setEnabled(AppData.TWITTER_RESTRICT >= length && (length > 0 || mediaCount > 0));

        String title = mActivity.getString(R.string.compose_default_title);

        if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_QUOTE) {
            title = mActivity.getString(R.string.compose_title_quote);
        } else if (Flags.CURRENT_COMPOSE == Flags.COMPOSE_REPLY) {
            title = mActivity.getString(R.string.compose_title_reply);
        }

        setTitle(length == 0 ? title : String.valueOf(AppData.TWITTER_RESTRICT - length));
        setText(length != 0);
        setCurrentTweetText(s.toString());
    }

    public String getCurrentTweetText() {
        return currentTweetText;
    }

    public void setCurrentTweetText(String currentTweetText) {
        this.currentTweetText = currentTweetText;
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

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public ImageHorizontalAdapter getImagesAdapter() {
        return imagesAdapter;
    }

    public ArrayList<ImageModel> getImagesFiltered() {
        return imagesFiltered;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
        notifyPropertyChanged(BR.hasVideo);
    }

    public void setHasGif(boolean hasGif) {
        this.hasGif = hasGif;
        notifyPropertyChanged(BR.hasGif);
    }

    public boolean isFromDraft() {
        return fromDraft;
    }

    public void setFromDraft(boolean fromDraft) {
        this.fromDraft = fromDraft;
    }

    @Bindable
    public boolean isHasGif() {
        return hasGif;
    }

    @Bindable
    public boolean isHasVideo() {
        return hasVideo;
    }

    @Bindable
    public List<File> getImagesSource() {
        return imagesSource;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
        notifyPropertyChanged(BR.tweetText);
    }

    @Bindable
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
        notifyPropertyChanged(BR.more);
    }

    @Bindable
    public boolean isFragment() {
        return isFragment;
    }

    public void setFragment(boolean fragment) {
        isFragment = fragment;
        notifyPropertyChanged(BR.fragment);
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public boolean isShowMentions() {
        return isShowMentions;
    }

    public void setShowMentions(boolean showMentions) {
        isShowMentions = showMentions;
        notifyPropertyChanged(BR.showMentions);
    }

    @Bindable
    public boolean isShowHashtags() {
        return isShowHashtags;
    }

    public void setShowHashtags(boolean showHashtags) {
        isShowHashtags = showHashtags;
        notifyPropertyChanged(BR.showHashtags);
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
    public ListConfig getListConfig() {
        return listConfig;
    }

    public void setListConfig(ListConfig listConfig) {
        this.listConfig = listConfig;
        notifyPropertyChanged(BR.listConfig);
    }

    @Bindable
    public int getLocationState() {
        return locationState;
    }

    public void setLocationState(int locationState) {
        this.locationState = locationState;
        notifyPropertyChanged(BR.locationState);
    }

    @Bindable
    public boolean isShowQuote() {
        return isShowQuote;
    }

    public void setShowQuote(boolean showQuote) {
        isShowQuote = showQuote;
        notifyPropertyChanged(BR.showQuote);
    }

    @Bindable
    public boolean isText() {
        return isText;
    }

    public void setText(boolean text) {
        isText = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public boolean isShowReply() {
        return isShowReply;
    }

    public void setShowReply(boolean showReply) {
        isShowReply = showReply;
        notifyPropertyChanged(BR.showReply);
    }

    @Bindable
    public boolean isLibrary() {
        return isLibrary;
    }

    public void setLibrary(boolean library) {
        isLibrary = library;
        notifyPropertyChanged(BR.library);
    }

    @Bindable
    public boolean isShowPhoto() {
        return isShowPhoto;
    }

    public void setShowPhoto(boolean showPhoto) {
        isShowPhoto = showPhoto;
        notifyPropertyChanged(BR.showPhoto);
    }

    @Bindable
    public ListConfig getImageConfig() {
        return imageConfig;
    }

    @Bindable
    public ListConfig getHashtagsConfig() {
        return hashtagsConfig;
    }

    @Bindable
    public ListConfig getMentionsConfig() {
        return mentionsConfig;
    }

     /*
      * ============================================
      * == This section starts a handlers section ==
      * ============================================
      */

    ImageHorizontalAdapter.ImageClickListener imageClickHandler = new ImageHorizontalAdapter.ImageClickListener() {
        @Override
        public void onItemClick(View v, ImageModel model) {
            final File imageFile = new File(model.getImageUrl());
            if (imageFile.canRead() && imageFile.exists()) {
                if (!model.isSelected()) {
                    if (imagesSource.size() > 0 && model.getMediaType().equals(Flags.MEDIA_TYPE.GIF)) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_multiple_image),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (imagesSource.size() > 0 && model.getMediaType().equals(Flags.MEDIA_TYPE.VIDEO)) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_multiple_image),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (imagesSource.size() > 3 || hasGif || hasVideo) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_multiple_image),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setHasGif(model.getMediaType().equals(Flags.MEDIA_TYPE.GIF));
                    setHasVideo(model.getMediaType().equals(Flags.MEDIA_TYPE.VIDEO));
                    model.setSelectNumber(imagesSource.size() + 1);
                    imagesSource.add(imageFile);
                    model.setSelected(true);
                } else {
                    if (model.getSelectNumber() == imagesSource.size()) {
                        model.setSelectNumber(model.getSelectNumber() - 1);
                    } else {
                        for (ImageModel imageModel : imagesFiltered) {
                            if (imageModel.getSelectNumber() > model.getSelectNumber()) {
                                imageModel.setSelectNumber(imageModel.getSelectNumber() - 1);
                            }
                        }
                    }


                    setHasGif(false);
                    setHasVideo(false);
                    model.setSelected(false);

                    File searched = null;
                    for (File file : imagesSource) {
                        if (file.getAbsolutePath().equals(model.getImageUrl())) {
                            searched = file;
                        }
                    }

                    if (searched != null) imagesSource.remove(searched);
                }

                setMediaCount(imagesSource.size());
                if (getMediaCount() > 0 ||
                        (currentTweetText.length() > 0 && currentTweetText.length() < AppData.TWITTER_RESTRICT)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } else {
                Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_create_file),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private SimpleHorizontalAdapter.SimpleClickListener hashClickHandler = new SimpleHorizontalAdapter.SimpleClickListener() {
        @Override
        public void onItemClick(View v, SimpleModel model) {
            boolean hasHash = false;

            for (SimpleModel simpleModel : hashtagsSource) {
                if (simpleModel.getTitle().toLowerCase().equals(model.getTitle().toLowerCase())) {
                    hasHash = true;
                    break;
                }
            }

            if (!hasHash) hashtagsSource.add(model);

            String[] strings = currentTweetText.split(" ");
            String lastWord = strings.length > 0 ? strings[strings.length - 1] : "";

            if (lastWord.contains("#")) {
                if (Utilities.checkLastCharIsSpace(currentTweetText)) {
                    setTweetText(currentTweetText + model.getTitle() + " ");
                } else {
                    if (lastWord.equals("#")) {
                        setTweetText(currentTweetText + model.getTitle().replace("#", "") + " ");
                    } else {
                        setTweetText(currentTweetText.replace(lastWord, model.getTitle()) + " ");
                    }
                }
            }

            setShowHashtags(false);
        }
    };

    private boolean isChanged = false;
    private UserHorizontalAdapter.UserClickHandler mentionsClickHandler = new UserHorizontalAdapter.UserClickHandler() {
        @Override
        public void onItemClick(UserModel model, View v) {
            String[] strings = currentTweetText.split(" ");
            String lastWord = strings.length > 0 ? strings[strings.length - 1] : "";

            if (lastWord.contains("@")) {
                if (Utilities.checkLastCharIsSpace(currentTweetText)) {
                    setTweetText((currentTweetText + model.getTwitterName() + " "));
                } else {
                    if (lastWord.equals("@")) {
                        setTweetText((currentTweetText + model.getTwitterName().replace("@", "") + " "));
                    } else {
                        setTweetText((currentTweetText.replace(lastWord, model.getTwitterName()) + " "));
                    }
                }
            }

            setShowMentions(false);
        }

        @Override
        public void onSearchClick(final UserModel model, View v) {
            final ImageView imageView = (ImageView) v.findViewById(R.id.user_search_img);
            final CircularProgressView cpvView = (CircularProgressView) v.findViewById(R.id.user_search_cpv);
            isChanged = false;

            cpvView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            String[] strings = currentTweetText.split(" ");
            final String lastWord = strings.length > 0 ? strings[strings.length - 1] : "";

            final Handler handler = new Handler();
            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.searchUsers(lastWord.replace("@", ""), 1);
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void onException(TwitterException te, TwitterMethod method) {
                    super.onException(te, method);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isChanged) {
                                cpvView.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                                model.setUsername(mActivity.getString(R.string.no_compose_search));
                                mentionsAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

                @Override
                public void searchedUser(final ResponseList<User> userList) {
                    super.searchedUser(userList);
                    Log.e(TAG, "found users - " + userList.size());
                    if (!isChanged) {
                        for (User user : userList) {
                            mentionsSource.add(new UserModel(user.getId(), user.getOriginalProfileImageURL(),
                                    user.getName(), "@" + user.getScreenName(), false, false, false));
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (userList.size() == 0) {
                                    cpvView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                    model.setUsername(mActivity.getString(R.string.no_compose_search));
                                    mentionsAdapter.notifyDataSetChanged();
                                } else {
                                    filterMentions(lastWord, true);
                                }
                            }
                        });
                    }
                }
            });
        }
    };

    /*
      * ==============================================
      * == This section starts a data logic section ==
      * ==============================================
    */

    public ImageModel checkImage(String path) {
        ImageModel hasImage = null;

        for (ImageModel imageModel : imagesFiltered) {
            if (imageModel.getImageUrl().toLowerCase().equals(path.toLowerCase())) {
                hasImage = imageModel;
                break;
            }
        }

        return hasImage;
    }

    public int addMedia(File file, Flags.MEDIA_TYPE mediaType) {
        imagesSource.add(0, file);

        ImageModel realModel = checkImage(file.getAbsolutePath());
//        if (realModel == null) {
        ImageModel newImage = new ImageModel(file.getAbsolutePath());
        newImage.setSelected(true);
        newImage.setSelectNumber(imagesSource.size());
        newImage.setMediaType(mediaType);

        imagesFiltered.add(0, newImage);
        imagesAdapter.notifyItemInserted(0);
        setEnabled(true);
        return 0;
//        } else {
//            realModel.setSelected(true);
//            realModel.setSelectNumber(imagesSource.size());
//            imagesAdapter.notifyDataSetChanged();
//            return imagesFiltered.indexOf(realModel);
//        }
    }

    /**
     * Sort hashtags for showUp
     *
     * @param lastWord - filtered word
     * @param isShow   - @true for showing panel
     */
    public void filterHashtags(String lastWord, boolean isShow) {
        hashtagsFiltered.clear();

        for (SimpleModel simpleModel : hashtagsSource) {
            if (simpleModel.getTitle().toLowerCase().contains(lastWord.toLowerCase().replace("@", ""))) {
                hashtagsFiltered.add(simpleModel);
            }
        }

        if (hashtagsFiltered.size() == 0)
            hashtagsFiltered.add(new SimpleModel(0, lastWord));

        if (hashtagsFiltered.size() != 0 && isShow) {
            setShowHashtags(true);
            setShowMentions(false);
            hashtagsAdapter.notifyDataSetChanged();
        } else {
            setShowHashtags(false);
        }
    }

    /**
     * Sort mentions to setup it
     *
     * @param lastWord - filter stringx
     */
    public void filterMentions(final String lastWord, final boolean isShow) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<UserModel> temp = new ArrayList<>();
                for (UserModel userModel : mentionsSource) {
                    if (userModel.getTwitterName().toLowerCase().contains(lastWord.toLowerCase().replace("@", ""))) {
                        temp.add(userModel);
                    }
                }

                if (mentionsFiltered.size() > 0 && mentionsFiltered.get(0).getUsername()
                        .equals(mActivity.getString(R.string.compose_find_user)) && temp.size() == 0)
                    return;
                mentionsFiltered.clear();

                for (UserModel userModel : mentionsSource) {
                    if (!mentionsFiltered.contains(userModel) &&
                            userModel.getTwitterName().toLowerCase().contains(lastWord.toLowerCase().replace("@", ""))) {
                        mentionsFiltered.add(userModel);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mentionsFiltered.size() != 0 && isShow) {
                            setShowMentions(true);
                            setShowHashtags(false);
                            mentionsAdapter.notifyDataSetChanged();
                        } else if (isShow) {
                            UserModel userModel = new UserModel(0, "wrongImage", mActivity.getString(R.string.compose_find_user),
                                    "", true, false, false);
                            mentionsFiltered.add(userModel);
                            setShowMentions(true);
                            setShowHashtags(false);
                            mentionsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();
    }

    public void loadRecentMedia() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };

        if (Permission.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final Cursor cursor = mActivity.getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                            null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            ArrayList<String> pathes = new ArrayList<>();
            for (ImageModel imageModel : imagesFiltered) {
                pathes.add(imageModel.getImageUrl());
            }

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    String type = imagePath == null ? "photo" : imagePath.substring(imagePath.lastIndexOf(".") > 0
                            ? imagePath.lastIndexOf(".") : 0);
                    if (!pathes.contains(imagePath)) {
                        ImageModel imageModel = new ImageModel(imagePath);
                        switch (type) {
                            case ".mp4":
                            case ".avi":
                                imageModel.setMediaType(Flags.MEDIA_TYPE.VIDEO);
                                break;

                            case ".gif":
                                imageModel.setMediaType(Flags.MEDIA_TYPE.GIF);
                                break;

                            default:
                                imageModel.setMediaType(Flags.MEDIA_TYPE.IMAGE);
                                break;
                        }

                        imagesFiltered.add(imageModel);
                    }
                }
            }
        }

        imagesAdapter.notifyDataSetChanged();
    }

    /**
     * Load hashtags from server
     */
    private void loadHashtags() {
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotAvailableTrends(ResponseList<twitter4j.Location> locations) {
                super.gotAvailableTrends(locations);
                if (locations.size() > 0) {
                    asyncTwitter.getPlaceTrends(locations.get(0).getWoeid());
                }
            }

            @Override
            public void gotPlaceTrends(Trends trends) {
                super.gotPlaceTrends(trends);
                Log.e(TAG, "success loaded trends - " + trends.getTrends().length);
                for (Trend trend : trends.getTrends()) {
                    if (trend.getName().contains("#") && !Cache.trends.contains(trend.getName())) {
                        Cache.trends.add(trend.getName());
                    }
                }

                for (int i = 0; i < Cache.trends.size(); i++) {
                    hashtagsSource.add(new SimpleModel(i, Cache.trends.get(i)));
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hashtagsAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error loading hashes - " + te.getLocalizedMessage());
            }
        });

        asyncTwitter.getPlaceTrends(1);
        Log.e(TAG, "start loading hashes");
    }

    /**
     * Add location to compose
     *
     * @param currentLocation - last known location
     */
    public void addLocation(final Location currentLocation) {
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void searchedPlaces(ResponseList<Place> places) {
                super.searchedPlaces(places);
                if (places.size() > 0) {
                    setLocation(places.get(0).getName() + ", " +
                            places.get(0).getCountry());
                    setLocationState(ComposeViewModel.LOCATION_ON);
                }
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                if (currentLocation != null) {
                    setLocation("GPS: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    setLocationState(ComposeViewModel.LOCATION_ON);
                } else {
                    setLocationState(ComposeViewModel.LOCATION_DISABLED);
                }

            }
        });

        if (currentLocation != null) {
            asyncTwitter.searchPlaces(new GeoQuery(new GeoLocation(currentLocation.getLatitude(),
                    currentLocation.getLongitude())));
        } else {
            Toast.makeText(mActivity.getApplicationContext(), "Location disabled", Toast.LENGTH_SHORT).show();
            setLocationState(LOCATION_OFF);
        }
    }
}

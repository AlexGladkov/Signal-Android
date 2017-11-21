package solonsky.signal.twitter.viewmodels;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.activities.MediaActivity;
import solonsky.signal.twitter.activities.SearchActivity;
import solonsky.signal.twitter.adapters.DetailStaggeredAdapter;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.overlays.ImageActionsOverlay;
import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 03.06.17.
 */
public class DetailViewModel extends BaseObservable {
    private static final String TAG = DetailViewModel.class.getSimpleName();
    private String userName;
    private String twitterName;
    private String avatar;
    private String text;
    private String location;
    private String date;
    private String client;

    private DetailActivity mActivity;

    private boolean isReply;
    private boolean isFollowed;
    private boolean isExpanded;
    private boolean isFavorite;
    private boolean isShowThread;
    private boolean isShowQuote;
    private boolean isLoaded = false;

    private int textMarginTop;
    private int dummyHeight;
    private int imageHeight;

    /* Images panel */
    private JsonArray imagesSource;
    private ArrayList<ImageModel> imagesFiltered;
//    private DetailStaggeredAdapter imagesAdapter;
    private ListConfig imagesConfig;

    /* Thread panel */
    private ArrayList<StatusModel> threadSource;
    private StatusAdapter threadAdapter;
    private ListConfig threadConfig;

    public interface DetailClickHandler {
        void onReplyClick(View v);

        void onRtClick(View v);

        void onLikeClick(View v);

        void onShareClick(View v);

        void onMoreClick(View v);

        void onThreadClick(View v);

        void onClientClick(View v);

        void onFollowClick(View v);

        void onQuoteMediaClick(View v);
    }

    public DetailViewModel(String userName, String twitterName, String avatar, String text,
                           String location, String date, String client, final DetailActivity mActivity,
                           boolean isFavorite, boolean isFollowed, boolean isReply, boolean isShowQuote, JsonArray imagesSource) {
        this.userName = userName;
        this.twitterName = twitterName;
        this.avatar = avatar;
        this.text = text;
        this.location = location;
        this.date = date;
        this.client = client;
        this.isFollowed = isFollowed;
        this.isFavorite = isFavorite;
        this.isExpanded = false;
        this.isShowThread = false;
        this.textMarginTop = (int) Utilities.convertDpToPixel(72, mActivity.getApplicationContext());
        this.imageHeight = (int) (Utilities.getScreenWidth(mActivity) * 0.617f);
        this.isShowQuote = isShowQuote;
        this.isReply = isReply;
        this.mActivity = mActivity;
        this.imagesSource = imagesSource;
        this.imagesFiltered = new ArrayList<>();



//        this.imagesAdapter = new DetailStaggeredAdapter(imagesFiltered, mActivity, imagesClickHandler);



//        this.imagesConfig = new ListConfig.Builder(imagesAdapter)
//                .setHasFixedSize(true)
//                .setDefaultDividerEnabled(false)
//                .setLayoutManagerProvider()
//                .addItemDecoration(new ListConfig.SpacesItemDecoration(
//                        (int) Utilities.convertDpToPixel(1, mActivity.getApplicationContext())))
////                .setLayoutManagerProvider(new ListConfig.SimpleGridLayoutManagerProvider(2))
//                .build(mActivity.getApplicationContext());

        this.threadSource = new ArrayList<>();
        this.threadAdapter = new StatusAdapter(threadSource, mActivity, false, false, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                AppData.searchQuery = searchText;
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), SearchActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {

            }
        });

        this.threadAdapter.setHasStableIds(true);
        ListConfig.SimpleLinearLayoutManagerProvider layoutManagerProvider =
                new ListConfig.SimpleLinearLayoutManagerProvider();
        RecyclerView.LayoutManager layoutManager = layoutManagerProvider.get(mActivity.getApplicationContext());
        layoutManager.setAutoMeasureEnabled(true);

        this.threadConfig = new ListConfig.Builder(threadAdapter)
                .setHasFixedSize(false)
                .setDefaultDividerEnabled(false)
                .setHasNestedScroll(false)
                .setLayoutManagerProvider(layoutManagerProvider)
                .build(mActivity.getApplicationContext());

        loadThread();
    }

    @Bindable
    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
        notifyPropertyChanged(BR.loaded);
    }

    @Bindable
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        notifyPropertyChanged(BR.favorite);
    }

    @Bindable
    public int getDummyHeight() {
        return dummyHeight;
    }

    public void setDummyHeight(int dummyHeight) {
        this.dummyHeight = dummyHeight;
        notifyPropertyChanged(BR.dummyHeight);
    }

    @Bindable
    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        notifyPropertyChanged(BR.imageHeight);
    }

    @Bindable
    public ArrayList<StatusModel> getThreadSource() {
        return threadSource;
    }

    public void setThreadSource(ArrayList<StatusModel> threadSource) {
        this.threadSource = threadSource;
        notifyPropertyChanged(BR.threadSource);
    }

    @Bindable
    public StatusAdapter getThreadAdapter() {
        return threadAdapter;
    }

    public void setThreadAdapter(StatusAdapter threadAdapter) {
        this.threadAdapter = threadAdapter;
        notifyPropertyChanged(BR.threadAdapter);
    }

    @Bindable
    public ListConfig getThreadConfig() {
        return threadConfig;
    }

    public void setThreadConfig(ListConfig threadConfig) {
        this.threadConfig = threadConfig;
        notifyPropertyChanged(BR.threadConfig);
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getTwitterName() {
        return twitterName;
    }

    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
        notifyPropertyChanged(BR.twitterName);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
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
    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
        notifyPropertyChanged(BR.followed);
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
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    @Bindable
    public int getTextMarginTop() {
        return textMarginTop;
    }

    public void setTextMarginTop(int textMarginTop) {
        this.textMarginTop = textMarginTop;
        notifyPropertyChanged(BR.textMarginTop);
    }

    @Bindable
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
        notifyPropertyChanged(BR.client);
    }

    @Bindable
    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean reply) {
        isReply = reply;
        notifyPropertyChanged(BR.reply);
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
    public ListConfig getImagesConfig() {
        return imagesConfig;
    }

    public void setImagesConfig(ListConfig imagesConfig) {
        this.imagesConfig = imagesConfig;
        notifyPropertyChanged(BR.imagesConfig);
    }

    @Bindable
    public JsonArray getImagesSource() {
        return imagesSource;
    }

    public void setImagesSource(JsonArray imagesSource) {
        this.imagesSource = imagesSource;
        notifyPropertyChanged(BR.imagesSource);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Bindable
    public boolean isShowThread() {
        return isShowThread;
    }

    public void setShowThread(boolean showThread) {
        isShowThread = showThread;
        notifyPropertyChanged(BR.showThread);
    }

    /*
      * ==================================================
      * == This section starts a click handlers section ==
      * ==================================================
    */

    /*
      * ==============================================
      * == This section starts a data logic section ==
      * ==============================================
    */

    private void loadThread() {
        final Gson gson = new Gson();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        final Handler handler = new Handler();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                if (threadSource.size() > 0) {
                    for (int i = 0; i < threadSource.size(); i++) {
                        threadSource.get(i).setDivideState((i == threadSource.size() - 1) ? Flags.DIVIDER_NONE :
                                Flags.DIVIDER_SHORT);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            threadAdapter.notifyDataSetChanged();
                            mActivity.measureThreadHeight();
                            setLoaded(true);
                        }
                    });
                }
            }

            @Override
            public void gotShowStatus(final Status status) {
                super.gotShowStatus(status);
                StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                statusModel.tuneModel(status);
                statusModel.linkClarify();
                threadSource.add(0, statusModel);
                notifyPropertyChanged(BR.threadSource);

                if (status.getInReplyToStatusId() > -1) {
                    asyncTwitter.showStatus(status.getInReplyToStatusId());
                } else {
                    for (int i = 0; i < threadSource.size(); i++) {
                        threadSource.get(i).setDivideState((i == threadSource.size() - 1) ? Flags.DIVIDER_NONE :
                                Flags.DIVIDER_SHORT);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            threadAdapter.notifyDataSetChanged();
                            mActivity.measureThreadHeight();
                            setLoaded(true);
                        }
                    });
                }
            }
        });

        Log.e(TAG, "start loading");
        asyncTwitter.showStatus(AppData.CURRENT_STATUS_MODEL.getInReplyToStatusId());
    }
}

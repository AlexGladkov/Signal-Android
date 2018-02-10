package solonsky.signal.twitter.viewmodels;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.MVPSearchActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Keys;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 19.05.17.
 * View model for
 *
 * @link to fragments.FeedFragment
 */

public class FeedViewModel extends BaseObservable {
    private static final String TAG = FeedViewModel.class.getSimpleName();
    private final AppCompatActivity mActivity;

    /* Feed panel */
    private StatusAdapter mFeedAdapter;
    private ListConfig feedConfig;

    private boolean isLoading = false;
    private boolean isLoadingNew = false;
    private boolean isRefresh = false;

    private int state;
    private long positionId;

    public interface FeedClickHandler {

    }

    public FeedViewModel(final AppCompatActivity mActivity, int state) {
        this.mActivity = mActivity;
        this.state = state;
        this.mFeedAdapter = new StatusAdapter(FeedData.getInstance().getFeedStatuses(), mActivity,
                true, true, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                Intent searchIntent = new Intent(mActivity.getApplicationContext(), MVPSearchActivity.class);
                searchIntent.putExtra(Keys.SearchQuery.getValue(), searchText);
                mActivity.startActivity(searchIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = FeedData.getInstance().getFeedStatuses().indexOf(statusModel);
                FeedData.getInstance().getFeedStatuses().remove(statusModel);
                FeedData.getInstance().saveCache(TAG);
                mFeedAdapter.notifyItemRemoved(position);
            }
        });
        this.positionId = 0;

        this.feedConfig = new ListConfig.Builder(mFeedAdapter)
                .setDefaultDividerEnabled(false)
                .setLayoutManagerProvider(new ListConfig.SpeedyLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .build(mActivity.getApplicationContext());

        if (FeedData.getInstance().getFeedStatuses().size() == 0) {
            loadCache();
        }
    }

    @Bindable
    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Bindable
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

    public boolean isLoadingNew() {
        return isLoadingNew;
    }

    public void setLoadingNew(boolean loadingNew) {
        isLoadingNew = loadingNew;
    }

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    @Bindable
    public StatusAdapter getmFeedAdapter() {
        return mFeedAdapter;
    }

    public void setmFeedAdapter(StatusAdapter mFeedAdapter) {
        this.mFeedAdapter = mFeedAdapter;
        notifyPropertyChanged(BR.mFeedAdapter);
    }

    @Bindable
    public ListConfig getFeedConfig() {
        return feedConfig;
    }

    public void setFeedConfig(ListConfig feedConfig) {
        this.feedConfig = feedConfig;
        notifyPropertyChanged(BR.feedConfig);
    }

     /*
      * ==============================================
      * == This section starts a data logic section ==
      * ==============================================
    */

    private void loadCache() {
        final Handler handler = new Handler();
        final long startTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Load cache " + AppData.ME.getId());
                Reservoir.getAsync(Cache.Feed + String.valueOf(AppData.ME.getId()), FeedData.class,
                        new ReservoirGetCallback<FeedData>() {
                            @Override
                            public void onSuccess(final FeedData feedData) {
                                for (StatusModel statusModel : feedData.getFeedStatuses()) {
                                    statusModel.parseYoutube();
                                }

                                Collections.sort(feedData.getFeedStatuses(), new Comparator<StatusModel>() {
                                    @Override
                                    public int compare(StatusModel o1, StatusModel o2) {
                                        return Long.compare(o2.getId(), o1.getId());
                                    }
                                });

                                if (feedData.getFeedStatuses().size() > 0) {
                                    FeedData.getInstance().setTopId(feedData.getFeedStatuses().get(0).getId());
                                }

                                int entryCount = 0;

//                                for (StatusModel statusModel : feedData.getFeedStatuses()) {
//                                    if (statusModel.isNewStatus()) {
//                                        entryCount = entryCount + 1;
//                                    }
//                                }

                                for (StatusModel statusModel : feedData.getFeedStatuses()) {
                                    if (!FeedData.getInstance().getFeedStatuses().contains(statusModel))
                                        FeedData.getInstance().getFeedStatuses().add(statusModel);
                                }

                                FeedData.getInstance().setScrollPosition(feedData.getScrollPosition());
                                FeedData.getInstance().setScrollTop(feedData.getScrollTop());
                                FeedData.getInstance().setCacheEntryCount(entryCount);
                                FeedData.getInstance().updateEntryCount();

                                LoggedData.getInstance().setNewFeed(entryCount > 0);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FeedData.getInstance().getUpdateHandler().onUpdate();
                                        LoggedData.getInstance().getUpdateHandler().onUpdate();
                                        loadNew();
                                    }
                                });

                            }

                            @Override
                            public void onFailure(Exception e) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadNew();
                                    }
                                });
                            }
                        });
            }
        }).start();
    }

    public void loadNext() {
        mFeedAdapter.setLoading(true);
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        final Handler handler = new Handler();
        final long maxId = FeedData.getInstance().getFeedStatuses().get(
                FeedData.getInstance().getFeedStatuses().size() - 1).getId();
        Paging paging = new Paging();
        paging.setMaxId(maxId);
        paging.setCount(50);

        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (te.getErrorMessage() != null) {
                            Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_loading_feed)
                                    + " because " + te.getErrorMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
                        }

                        mFeedAdapter.setLoading(false);
                        mFeedAdapter.notifyDataSetChanged();
                        setLoading(false);
                    }
                });
            }

            @Override
            public void gotHomeTimeline(ResponseList<Status> statuses) {
                super.gotHomeTimeline(statuses);
                for (Status status : statuses) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();
                    if (status.getId() < maxId) {
                        FeedData.getInstance().getFeedStatuses().add(statusModel);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        FeedData.getInstance().getUpdateHandler().onUpdate();

                        mFeedAdapter.setLoading(false);
                        mFeedAdapter.notifyDataSetChanged();
                        setLoading(false);
                    }
                });
            }
        });

        asyncTwitter.getHomeTimeline(paging);
    }

    public void loadNew() {
        if (!isLoadingNew()) {
            setLoadingNew(true);
            final Handler handler = new Handler();
            final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
            final ArrayList<Status> temp = new ArrayList<>();

            Paging paging;
            if (FeedData.getInstance().getTopId() == 0) {
                paging = new Paging(1, 100);
            } else {
                paging = new Paging(1, 100, FeedData.getInstance().getTopId());
            }

            final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void onException(final TwitterException te, TwitterMethod method) {
                    super.onException(te, method);
                    if (temp.size() > 0) {
                        Collections.sort(temp, new Comparator<Status>() {
                            @Override
                            public int compare(Status o1, Status o2) {
                                return Long.compare(o1.getId(), o2.getId());
                            }
                        });

                        for (Status status : temp) {
                            StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                            statusModel.tuneModel(status);
                            statusModel.linkClarify();

                            FeedData.getInstance().addItemToNew(statusModel, true);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                FeedData.getInstance().getUpdateHandler().onAdd();
                                if (te.getErrorMessage() != null) {
                                    Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_loading_feed)
                                            + " because " + te.getErrorMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (te.getErrorMessage() != null) {
                                    Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_loading_feed)
                                            + " because " + te.getErrorMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    setLoadingNew(false);
                }

                @Override
                public void gotHomeTimeline(ResponseList<Status> statuses) {
                    super.gotHomeTimeline(statuses);
                    if (FeedData.getInstance().getTopId() == 0) {
                        Collections.sort(statuses, new Comparator<Status>() {
                            @Override
                            public int compare(Status o1, Status o2) {
                                return Long.compare(o1.getId(), o2.getId());
                            }
                        });

                        for (Status status : statuses) {
                            StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                            statusModel.tuneModel(status);
                            statusModel.linkClarify();

                            FeedData.getInstance().getFeedStatuses().add(0, statusModel);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (FeedData.getInstance().getFeedStatuses().size() > 0)
                                    FeedData.getInstance().setTopId(FeedData.getInstance().getFeedStatuses().get(0).getId());
                                FeedData.getInstance().getUpdateHandler().onUpdate();
                                setLoadingNew(false);
                            }
                        });

                        return;
                    }

                    if (FeedData.getInstance().getTopId() > 0) {
                        if (statuses.size() == 0) {
                            if (temp.size() > 0) {
                                Collections.sort(temp, new Comparator<Status>() {
                                    @Override
                                    public int compare(Status o1, Status o2) {
                                        return Long.compare(o1.getId(), o2.getId());
                                    }
                                });

                                for (Status status : temp) {
                                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                                    statusModel.tuneModel(status);
                                    statusModel.linkClarify();

                                    FeedData.getInstance().addItemToNew(statusModel, true);
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FeedData.getInstance().getUpdateHandler().onAdd();
                                        setLoadingNew(false);
                                    }
                                });
                            }
                        } else {
                            temp.addAll(statuses);
                            Paging newPage = new Paging(1, 100, statuses.get(0).getId());
                            asyncTwitter.getHomeTimeline(newPage);
                        }
                    }
                }
            });

            asyncTwitter.getHomeTimeline(paging);
        }
    }

    /**
     * Set tweet divider values
     */
    private void verifyDividers() {
//        boolean isNight = App.getInstance().isNightEnabled();
//        for (int i = 0; i < feedSource.size() - 1; i++) {
//            StatusModel nextModel = feedSource.get(i + 1);
//            StatusModel statusModel = feedSource.get(i);
//
//            if (i == feedSource.size() - 2) {
//                nextModel.setDivideState(StatusModel.DIVIDER_LONG);
//            } else {
//                statusModel.setDivideState(statusModel.isHighlighted() ?
//                        nextModel.isHighlighted() ? StatusModel.DIVIDER_SHORT : isNight ? StatusModel.DIVIDER_NONE
//                                : StatusModel.DIVIDER_LONG : nextModel.isHighlighted() ? isNight ? StatusModel.DIVIDER_NONE
//                        : StatusModel.DIVIDER_LONG : StatusModel.DIVIDER_SHORT);
//            }
//        }
    }
}

package solonsky.signal.twitter.viewmodels;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.android.databinding.library.baseAdapters.BR;
import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.SearchActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.data.LikesData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;
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
 * Created by neura on 22.05.17.
 */

public class LikesViewModel extends BaseObservable {
    private static final String TAG = LikesViewModel.class.getSimpleName();
    private final LoggedActivity mActivity;
    private int state;
    private boolean isLoading;

    /* Feed panel */
    private ArrayList<StatusModel> likesSource;
    private StatusAdapter likesAdapter;
    private ListConfig likesConfig;

    public interface LikesClickHandler {

    }

    public LikesViewModel(final LoggedActivity mActivity, int state) {
        this.mActivity = mActivity;
        this.state = state;
        this.likesSource = new ArrayList<>();
        this.likesAdapter = new StatusAdapter(LikesData.getInstance().getLikesStatuses(), mActivity,
                true, true, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                AppData.searchQuery = (searchText);
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), SearchActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = LikesData.getInstance().getLikesStatuses().indexOf(statusModel);
                LikesData.getInstance().getLikesStatuses().remove(statusModel);
                LikesData.getInstance().saveCache();
                likesAdapter.notifyItemRemoved(position);
            }
        });

        this.likesConfig = new ListConfig.Builder(likesAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .build(mActivity.getApplicationContext());

//        LikesData.getInstance().setUpdateHandler(new UpdateHandler() {
//            @Override
//            public void onUpdate() {
//                final Handler handler = new Handler();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        verifyDividers();
//
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                likesAdapter.notifyDataSetChanged();
//                                setState(LikesData.getInstance().getLikesStatuses().size() > 0 ?
//                                        AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
//                            }
//                        });
//                    }
//                }).start();
//            }
//        });
//
        if (LikesData.getInstance().getLikesStatuses().size() == 0) {
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

    @Bindable
    public ArrayList<StatusModel> getLikesSource() {
        return likesSource;
    }

    public void setLikesSource(ArrayList<StatusModel> likesSource) {
        this.likesSource = likesSource;
        notifyPropertyChanged(BR.likesSource);
    }

    @Bindable
    public StatusAdapter getLikesAdapter() {
        return likesAdapter;
    }

    public void setLikesAdapter(StatusAdapter likesAdapter) {
        this.likesAdapter = likesAdapter;
        notifyPropertyChanged(BR.likesAdapter);
    }

    @Bindable
    public ListConfig getLikesConfig() {
        return likesConfig;
    }

    public void setLikesConfig(ListConfig likesConfig) {
        this.likesConfig = likesConfig;
        notifyPropertyChanged(BR.likesConfig);
    }

    /*
      * ==============================================
      * == This section starts a data logic section ==
      * ==============================================
    */

    private void loadCache() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Reservoir.getAsync(Cache.Likes + String.valueOf(AppData.ME.getId()), LikesData.class,
                        new ReservoirGetCallback<LikesData>() {
                            @Override
                            public void onSuccess(final LikesData likesData) {
                                LikesData.getInstance().setScrollPosition(likesData.getScrollPosition());
                                LikesData.getInstance().setScrollTop(likesData.getScrollTop());

                                for (StatusModel statusModel : likesData.getLikesStatuses()) {
                                    if (!LikesData.getInstance().getLikesStatuses().contains(statusModel)) {
                                        LikesData.getInstance().getLikesStatuses().add(statusModel);
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        LikesData.getInstance().getUpdateHandler().onUpdate();
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
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        final Handler handler = new Handler();
        final long maxId = LikesData.getInstance().getLikesStatuses().get(LikesData.getInstance().getLikesStatuses().size() - 1).getId();
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
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_loading_feed)
                                + " because " + te.getErrorMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                });
            }

            @Override
            public void gotFavorites(ResponseList<Status> statuses) {
                super.gotFavorites(statuses);
                for (Status status : statuses) {
                    if (status.getId() < maxId) {
                        StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                        statusModel.tuneModel(status);
                        statusModel.linkClarify();
                        LikesData.getInstance().getLikesStatuses().add(statusModel);
                    }
                }

                LikesData.getInstance().saveCache();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LikesData.getInstance().getUpdateHandler().onUpdate();
                        setLoading(false);
                    }
                });
            }
        });

        asyncTwitter.getFavorites(paging);
    }

    public void loadNew() {
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        final Handler handler = new Handler();
        Paging paging = new Paging(1, 100);

        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (te.getErrorMessage() != null)
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.error_loading_likes)
                                + " because " + te.getErrorMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void gotFavorites(ResponseList<Status> statuses) {
                super.gotFavorites(statuses);
                for (Status status : statuses) {

                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();
                    statusModel.setNewStatus(true);

                    if (!LikesData.getInstance().getLikesStatuses().contains(statusModel)) {
                        LikesData.getInstance().getLikesStatuses().add(statusModel);
                        LikesData.getInstance().incEntryCount();
                    }
                }

                Collections.sort(LikesData.getInstance().getLikesStatuses(), new Comparator<StatusModel>() {
                    @Override
                    public int compare(StatusModel o1, StatusModel o2) {
                        return Long.compare(o2.getId(), o1.getId());
                    }
                });

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        LikesData.getInstance().saveCache();
                        LikesData.getInstance().getUpdateHandler().onUpdate();
                    }
                });
            }
        });

        asyncTwitter.getFavorites(paging);
    }

    /**
     * Set tweet divider values
     */
    private void verifyDividers() {
        boolean isNight = App.getInstance().isNightEnabled();
        for (int i = 0; i < LikesData.getInstance().getLikesStatuses().size() - 1; i++) {
            StatusModel nextModel = LikesData.getInstance().getLikesStatuses().get(i + 1);
            StatusModel statusModel = LikesData.getInstance().getLikesStatuses().get(i);

            if (i == LikesData.getInstance().getLikesStatuses().size() - 2) {
                nextModel.setDivideState(Flags.DIVIDER_LONG);
            } else {
                statusModel.setDivideState(statusModel.isHighlighted() ?
                        nextModel.isHighlighted() ? Flags.DIVIDER_SHORT : isNight ? Flags.DIVIDER_NONE
                                : Flags.DIVIDER_LONG : nextModel.isHighlighted() ? isNight ? Flags.DIVIDER_NONE
                        : Flags.DIVIDER_LONG : Flags.DIVIDER_SHORT);
            }
        }
    }
}

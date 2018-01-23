package solonsky.signal.twitter.viewmodels;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import solonsky.signal.twitter.activities.MVPSearchActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.data.MentionsData;
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
 * Created by neura on 22.05.17.
 */

public class MentionsViewModel extends BaseObservable {
    private static final String TAG = MentionsViewModel.class.getSimpleName();
    private final AppCompatActivity mActivity;
    private int state;

    /* Feed panel */
    private ArrayList<StatusModel> mentionsSource;
    private StatusAdapter mentionsAdapter;
    private ListConfig mentionsConfig;

    private boolean isLoading = false;

    public interface MentionsClickHandler {

    }

    public MentionsViewModel(final AppCompatActivity mActivity, int state, TweetActions.MoreCallback moreCallback) {
        this.mActivity = mActivity;
        this.state = state;
        this.mentionsSource = new ArrayList<>();
        this.mentionsAdapter = new StatusAdapter(MentionsData.Companion.getInstance().getMentionsStatuses(),
                mActivity, true, true, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                Intent searchIntent = new Intent(mActivity.getApplicationContext(), MVPSearchActivity.class);
                searchIntent.putExtra(Keys.SearchQuery.getValue(), searchText);
                mActivity.startActivity(searchIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, moreCallback);

        this.mentionsConfig = new ListConfig.Builder(mentionsAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SpeedyLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .build(mActivity.getApplicationContext());

        if (MentionsData.Companion.getInstance().getMentionsStatuses().size() == 0) {
            loadCache();
        }
    }

    @Bindable
    public ArrayList<StatusModel> getMentionsSource() {
        return mentionsSource;
    }

    public void setMentionsSource(ArrayList<StatusModel> mentionsSource) {
        this.mentionsSource = mentionsSource;
        notifyPropertyChanged(BR.mentionsSource);
    }

    @Bindable
    public StatusAdapter getMentionsAdapter() {
        return mentionsAdapter;
    }

    public void setMentionsAdapter(StatusAdapter mentionsAdapter) {
        this.mentionsAdapter = mentionsAdapter;
        notifyPropertyChanged(BR.mentionsAdapter);
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
    public ListConfig getMentionsConfig() {
        return mentionsConfig;
    }

    public void setMentionsConfig(ListConfig mentionsConfig) {
        this.mentionsConfig = mentionsConfig;
        notifyPropertyChanged(BR.mentionsConfig);
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
                Reservoir.getAsync(Cache.Mentions + String.valueOf(AppData.ME.getId()), MentionsData.class,
                        new ReservoirGetCallback<MentionsData>() {
                            @Override
                            public void onSuccess(final MentionsData mentionsData) {
                                Collections.sort(mentionsData.getMentionsStatuses(), new Comparator<StatusModel>() {
                                    @Override
                                    public int compare(StatusModel o1, StatusModel o2) {
                                        return Long.compare(o2.getId(), o1.getId());
                                    }
                                });

                                for (StatusModel statusModel : mentionsData.getMentionsStatuses()) {
                                    if (!MentionsData.Companion.getInstance().getMentionsStatuses().contains(statusModel))
                                        MentionsData.Companion.getInstance().getMentionsStatuses().add(statusModel);
                                }

                                if (MentionsData.Companion.getInstance().getMentionsStatuses().size() > 0) {
                                    MentionsData.Companion.getInstance().setTopId(MentionsData.Companion.getInstance().getMentionsStatuses().get(0).getId());
                                }

                                int entryCount = 0;
                                for (StatusModel statusModel : MentionsData.Companion.getInstance().getMentionsStatuses()) {
                                    if (statusModel.isHighlighted())
                                        entryCount = entryCount + 1;
                                }

                                MentionsData.Companion.getInstance().setScrollPosition(mentionsData.getScrollPosition());
                                MentionsData.Companion.getInstance().setScrollTop(mentionsData.getScrollTop());
                                MentionsData.Companion.getInstance().setCacheEntryCount(entryCount);
                                MentionsData.Companion.getInstance().updateEntryCount();

                                LoggedData.getInstance().setNewMention(entryCount > 0);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MentionsData.Companion.getInstance().getUpdateHandler().onUpdate();
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

    public void loadNew() {
        final Handler handler = new Handler();
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();

        Paging paging;
        if (MentionsData.Companion.getInstance().getTopId() == 0) {
            paging = new Paging(1, 100);
        } else {
            paging = new Paging(1, 100, MentionsData.Companion.getInstance().getTopId());
        }

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
                    }
                });
            }

            @Override
            public void gotMentions(ResponseList<Status> statuses) {
                super.gotMentions(statuses);
                if (MentionsData.Companion.getInstance().getTopId() == 0) {
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

                        if (MentionsData.Companion.getInstance().getTopId() == 0) {
                            MentionsData.Companion.getInstance().getMentionsStatuses().add(0, statusModel);
                        } else {
                            MentionsData.Companion.getInstance().addItemToNew(statusModel, false);
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (MentionsData.Companion.getInstance().getTopId() == 0) {
                                if (MentionsData.Companion.getInstance().getMentionsStatuses().size() > 0)
                                    MentionsData.Companion.getInstance().setTopId(MentionsData.Companion.getInstance().getMentionsStatuses().get(0).getId());
                                if (MentionsData.Companion.getInstance().getUpdateHandler() != null)
                                    MentionsData.Companion.getInstance().getUpdateHandler().onUpdate();
                            } else {
                                MentionsData.Companion.getInstance().getUpdateHandler().onAdd();
                            }
                        }
                    });
                }
            }
        });

        asyncTwitter.getMentions(paging);
    }

    public void loadNext() {
        mentionsAdapter.setLoading(true);
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        final Handler handler = new Handler();
        final long maxId = MentionsData.Companion.getInstance().getMentionsStatuses().get(
                MentionsData.Companion.getInstance().getMentionsStatuses().size() - 1).getId();
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

                        mentionsAdapter.setLoading(false);
                        mentionsAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void gotMentions(ResponseList<Status> statuses) {
                super.gotMentions(statuses);
                for (Status status : statuses) {
                    if (status.getId() < maxId) {
                        StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                        statusModel.tuneModel(status);
                        statusModel.linkClarify();
                        MentionsData.Companion.getInstance().getMentionsStatuses().add(statusModel);
                    }
                }

                MentionsData.Companion.getInstance().saveCache(TAG);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MentionsData.Companion.getInstance().getUpdateHandler().onUpdate();
                        setLoading(false);

                        mentionsAdapter.setLoading(false);
                        mentionsAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        asyncTwitter.getMentions(paging);
    }

}

package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.data.MentionsData;
import solonsky.signal.twitter.databinding.FragmentMentionsBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateAddHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.MentionsViewModel;

import static solonsky.signal.twitter.helpers.TweetActions.*;

/**
 * Created by neura on 19.05.17.
 */

public class MentionsFragment extends Fragment implements FragmentCounterListener {
    private final String TAG = MentionsFragment.class.getSimpleName();
    private MentionsViewModel viewModel;
    private FragmentMentionsBinding binding;
    private ActivityListener mCallback;

    /**
     * Setup endless scroll
     */
    private RecyclerView.OnScrollListener mentionsScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int hiddenPosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (mCallback != null && dy != 0) {
                mCallback.updateBars(dy);

                MentionsData.Companion.getInstance().setScrollPosition(linearLayoutManager.findFirstVisibleItemPosition());
                MentionsData.Companion.getInstance().setScrollTop(linearLayoutManager.getChildAt(0).getTop());

                if (hiddenPosition > 0) {
                    if (MentionsData.Companion.getInstance().getMentionsStatuses().get(hiddenPosition - 1).isHighlighted()) {
                        MentionsData.Companion.getInstance().getMentionsStatuses().get(hiddenPosition - 1).setHighlighted(false);
                        MentionsData.Companion.getInstance().decEntryCount();
                        mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());
                        if (MentionsData.Companion.getInstance().getEntryCount() == 0) {
                            LoggedData.getInstance().setNewMention(false);
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }

                        LoggedData.getInstance().setNewFeed(MentionsData.Companion.getInstance().getEntryCount() > 0);
                        LoggedData.getInstance().getUpdateHandler().onUpdate();
                    }
                }
            }

            if (dy > 0) {
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (!viewModel.isLoading()) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        viewModel.setLoading(true);
                        viewModel.loadNext();
                    }
                }
            }
        }
    };

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (MentionsData.Companion.getInstance().getNewStatuses().size() > 0) {
                MentionsFragment.this.onUpdate();
//                try {
//                    for (StatusModel statusModel : MentionsData.getInstance().getMentionsStatuses()) {
//                        statusModel.setHighlighted(false);
//                    }
//
//                    MentionsData.getInstance().setEntryCount(0);
//                    LoggedData.getInstance().setNewMention(false);
//                    LoggedData.getInstance().getUpdateHandler().onUpdate();
//
//                    if (mCallback != null)
//                        mCallback.updateCounter(MentionsData.getInstance().getEntryCount());
//                } catch (Exception e) {
//
//                }
            } else {
                viewModel.loadNew();
            }
            binding.srlMentionsMain.setRefreshing(false);
        }
    };

    TweetActions.MoreCallback moreCallback = new MoreCallback() {
        @Override
        public void onDelete(StatusModel statusModel) {
            int position = MentionsData.Companion.getInstance().getMentionsStatuses().indexOf(statusModel);
            MentionsData.Companion.getInstance().getMentionsStatuses().remove(statusModel);
            MentionsData.Companion.getInstance().saveCache(TAG);
            viewModel.getMentionsAdapter().notifyItemRemoved(position);

            if (statusModel.isHighlighted()) {
                MentionsData.Companion.getInstance().decEntryCount();
                mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());

                LoggedData.getInstance().setNewMention(MentionsData.Companion.getInstance().getEntryCount() > 0);
                LoggedData.getInstance().getUpdateHandler().onUpdate();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mentions, container, false);

        if (MentionsData.Companion.getInstance().getMentionsStatuses().size() == 0) {
            viewModel = new MentionsViewModel((AppCompatActivity) getActivity(), AppData.UI_STATE_LOADING, moreCallback);
        } else {
            viewModel = new MentionsViewModel((AppCompatActivity) getActivity(), AppData.UI_STATE_VISIBLE, moreCallback);
        }

        binding.setMentions(viewModel);
        binding.recyclerMentions.setOnScrollListener(mentionsScrollListener);

        if (mCallback != null) {
            mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());
            mCallback.updateSettings(R.string.title_mentions, false, false);
            mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);

            LoggedData.getInstance().setNewMention(MentionsData.Companion.getInstance().getEntryCount() > 0);
            LoggedData.getInstance().getUpdateHandler().onUpdate();
        }

        MentionsData.Companion.getInstance().setUpdateHandler(new UpdateAddHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(MentionsData.Companion.getInstance().getMentionsStatuses().size() > 0 ?
                        AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
                viewModel.getMentionsAdapter().notifyDataSetChanged();
                binding.recyclerMentions.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((LinearLayoutManager) binding.recyclerMentions.getLayoutManager()).scrollToPositionWithOffset(
                                MentionsData.Companion.getInstance().getScrollPosition(),
                                MentionsData.Companion.getInstance().getScrollTop());
                    }
                }, 50);

                if (mCallback != null && !isHidden())
                    mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());

                LoggedData.getInstance().setNewMention(MentionsData.Companion.getInstance().getEntryCount() > 0);
                LoggedData.getInstance().getUpdateHandler().onUpdate();
            }

            @Override
            public void onAdd() {
                if (MentionsData.Companion.getInstance().getMentionsStatuses().size() == 0) {
                    MentionsFragment.this.onUpdate();
                } else {
                    MentionsData.Companion.getInstance().updateEntryCount();
                    LoggedData.getInstance().setNewMention(MentionsData.Companion.getInstance().getEntryCount() > 0);
                    LoggedData.getInstance().getUpdateHandler().onUpdate();

                    if (mCallback != null && !isHidden())
                        mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());
                }
            }

            @Override
            public void onError() {

            }

            @Override
            public void onDelete(int position) {
            }
        });

        binding.srlMentionsMain.setOnRefreshListener(refreshListener);
        binding.srlMentionsMain.setProgressViewOffset(false,
                (int) Utilities.convertDpToPixel(80, getContext()),
                (int) Utilities.convertDpToPixel(96, getContext()));

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ActivityListener) context;
        } catch (ClassCastException e) {
            // Do nothing
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        MentionsData.Companion.getInstance().setUpdateHandler(null);
    }

    @Override
    public void onResume() {
        super.onResume();
//        MentionsData.getInstance().getUpdateHandler().onUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MentionsData.Companion.getInstance().saveCache(TAG);
            }
        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null) {
                mCallback.updateCounter(MentionsData.Companion.getInstance().getEntryCount());
                mCallback.updateSettings(R.string.title_mentions, false, false);
                mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);

                LoggedData.getInstance().setNewMention(MentionsData.Companion.getInstance().getEntryCount() > 0);
                LoggedData.getInstance().getUpdateHandler().onUpdate();
                binding.recyclerMentions.scrollToPosition(0);
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MentionsData.Companion.getInstance().saveCache("hidden change " + TAG);
                }
            }).start();
        }
    }

    @Override
    public void onScrollToTop() {
        binding.recyclerMentions.scrollToPosition(0);
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, int pauseDuration, int endDuration) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerMentions.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        if (!isHidden()) {
            if (position > 15) {
                binding.recyclerMentions.scrollToPosition(15);
                binding.recyclerMentions.smoothScrollToPosition(0);
            } else {
                binding.recyclerMentions.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    public void onBackToPosition() {
        ((LinearLayoutManager) binding.recyclerMentions.getLayoutManager())
                .scrollToPositionWithOffset(
                        MentionsData.Companion.getInstance().getScrollPosition(),
                        MentionsData.Companion.getInstance().getScrollTop());
    }

    @Override
    public void onUpdate() {
        if (MentionsData.Companion.getInstance().getNewStatuses().size() > 0 && !isHidden()) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MentionsData.Companion.getInstance().updateEntryCount();
                    MentionsData.Companion.getInstance().addNewItems();
                    MentionsData.Companion.getInstance().setScrollPosition(MentionsData.Companion.getInstance().getScrollPosition() +
                            MentionsData.Companion.getInstance().getEntryCount());
                    MentionsData.Companion.getInstance().setScrollTop(0);
                    MentionsData.Companion.getInstance().saveCache("Update " + TAG);

                    LoggedData.getInstance().setNewFeed(MentionsData.Companion.getInstance().getEntryCount() > 0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                            viewModel.getMentionsAdapter().notifyDataSetChanged();
                            viewModel.setState(MentionsData.Companion.getInstance().getMentionsStatuses().size() > 0 ?
                                    AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
                        }
                    });
                }
            }).start();
        }
    }
}

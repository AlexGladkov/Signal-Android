package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import moxy.MvpAppCompatFragment;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.databinding.FragmentFeedBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateAddHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.SpeedyLinearLayoutManager;
import solonsky.signal.twitter.viewmodels.FeedViewModel;

/**
 * Created by neura on 19.05.17.
 */

public class FeedFragment extends MvpAppCompatFragment implements FragmentCounterListener {
    private final String TAG = FeedFragment.class.getSimpleName();
    private FeedViewModel viewModel;
    private FragmentFeedBinding binding;

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (FeedData.getInstance().getNewStatuses().size() > 0) {
                Log.e(TAG, "refresh 1");
                FeedFragment.this.onUpdate();
                if (mCallback != null) {
                    mCallback.updateStatusType(Flags.STATUS_TYPE.ARROW);
                }
            } else {
                Log.e(TAG, "refresh 2");
                viewModel.setRefresh(true);
                viewModel.loadNew();
            }
            binding.srlFeedMain.setRefreshing(false);
        }
    };

    private RecyclerView.OnScrollListener feedScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

            if (mCallback != null && dy != 0) {
                mCallback.updateBars(dy);
                if (mCallback.checkState().equals(Flags.STATUS_TYPE.ARROW)) {
                    mCallback.updateStatusType(Flags.STATUS_TYPE.TITLE);
                    FeedData.getInstance().setEntryCount(0);
                    FeedData.getInstance().clearNew();
                    LoggedData.getInstance().setNewFeed(false);
                    LoggedData.getInstance().getUpdateHandler().onUpdate();
                } else if (mCallback.checkState().equals(Flags.STATUS_TYPE.COUNTER)) {
                    try {
                        if (FeedData.getInstance().getFeedStatuses().get(firstVisiblePosition).isNewStatus()) {
                            FeedData.getInstance().getFeedStatuses().get(firstVisiblePosition).setNewStatus(false);
                            FeedData.getInstance().decEntryCount();
                            mCallback.updateCounter(FeedData.getInstance().getEntryCount());

                            LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Do nothing
                    }
                } else {
                    if (FeedData.getInstance().getEntryCount() > 0) {
                        mCallback.updateCounter(FeedData.getInstance().getEntryCount());
                    } else {
                        LoggedData.getInstance().setNewFeed(false);
                        LoggedData.getInstance().getUpdateHandler().onUpdate();
                    }
                }
            }

            if (dy != 0) {
                FeedData.getInstance().setScrollPosition(firstVisiblePosition);
                FeedData.getInstance().setScrollTop(linearLayoutManager.getChildAt(0).getTop());
            }


            if (dy > 0) {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!viewModel.isLoading()) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        viewModel.setLoading(true);
                        viewModel.loadNext();
                    }
                }
            }
        }
    };
    private ActivityListener mCallback = null;

    @Override
    public void onScrollToTop() {
        if (!isHidden())
            binding.recyclerFeed.scrollToPosition(0);
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, final int pauseDuration, final int endDuration) {
        SpeedyLinearLayoutManager layoutManager = (SpeedyLinearLayoutManager) binding.recyclerFeed.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        if (!isHidden()) {
            if (position > 5) {
                binding.recyclerFeed.scrollToPosition(5);
                binding.recyclerFeed.smoothScrollToPosition(0);
            } else {
                binding.recyclerFeed.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    public void onBackToPosition() {
        if (!isHidden())
            ((LinearLayoutManager) binding.recyclerFeed.getLayoutManager())
                    .scrollToPositionWithOffset(FeedData.getInstance().getScrollPosition(), FeedData.getInstance().getScrollTop());
    }

    @Override
    public void onUpdate() {
        if (FeedData.getInstance().getNewStatuses().size() > 0 && !isHidden()) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FeedData.getInstance().updateEntryCount();
                    FeedData.getInstance().addNewItems();
                    FeedData.getInstance().setScrollPosition(FeedData.getInstance().getScrollPosition() +
                            FeedData.getInstance().getEntryCount());
                    FeedData.getInstance().setScrollTop(0);
                    FeedData.getInstance().saveCache("Fragment on Update");

                    LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            LoggedData.getInstance().getUpdateHandler().onUpdate();
                            viewModel.getmFeedAdapter().notifyDataSetChanged();
                            viewModel.setState(FeedData.getInstance().getFeedStatuses().size() > 0 ?
                                    AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
                        }
                    });
                }
            }).start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (FeedData.getInstance().getFeedStatuses().size() == 0) {
            viewModel = new FeedViewModel((AppCompatActivity) getActivity(), AppData.UI_STATE_LOADING);
        } else {
            viewModel = new FeedViewModel((AppCompatActivity) getActivity(), AppData.UI_STATE_VISIBLE );
        }

        binding.setFeed(viewModel);

        binding.recyclerFeed.setLayoutManager(new SpeedyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerFeed.setHasFixedSize(true);
        binding.recyclerFeed.setAdapter(viewModel.getmFeedAdapter());

        binding.recyclerFeed.addOnScrollListener(feedScrollListener);
        binding.srlFeedMain.setOnRefreshListener(refreshListener);
        binding.srlFeedMain.setProgressViewOffset(false,
                (int) Utilities.convertDpToPixel(80, getContext()),
                (int) Utilities.convertDpToPixel(96, getContext()));

        if (mCallback != null) {
            mCallback.updateSettings(R.string.title_feed, false, false);
            mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
            mCallback.updateCounter(FeedData.getInstance().getEntryCount());

            LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);
            LoggedData.getInstance().getUpdateHandler().onUpdate();
        }

        FeedData.getInstance().setUpdateHandler(new UpdateAddHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(FeedData.getInstance().getFeedStatuses().size() > 0 ?
                        AppData.UI_STATE_VISIBLE  : AppData.UI_STATE_NO_ITEMS);
                viewModel.getmFeedAdapter().notifyDataSetChanged();
                binding.recyclerFeed.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((LinearLayoutManager) binding.recyclerFeed.getLayoutManager()).scrollToPositionWithOffset(
                                FeedData.getInstance().getScrollPosition(),
                                FeedData.getInstance().getScrollTop());
                    }
                }, 50);

                if (mCallback != null && !isHidden())
                    mCallback.updateCounter(FeedData.getInstance().getEntryCount());

                LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);
                LoggedData.getInstance().getUpdateHandler().onUpdate();
            }

            @Override
            public void onAdd() {
                if (FeedData.getInstance().getFeedStatuses().size() == 0 || viewModel.isRefresh()) {
                    viewModel.setRefresh(false);
                    FeedFragment.this.onUpdate();
                } else {
                    FeedData.getInstance().updateEntryCount();
                    LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);
                    LoggedData.getInstance().getUpdateHandler().onUpdate();

                    if (mCallback != null && !isHidden())
                        mCallback.updateCounter(FeedData.getInstance().getEntryCount());
                }
            }

            @Override
            public void onError() {

            }

            @Override
            public void onDelete(int position) {
                try {
                    viewModel.getmFeedAdapter().notifyItemRemoved(position);
                } catch (Exception e) {
                    //Do nothing
                }
            }
        });

        if (FeedData.getInstance().getFeedStatuses().size() > 0)
            onBackToPosition();
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
    public void onResume() {
        super.onResume();
        if (isAdded() && viewModel != null && viewModel.getmFeedAdapter() != null && Flags.needsToRedrawFeed) {
            Flags.needsToRedrawFeed = false;
            viewModel.getmFeedAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FeedData.getInstance().saveCache("Fragment onPause");
            }
        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null) {
                Log.e(TAG, "entry count - " + FeedData.getInstance().getEntryCount());
                mCallback.updateSettings(R.string.title_feed, false, false);
                mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
                mCallback.updateCounter(FeedData.getInstance().getEntryCount());

                LoggedData.getInstance().setNewFeed(FeedData.getInstance().getEntryCount() > 0);
                LoggedData.getInstance().getUpdateHandler().onUpdate();
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FeedData.getInstance().saveCache("Fragment onHiddenChanged");
                }
            }).start();

            if (binding.srlFeedMain != null) {
                binding.srlFeedMain.setRefreshing(false);
                binding.srlFeedMain.destroyDrawingCache();
                binding.srlFeedMain.clearAnimation();
            }
        }
    }
}

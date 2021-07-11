package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import moxy.MvpAppCompatFragment;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.data.LikesData;
import solonsky.signal.twitter.databinding.FragmentLikesBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.viewmodels.LikesViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class LikesFragment extends MvpAppCompatFragment implements FragmentCounterListener {
    private final String TAG = LikesFragment.class.getSimpleName();
    private LikesViewModel viewModel;
    private LoggedActivity mActivity;
    private FragmentLikesBinding binding;
    private ActivityListener mCallback;

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            viewModel.loadNew();
            binding.srlLikes.setRefreshing(false);
        }
    };

    /**
     * Setup endless scroll
     */
    private RecyclerView.OnScrollListener likesScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy != 0) {
                if (mCallback != null)
                    mCallback.updateBars(dy);
            }

            if (dy > 0) {
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (!viewModel.isLoading()) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        viewModel.setLoading(true);
//                        viewModel.loadNext();
                    }
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_likes, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (LoggedActivity) getActivity();

        if (LikesData.getInstance().getLikesStatuses().size() == 0) {
            viewModel = new LikesViewModel(mActivity, AppData.UI_STATE_LOADING);
        } else {
            viewModel = new LikesViewModel(mActivity, AppData.UI_STATE_VISIBLE );
        }

        binding.setModel(viewModel);

        if (mCallback != null) {
            mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
            mCallback.updateCounter(0);
            mCallback.updateSettings(R.string.title_likes, false, false);
        }

        binding.recyclerLike.setOnScrollListener(likesScrollListener);
        binding.srlLikes.setOnRefreshListener(refreshListener);
        binding.srlLikes.setProgressViewOffset(false,
                (int) Utilities.convertDpToPixel(80, getContext()),
                (int) Utilities.convertDpToPixel(96, getContext()));

        LikesData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(LikesData.getInstance().getLikesStatuses().size() > 0 ?
                        AppData.UI_STATE_VISIBLE  : AppData.UI_STATE_NO_ITEMS);
                viewModel.getLikesAdapter().notifyDataSetChanged();
                binding.recyclerLike.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((LinearLayoutManager) binding.recyclerLike.getLayoutManager()).scrollToPositionWithOffset(
                                LikesData.getInstance().getScrollPosition(),
                                LikesData.getInstance().getScrollTop());
                    }
                }, 50);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null) {
                mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
                mCallback.updateCounter(0);
                mCallback.updateSettings(R.string.title_likes, false, false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onScrollToTop() {
        binding.recyclerLike.scrollToPosition(0);
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, int pauseDuration, int endDuration) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerLike.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        if (!isHidden()) {
            if (position > 10) {
                binding.recyclerLike.scrollToPosition(10);
                binding.recyclerLike.smoothScrollToPosition(0);
            } else {
                binding.recyclerLike.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    public void onBackToPosition() {

    }

    @Override
    public void onUpdate() {

    }
}

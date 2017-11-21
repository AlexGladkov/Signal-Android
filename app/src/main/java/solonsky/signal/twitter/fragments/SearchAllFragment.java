package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.databinding.FragmentSearchAllBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.SearchDetailViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.SavedSearch;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 03.06.17.
 */

public class SearchAllFragment extends Fragment {
    private final String TAG = SearchAllFragment.class.getSimpleName();
    private SearchDetailViewModel viewModel;
    private StatusAdapter mAdapter;
    private ArrayList<StatusModel> mTweetsList;
    private ArrayList<StatusModel> mSourceList;

    private final int popular = 0;
    private final int retweet = 1;

    private boolean isPopular = true;
    private boolean isRetweet = true;

    private FragmentSearchAllBinding binding;
    private SearchedFragment searchFragment;
//    private LoggedActivity mActivity;
    private SearchListener mCallback;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_all, container, false);

//        mActivity = (LoggedActivity) getActivity();

        mSourceList = new ArrayList<>();
        mTweetsList = new ArrayList<>();

        mAdapter = new StatusAdapter(mTweetsList, (AppCompatActivity) getActivity(), true,
                false, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                if (mCallback != null)
                    mCallback.startSearch(searchText);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = mTweetsList.indexOf(statusModel);
                mTweetsList.remove(statusModel);
                mAdapter.notifyItemRemoved(position);
            }
        });

        ListConfig config = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(false)
                .setDefaultDividerEnabled(true)
                .setHasNestedScroll(false)
                .build(getContext());

        viewModel = new SearchDetailViewModel(config);
        viewModel.setState(AppData.UI_STATE_LOADING);

        binding.setModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null)
                mCallback.updatePosition(0);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SearchListener) context;
        } catch (ClassCastException e) {
            //Do nothing
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void updateData(ArrayList<StatusModel> statusModels) {
        mSourceList = new ArrayList<>();
        mTweetsList = new ArrayList<>();
        for (StatusModel statusModel : statusModels) {
            mTweetsList.add(statusModel);
            mSourceList.add(statusModel);
        }

        TweetActions.verifyDividers(mTweetsList);
        setupList();
    }

    public void filterSource(int source, boolean value) {
        switch (source) {
            case popular:
                isPopular = value;
                break;

            case retweet:
                isRetweet = value;
                break;
        }

        mTweetsList = new ArrayList<>();
        for (StatusModel statusModel : mSourceList) {
            if (isRetweet || (!isRetweet && !statusModel.isRetweet()) &&
                    (isPopular || (!isPopular && statusModel.getRetweetCount() <= 1000))) {
                mTweetsList.add(statusModel);
            }
        }

        TweetActions.verifyDividers(mTweetsList);
    }

    public void setupList() {
        mAdapter = new StatusAdapter(mTweetsList, (AppCompatActivity) getActivity(), true,
                false, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                if (mCallback != null)
                    mCallback.startSearch(searchText);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = mTweetsList.indexOf(statusModel);
                mTweetsList.remove(statusModel);
                mAdapter.notifyItemRemoved(position);
            }
        });

        ListConfig config = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(false)
                .setDefaultDividerEnabled(true)
                .setHasNestedScroll(false)
                .build(getContext());

        viewModel = new SearchDetailViewModel(config);
        viewModel.setState(mTweetsList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);

        searchFragment = (SearchedFragment) getParentFragment();
        binding.setModel(viewModel);
    }
}

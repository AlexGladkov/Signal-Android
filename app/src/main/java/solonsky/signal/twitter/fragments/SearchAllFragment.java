package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.databinding.FragmentSearchAllBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Keys;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.SearchDetailViewModel;

/**
 * Created by neura on 03.06.17.
 */

public class SearchAllFragment extends Fragment {
    private final String TAG = SearchAllFragment.class.getSimpleName();

    private final int popular = 0;
    private final int retweet = 1;

    private boolean isPopular = true;
    private boolean isRetweet = true;

    private FragmentSearchAllBinding binding;
    private SearchListener mCallback;

    public static SearchAllFragment getNewInstance(Bundle args) {
        SearchAllFragment fragment = new SearchAllFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_all, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            ArrayList<StatusModel> mStatusesList = getArguments().getParcelableArrayList(Keys.SearchList.getValue());
            boolean isLoaded = getArguments().getBoolean(Keys.SearchLoaded.getValue());
            if (mStatusesList == null) mStatusesList = new ArrayList<>();

            StatusAdapter mAdapter = new StatusAdapter(mStatusesList, (AppCompatActivity) getActivity(), true,
                    false, new StatusAdapter.StatusClickListener() {
                @Override
                public void onSearch(String searchText, View v) {
//                    if (mCallback != null)
//                        mCallback.startSearch(searchText);
                }
            }, new TweetActions.MoreCallback() {
                @Override
                public void onDelete(StatusModel statusModel) {
//                    int position = mTweetsList.indexOf(statusModel);
//                    mTweetsList.remove(statusModel);
//                    mAdapter.notifyItemRemoved(position);
                }
            });

            ListConfig config = new ListConfig.Builder(mAdapter)
                    .setHasFixedSize(false)
                    .setDefaultDividerEnabled(true)
                    .setHasNestedScroll(false)
                    .build(getContext());

            SearchDetailViewModel viewModel = new SearchDetailViewModel(config);
            viewModel.setState(!isLoaded ? AppData.UI_STATE_LOADING : mStatusesList.size() == 0 ?
                    AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
            binding.setModel(viewModel);
        }
    }
}

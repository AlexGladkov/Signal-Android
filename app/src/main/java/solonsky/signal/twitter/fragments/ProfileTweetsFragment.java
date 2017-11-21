package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.SearchActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.api.ProfileDataApi;
import solonsky.signal.twitter.databinding.FragmentProfileTweetsBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.ProfileTweetsViewModel;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileTweetsFragment extends Fragment {
    private final String TAG = ProfileTweetsFragment.class.getSimpleName();
    private ProfileTweetsViewModel viewModel;
    private StatusAdapter mAdapter;
    private FragmentProfileTweetsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_tweets, container, false);

        mAdapter = new StatusAdapter(ProfileDataApi.getInstance().getTweets(), (AppCompatActivity) getActivity(), true,
                true, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                AppData.searchQuery = searchText;
                getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = ProfileDataApi.getInstance().getTweets().indexOf(statusModel);
                ProfileDataApi.getInstance().getTweets().remove(statusModel);
                mAdapter.notifyItemRemoved(position);
            }
        });

        viewModel = new ProfileTweetsViewModel(mAdapter, getContext());
        viewModel.setState(ProfileDataApi.getInstance().getTweets().size() == 0 ?
                AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
        binding.setModel(viewModel);

        ProfileDataApi.getInstance().setTweetHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(ProfileDataApi.getInstance().getTweets().size() == 0 ?
                        AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.profileBottomMore.setOnClickListener(null);
    }
}

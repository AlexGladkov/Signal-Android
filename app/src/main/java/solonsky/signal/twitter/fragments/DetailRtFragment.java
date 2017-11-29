package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.activities.ProfileActivity;
import solonsky.signal.twitter.adapters.UserDetailAdapter;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.FragmentDetailRtBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.viewmodels.DetailListViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.User;

/**
 * Created by neura on 04.06.17.
 */

public class DetailRtFragment extends Fragment {
    private static final String TAG = DetailRtFragment.class.getSimpleName();
    private ArrayList<UserModel> mUsersList;
    private UserDetailAdapter mAdapter;
    private DetailListViewModel viewModel;
    private DetailActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDetailRtBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_rt, container, false);

        mUsersList = new ArrayList<>();

        mActivity = (DetailActivity) getActivity();
        mAdapter = new UserDetailAdapter(mUsersList, getContext(), (AppCompatActivity) getActivity(),
                new UserDetailAdapter.UserClickHandler() {
                    @Override
                    public void onItemClick(UserModel model, View v) {
                        if (model.getUser() != null) {
                            AppData.CURRENT_USER = model.getUser();
                            Flags.userDirection = Flags.Directions.FROM_RIGHT;
                            Flags.userSource = Flags.UserSource.data;
                            Flags.homeUser = AppData.CURRENT_USER.getId() == AppData.ME.getId();
                            mActivity.startActivity(new Intent(getContext(), ProfileActivity.class));
                            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                });

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasNestedScroll(false)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        viewModel = new DetailListViewModel(listConfig);
        viewModel.setState(AppData.UI_STATE_LOADING);
        binding.setModel(viewModel);
        return binding.getRoot();
    }

    public void loadApi() {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotRetweets(ResponseList<Status> retweets) {
                super.gotRetweets(retweets);
                for (int i = 0; i < retweets.size(); i++) {
                    User user = retweets.get(i).getUser();

                    UserModel userModel = new UserModel(user.getId(), user.getBiggerProfileImageURL(),
                            user.getName(), Utilities.parseFollowers(user.getFollowersCount(), "followers"),
                            UsersData.getInstance().getFollowingList().contains(user.getId())
                                    || user.getId() == AppData.ME.getId(), false, false);
                    userModel.setUser(solonsky.signal.twitter.models.User.getFromUserInstance(user));
                    mUsersList.add(userModel);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.setState(mUsersList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
                        mActivity.setRtCount(mUsersList.size());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        asyncTwitter.getRetweets(AppData.CURRENT_STATUS_MODEL.getId());
    }
}

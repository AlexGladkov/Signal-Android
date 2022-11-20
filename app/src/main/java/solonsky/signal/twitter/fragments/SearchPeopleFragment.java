package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.NotificationsDetailAdapter;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.FragmentSearchPeopleBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Keys;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.viewmodels.SearchDetailViewModel;

/**
 * Created by neura on 03.06.17.
 */

public class SearchPeopleFragment extends Fragment {
    private final String TAG = SearchPeopleFragment.class.getSimpleName();
    private NotificationsDetailAdapter mAdapter;
    private SearchDetailViewModel viewModel;
    private FragmentSearchPeopleBinding binding;
    private List<User> mUsersList;

    private SearchListener mCallback;

    NotificationsDetailAdapter.DetailClickListener detailClickListener =
            new NotificationsDetailAdapter.DetailClickListener() {
                @Override
                public void onItemClick(NotificationDetailModel model, View v) {
                    for (User user : mUsersList) {
                        if (user.getId() == model.getId()) {
                            Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                            profileIntent.putExtra(Flags.PROFILE_DATA, user);
                            getActivity().startActivity(profileIntent);
                            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                }
            };

    public static SearchPeopleFragment getNewInstance(Bundle args) {
        SearchPeopleFragment fragment = new SearchPeopleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_people, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mUsersList = getArguments().getParcelableArrayList(Keys.SearchList.getValue());
            boolean isLoaded = getArguments().getBoolean(Keys.SearchLoaded.getValue());
            if (mUsersList == null) mUsersList = new ArrayList<>();

            List<NotificationDetailModel> mFinalUsers = new LinkedList<>();
            for (int i = 0; i < mUsersList.size(); i++) {
                User user = mUsersList.get(i);
                NotificationDetailModel notificationDetailModel = NotificationDetailModel.getFollowInstance(user.getId(),
                        user.getName(), "@" + user.getScreenName(), user.getOriginalProfileImageURL(),
                        user.getDescription(), UsersData.getInstance().getFollowingList().contains(user.getId()), false);

                mFinalUsers.add(notificationDetailModel);
            }

            Log.e(TAG, "final users count " + mFinalUsers.size());
            mAdapter = new NotificationsDetailAdapter(mFinalUsers, getContext(), detailClickListener);

            ListConfig config = new ListConfig.Builder(mAdapter)
                    .setHasFixedSize(false)
                    .setDefaultDividerEnabled(true)
                    .setHasNestedScroll(false)
                    .build(getContext());

            viewModel = new SearchDetailViewModel(config);
            viewModel.setState(!isLoaded ? AppData.UI_STATE_LOADING : mFinalUsers.size() == 0 ?
                    AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
            binding.setModel(viewModel);
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null)
                mCallback.updatePosition(1);
        }
    }

//    public void filterSource(int source, boolean value) {
//        switch (source) {
//            case followers:
//                isFollowers = value;
//                break;
//
//            case verified:
//                isVerified = value;
//                break;
//        }
//
//        mTweetList = new ArrayList<>();
//        for (User user : mSourceList) {
//            if ((!isVerified || user.isVerified()) && (!isFollowers || user.isFollowRequestSent())) {
//                NotificationDetailModel notificationDetailModel = NotificationDetailModel.getFollowInstance(user.getId(),
//                        user.getName(), user.getScreenName(), user.getBiggerProfileImageURL(),
//                        user.getDescription(), user.isFollowRequestSent(), false);
//
//                mTweetList.add(notificationDetailModel);
//            }
//        }
//
//        mAdapter.notifyDataSetChanged();
//    }
//
//    public void setupList() {
//        mAdapter = new NotificationsDetailAdapter(mTweetList, getContext(), detailClickListener);
//
//        ListConfig config = new ListConfig.Builder(mAdapter)
//                .setHasFixedSize(false)
//                .setDefaultDividerEnabled(true)
//                .setHasNestedScroll(false)
//                .build(getContext());
//
//        viewModel = new SearchDetailViewModel(config);
//        viewModel.setState(mTweetList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
//
//        binding.setModel(viewModel);
//    }
//
//    public void updateData(ArrayList<User> userModels) {
//        for (int i = 0; i < userModels.size(); i++) {
//            User user = userModels.get(i);
//            NotificationDetailModel notificationDetailModel = NotificationDetailModel.getFollowInstance(user.getId(),
//                    user.getName(), "@" + user.getScreenName(), user.getOriginalProfileImageURL(),
//                    user.getDescription(), UsersData.getInstance().getFollowingList().contains(user.getId()), false);
//
//            mTweetList.add(notificationDetailModel);
//            mSourceList.add(user);
//        }
//
//        viewModel.setState(mTweetList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
//        mAdapter.notifyDataSetChanged();
//    }
}

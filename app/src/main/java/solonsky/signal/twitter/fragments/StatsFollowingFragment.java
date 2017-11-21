package solonsky.signal.twitter.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.models.UserModel;

/**
 * Created by neura on 05.07.17.
 */

public class StatsFollowingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.fragment_stats, container, false);

        final LoggedActivity mActivity = (LoggedActivity) getActivity();

        ImageView mBtnBackdrop = (ImageView) viewHierarchy.findViewById(R.id.img_profile_followers);
        ImageView mBtnBack = (ImageView) viewHierarchy.findViewById(R.id.btn_stats_back);
        TextView mTxtTitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_subtitle);

        if (AppData.ME != null) {
            Picasso.with(getContext()).load(AppData.ME.getProfileBannerImageUrl()).into(mBtnBackdrop);
            mTxtSubtitle.setText(getString(R.string.stats_following) + " " + AppData.ME.getFriendsCount());
            mTxtTitle.setText(AppData.ME.getName());
        }

        RecyclerView mRvStats = (RecyclerView) viewHierarchy.findViewById(R.id.recycler_stats);

        final ArrayList<UserModel> userModels = new ArrayList<>();

        for (User user : UsersData.getInstance().getUsersList()) {
            if (UsersData.getInstance().getFollowingList().contains(user.getId())) {
                userModels.add(new UserModel(user.getId(), user.getProfileImageUrl(),
                        user.getName(), "@" + user.getScreenName(), true, false, false));
            }
        }

        UserAdapter mAdapter = new UserAdapter(userModels, getContext(), mActivity, new UserAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {

            }
        });

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        listConfig.applyConfig(getContext(), mRvStats);
        return viewHierarchy;
    }
}

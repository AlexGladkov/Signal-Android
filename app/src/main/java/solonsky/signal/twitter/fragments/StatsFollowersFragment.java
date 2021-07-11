package solonsky.signal.twitter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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

public class StatsFollowersFragment extends Fragment {
    private final String TAG = StatsFollowersFragment.class.getSimpleName();
    private UserAdapter mAdapter;
    private ArrayList<UserModel> userModels;
    private LoggedActivity mActivity;
    private RecyclerView mRvStats;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.fragment_stats, container, false);

        mActivity = (LoggedActivity) getActivity();

        ImageView mBtnBackdrop = (ImageView) viewHierarchy.findViewById(R.id.img_profile_followers);
        ImageView mBtnBack = (ImageView) viewHierarchy.findViewById(R.id.btn_stats_back);
        TextView mTxtTitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_subtitle);

        userModels = new ArrayList<>();

        if (AppData.ME != null) {
            Picasso.get().load(AppData.ME.getProfileBannerImageUrl()).into(mBtnBackdrop);

            userModels.add(new UserModel(AppData.ME.getId(), AppData.ME.getProfileImageUrl(), AppData.ME.getName(),
                    "@" + AppData.ME.getScreenName(), true, false, false));

            mTxtSubtitle.setText(getString(R.string.stats_followers) + " " + AppData.ME.getFollowersCount());
            mTxtTitle.setText(AppData.ME.getName());
        }

        mRvStats = (RecyclerView) viewHierarchy.findViewById(R.id.recycler_stats);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mActivity.onBackPressed();
            }
        });

        for (User user : UsersData.getInstance().getUsersList()) {
            if (UsersData.getInstance().getFollowersList().contains(user.getId())) {
                userModels.add(new UserModel(user.getId(), user.getProfileImageUrl(),
                        user.getName(), "@" + user.getScreenName(),
                        UsersData.getInstance().getFollowingList().contains(user.getId()), false, false));
            }
        }

        mAdapter = new UserAdapter(userModels, getContext(), mActivity, new UserAdapter.UserClickHandler() {
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

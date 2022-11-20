package solonsky.signal.twitter.fragments;

import android.os.Bundle;
import android.os.Handler;
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
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;
import twitter4j.AsyncTwitter;
import twitter4j.PagableResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.UserList;

/**
 * Created by neura on 05.07.17.
 */

public class StatsListedFragment extends Fragment {

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
            Picasso.get().load(AppData.ME.getProfileBannerImageUrl()).into(mBtnBackdrop);
            mTxtSubtitle.setText(getString(R.string.stats_listed) + " " + AppData.ME.getListedCount());
            mTxtTitle.setText(AppData.ME.getName());
        }

        RecyclerView mRvStats = (RecyclerView) viewHierarchy.findViewById(R.id.recycler_stats);

        final ArrayList<UserModel> userModels = new ArrayList<>();

        final UserAdapter mAdapter = new UserAdapter(userModels, getContext(), mActivity, new UserAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {

            }
        });

        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotUserListMemberships(PagableResponseList<UserList> userLists) {
                super.gotUserListMemberships(userLists);
                for (UserList userList : userLists) {
                    userModels.add(new UserModel(userList.getUser().getId(),
                            userList.getUser().getBiggerProfileImageURL(),
                            userList.getUser().getName(),
                            "@" + userList.getUser().getScreenName(),
                            UsersData.getInstance().getFollowingList().contains(userList.getUser().getId()),
                            false, false));
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        asyncTwitter.getUserListMemberships(-1);

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        listConfig.applyConfig(getContext(), mRvStats);

//        mBtnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mActivity.onBackPressed();
//            }
//        });

        return viewHierarchy;
    }
}

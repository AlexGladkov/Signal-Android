package solonsky.signal.twitter.fragments;

import android.content.Intent;
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

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.MVPSearchActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Keys;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

/**
 * Created by neura on 05.07.17.
 */

public class StatsTweetsFragment extends Fragment {

    private StatusAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.fragment_stats, container, false);

        final LoggedActivity mActivity = (LoggedActivity) getActivity();

        ImageView mImgBackdrop = (ImageView) viewHierarchy.findViewById(R.id.img_profile_followers);
        ImageView mBtnBack = (ImageView) viewHierarchy.findViewById(R.id.btn_stats_back);
        TextView mTxtTitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) viewHierarchy.findViewById(R.id.txt_stats_subtitle);

        RecyclerView mRvStats = (RecyclerView) viewHierarchy.findViewById(R.id.recycler_stats);

        final ArrayList<StatusModel> mStatusList = new ArrayList<>();
        mAdapter = new StatusAdapter(mStatusList, mActivity, true, true,
                new StatusAdapter.StatusClickListener() {
                    @Override
                    public void onSearch(String searchText, View v) {
                        Intent searchIntent = new Intent(getContext(), MVPSearchActivity.class);
                        searchIntent.putExtra(Keys.SearchQuery.getValue(), searchText);
                        getActivity().startActivity(searchIntent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = mStatusList.indexOf(statusModel);
                mStatusList.remove(statusModel);
                mAdapter.notifyItemRemoved(position);
            }
        });

        if (AppData.ME != null) {
            Picasso.get().load(AppData.ME.getProfileBannerImageUrl()).into(mImgBackdrop);
            mTxtSubtitle.setText(getString(R.string.stats_tweets) + " " + AppData.ME.getStatusesCount());
            mTxtTitle.setText(AppData.ME.getName());

            final Handler handler = new Handler();
            final Gson gson = new Gson();
            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void gotUserTimeline(ResponseList<Status> statuses) {
                    super.gotUserTimeline(statuses);
                    for (Status status : statuses) {
                        StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                        statusModel.tuneModel(status);
                        mStatusList.add(statusModel);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });

            asyncTwitter.getUserTimeline(AppData.ME.getId(), new Paging(1, 300));
        }

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        listConfig.applyConfig(getContext(), mRvStats);
        return viewHierarchy;
    }
}

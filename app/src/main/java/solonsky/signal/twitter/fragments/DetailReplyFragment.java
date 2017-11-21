package solonsky.signal.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.databinding.FragmentDetailReplyBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.DetailListViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 04.06.17.
 */

public class DetailReplyFragment extends Fragment {

    private static final String TAG = DetailReplyFragment.class.getSimpleName();
    private ArrayList<StatusModel> mFeedList;
    private StatusAdapter mAdapter;
    private DetailListViewModel viewModel;
    private DetailActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDetailReplyBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_reply, container, false);

        mFeedList = new ArrayList<>();

        mActivity = (DetailActivity) getActivity();
        mAdapter = new StatusAdapter(mFeedList, (AppCompatActivity) getActivity(), false, true,
                mActivity.getStatusClickListener(), new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = mFeedList.indexOf(statusModel);
                mFeedList.remove(statusModel);
                mAdapter.notifyItemRemoved(position);
            }
        });

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasNestedScroll(false)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        viewModel = new DetailListViewModel(listConfig);
        viewModel.setState(AppData.UI_STATE_VISIBLE);
        binding.setModel(viewModel);

        return binding.getRoot();
    }

    public void loadApi() {
        final Handler handler = new Handler();
        final Gson gson = new Gson();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error searching - " + te.getLocalizedMessage());
            }

            @Override
            public void searched(QueryResult queryResult) {
                super.searched(queryResult);
                int repCount = 0;

                for (Status status : queryResult.getTweets()) {
                    if (status.getInReplyToStatusId() == AppData.CURRENT_STATUS_MODEL.getId()) {
                        StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                        statusModel.tuneModel(status);
                        statusModel.linkClarify();
                        mFeedList.add(statusModel);
                        repCount++;
                    }
                }

                final int finalRepCount = repCount;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.setReplyCount(finalRepCount);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        asyncTwitter.search(new Query("to:" + AppData.CURRENT_STATUS_MODEL.getUser().getScreenName()));
    }
}

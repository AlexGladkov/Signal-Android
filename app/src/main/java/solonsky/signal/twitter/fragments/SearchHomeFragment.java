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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.databinding.FragmentSearchHomeBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.SearchDetailViewModel;

/**
 * Created by neura on 03.06.17.
 */
public class SearchHomeFragment extends Fragment {
    private final String TAG = SearchHomeFragment.class.getSimpleName();
    private ArrayList<StatusModel> mTweetsList;
    private ArrayList<StatusModel> mSourceList = new ArrayList<>();
    private FragmentSearchHomeBinding  binding;

    private final int images = 4;
    private final int video = 5;
    private final int links = 6;
    private final int mentions = 7;
    private final int retweets = 1;

    private boolean isMentions = true;
    private boolean isRetweets = true;
    private boolean isImages = true;
    private boolean isVideo = true;
    private boolean isLinks = true;
    private SearchListener mCallback;
    private StatusAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mCallback != null)
                mCallback.updatePosition(3);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SearchListener) context;
        } catch (ClassCastException e) {
            // Do nothing
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void filterSource(int source, boolean value) {
        switch (source) {
            case images:
                isImages = value;
                break;

            case video:
                isVideo = value;
                break;

            case mentions:
                isMentions = value;
                break;

            case retweets:
                isRetweets = value;
                break;

            case links:
                isLinks = value;
                break;
        }

        mTweetsList = new ArrayList<>();
        for (StatusModel statusModel : mSourceList) {
            if (statusModel.isRetweet() && isRetweets) {
                mTweetsList.add(statusModel);
                continue;
            }

            if (statusModel.getInReplyToStatusId() == -1 && statusModel.getText().contains("@") && isMentions) {
                mTweetsList.add(statusModel);
                continue;
            }

            if (statusModel.getMediaEntities().size() > 0) {
                boolean isAdded = false;
                for (JsonElement media : statusModel.getMediaEntities()) {
                    JsonObject mediaEntity = (JsonObject) media;

                    if ((mediaEntity.get("type").getAsString().equals(Flags.MEDIA_PHOTO) ||
                        mediaEntity.get("type").getAsString().equals(Flags.MEDIA_GIF)) && isImages) {
                        mTweetsList.add(statusModel);
                        isAdded = true;
                        break;
                    }

                    if (mediaEntity.get("type").getAsString().equals(Flags.MEDIA_VIDEO) && isVideo) {
                        mTweetsList.add(statusModel);
                        isAdded = true;
                        break;
                    }
                }

                if (isAdded) continue;
            }

            if (statusModel.getUrlEntities().size() > 0 && isLinks) {
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

        SearchDetailViewModel viewModel = new SearchDetailViewModel(config);
        viewModel.setState(mTweetsList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
        binding.setModel(viewModel);
    }

    public void updateData(ArrayList<StatusModel> statusModels) {
        mTweetsList = new ArrayList<>();
        mTweetsList.addAll(statusModels);
        mSourceList.addAll(statusModels);

        TweetActions.verifyDividers(mTweetsList);
        setupList();
    }
}

package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.databinding.FragmentSearchMediaBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.SearchDetailViewModel;

/**
 * Created by neura on 03.06.17.
 */
public class SearchMediaFragment extends Fragment {
    private final String TAG = SearchMediaFragment.class.getSimpleName();
    private ArrayList<StatusModel> mTweetsList = new ArrayList<>();
    private ArrayList<StatusModel> mSourceList = new ArrayList<>();
    private StatusAdapter mAdapter;
    private SearchDetailViewModel viewModel;
    private FragmentSearchMediaBinding binding;

    private final int images = 4;
    private final int video = 5;
    private final int links = 6;

    private boolean isImages = true;
    private boolean isVideo = true;
    private boolean isLinks = true;
    private SearchListener mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_media, container, false);


//        mAdapter = new StatusAdapter(mTweetsList, (AppCompatActivity) getActivity(), true,
//                false, new StatusAdapter.StatusClickListener() {
//            @Override
//            public void onSearch(String searchText, View v) {
//                if (mCallback != null)
//                    mCallback.startSearch(searchText);
//            }
//        }, new TweetActions.MoreCallback() {
//            @Override
//            public void onDelete(StatusModel statusModel) {
//                int position = mTweetsList.indexOf(statusModel);
//                mTweetsList.remove(statusModel);
//                mAdapter.notifyItemRemoved(position);
//            }
//        });
//
//        ListConfig config = new ListConfig.Builder(mAdapter)
//                .setHasFixedSize(true)
//                .setDefaultDividerEnabled(true)
//                .setHasNestedScroll(false)
//                .build(getContext());
//
//        viewModel = new SearchDetailViewModel(config);
//        binding.setModel(viewModel);
        return binding.getRoot();
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
                mCallback.updatePosition(2);
        }
    }

    public void filterSource(int source, boolean value) {
        switch (source) {
            case images:
                isImages = value;
                break;

            case video:
                isVideo = value;
                break;

            case links:
                isLinks = value;
                break;
        }

        mTweetsList = new ArrayList<>();

        for (StatusModel statusModel : mSourceList) {
            boolean hasMedia = statusModel.getMediaEntities().size() > 0;

            if (hasMedia) {
                for (JsonElement mediaEntity : statusModel.getMediaEntities()) {
                    JsonObject media = (JsonObject) mediaEntity;
                    if (((media.get("type").getAsString().equals("photo") ||
                            media.get("type").getAsString().equals("animated-gif")) && isImages) ||
                            (media.get("type").getAsString().equals("video") && isVideo) ||
                            (statusModel.getUrlEntities().size() > 0 && isLinks)) {
                        mTweetsList.add(statusModel);
                    }
                }
            } else if (statusModel.getUrlEntities().size() > 0 && isLinks) {
                mTweetsList.add(statusModel);
            }
        }

        TweetActions.verifyDividers(mTweetsList);
        mAdapter.notifyDataSetChanged();
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

        viewModel = new SearchDetailViewModel(config);
        viewModel.setState(mTweetsList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);

        binding.setModel(viewModel);
    }

    public void updateData(ArrayList<StatusModel> statusModels) {
        mSourceList.addAll(statusModels);
        mTweetsList.addAll(statusModels);

        TweetActions.verifyDividers(mTweetsList);

        mAdapter.notifyDataSetChanged();
        viewModel.setState(mTweetsList.size() == 0 ? AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
    }
}

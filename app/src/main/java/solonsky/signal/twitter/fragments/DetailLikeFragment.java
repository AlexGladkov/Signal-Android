package solonsky.signal.twitter.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.databinding.FragmentDetailLikeBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.HTML;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.viewmodels.DetailListViewModel;

/**
 * Created by neura on 04.06.17.
 */

public class DetailLikeFragment extends Fragment {
    private final String TAG = DetailLikeFragment.class.getSimpleName();
    private ArrayList<UserModel> mUsersList;
    private UserAdapter mAdapter;
    private DetailListViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDetailLikeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_like, container, false);

        mUsersList = new ArrayList<>();



        mAdapter = new UserAdapter(mUsersList, getContext(), (AppCompatActivity) getActivity(),
                new UserAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {

            }
        });

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasNestedScroll(false)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(true)
                .build(getContext());

        viewModel = new DetailListViewModel(listConfig);
        binding.setModel(viewModel);

        loadFavorites();
        return binding.getRoot();
    }

    private void loadFavorites() {
        String link = "https://twitter.com/i/activity/favorited_popup?id=" + AppData.CURRENT_STATUS_MODEL.getId();
        Log.e(TAG, "Link " + link);
        new HTML(getContext(), new HTML.HtmlRequestHandler() {
            @Override
            public void onUserIds(ArrayList<Long> ids) {
                Log.e(TAG, "favorites size - " + ids.size());
//                for (Long id : ids) {
//                    if (!uniqueIds.contains(id)) uniqueIds.add(id);
//                }
//
//                currentStatusCount++;
//                completeUserRequest(uniqueIds);
            }

            @Override
            public void onFailure() {
//                currentStatusCount++;
//                completeUserRequest(uniqueIds);
            }
        }).extractFavoriteUsers(link);
    }
}

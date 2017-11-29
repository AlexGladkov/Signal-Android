package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.NotificationsDetailAdapter;
import solonsky.signal.twitter.data.NotificationsFollowData;
import solonsky.signal.twitter.databinding.FragmentNotificationsFollowBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.viewmodels.NotificationsDetailViewModel;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsFollowFragment extends Fragment implements FragmentCounterListener {
    private NotificationsDetailAdapter mAdapter;
    private LoggedActivity mActivity;
    private FragmentNotificationsFollowBinding binding;
    private NotificationsFragment mParent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_notifications_follow, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (LoggedActivity) getActivity();
        mParent = (NotificationsFragment) getParentFragment();
        mAdapter = new NotificationsDetailAdapter(NotificationsFollowData.getInstance().getFollowList(), getContext(), clickListener);
        mAdapter.setHasStableIds(true);
        NotificationsDetailViewModel viewModel = new NotificationsDetailViewModel(mAdapter, getContext());
        NotificationsFollowData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                mAdapter.notifyDataSetChanged();
                binding.txtNotificationNoData.setVisibility(NotificationsFollowData.getInstance()
                    .getFollowList().size() == 0 ? View.VISIBLE : View.GONE);

                if (mParent.getUpdateHandler() != null)
                    mParent.getUpdateHandler().onUpdate();
            }
        });

        binding.setModel(viewModel);
    }

//    private void loadCache() {
//        final Handler handler = new Handler();
//        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
//        }.getType();
//        Reservoir.getAsync(Cache.NotificationsFollow + String.valueOf(AppData.ME.getId()), resultType,
//                new ReservoirGetCallback<List<NotificationDetailModel>>() {
//                    @Override
//                    public void onSuccess(final List<NotificationDetailModel> notificationDetailModels) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                NotificationsFollowData.getInstance().getFollowList().addAll(notificationDetailModels);
//
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (NotificationsFollowData.getInstance().getUpdateHandler() != null)
//                                            NotificationsFollowData.getInstance().getUpdateHandler().onUpdate();
//                                    }
//                                });
//                            }
//                        }).start();
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//
//                    }
//                });
//    }

    private NotificationsDetailAdapter.DetailClickListener clickListener = new NotificationsDetailAdapter.DetailClickListener() {
        @Override
        public void onItemClick(NotificationDetailModel model, View v) {
            if (model.getUser() != null) {
                Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                profileIntent.putExtra(Flags.PROFILE_DATA, model.getUser());
                getActivity().startActivity(profileIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) onScrollToTop();
    }

    @Override
    public void onScrollToTop() {
        binding.notificationRecyclerMain.scrollToPosition(0);
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, int pauseDuration, int endDuration) {
        binding.notificationRecyclerMain.smoothScrollToPosition(0);
    }

    @Override
    public void onBackToPosition() {

    }

    @Override
    public void onUpdate() {

    }
}

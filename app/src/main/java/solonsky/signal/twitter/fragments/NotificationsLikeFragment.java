package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.NotificationsDetailAdapter;
import solonsky.signal.twitter.data.NotificationsLikeData;
import solonsky.signal.twitter.databinding.FragmentNotificationsLikeBinding;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.viewmodels.NotificationsDetailViewModel;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsLikeFragment extends Fragment implements FragmentCounterListener {
    private final String TAG = NotificationsLikeFragment.class.getSimpleName();
    private NotificationsDetailAdapter mAdapter;
    private LoggedActivity mActivity;
    private FragmentNotificationsLikeBinding binding;
    private NotificationsFragment mParent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_notifications_like, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (LoggedActivity) getActivity();
        mParent = (NotificationsFragment) getParentFragment();
        mAdapter = new NotificationsDetailAdapter(NotificationsLikeData.getInstance().getLikesList(), getContext(), clickListener);
        NotificationsDetailViewModel viewModel = new NotificationsDetailViewModel(mAdapter, getContext());
        binding.setModel(viewModel);

        NotificationsLikeData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                mAdapter.notifyDataSetChanged();
                binding.txtNotificationNoData.setVisibility(NotificationsLikeData.getInstance()
                        .getLikesList().size() == 0 ? View.VISIBLE : View.GONE);

                if (mParent.getUpdateHandler() != null)
                    mParent.getUpdateHandler().onUpdate();
            }
        });
    }

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

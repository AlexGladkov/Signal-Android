package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.NotificationsDetailAdapter;
import solonsky.signal.twitter.data.NotificationsRetweetData;
import solonsky.signal.twitter.databinding.FragmentNotificationsRetweetBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.viewmodels.NotificationsDetailViewModel;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsRetweetFragment extends Fragment implements FragmentCounterListener {
    private NotificationsDetailAdapter mAdapter;
    private LoggedActivity mActivity;
    private FragmentNotificationsRetweetBinding binding;
    private NotificationsFragment mParent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_notifications_retweet, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (LoggedActivity) getActivity();
        mParent = (NotificationsFragment) getParentFragment();
        mAdapter = new NotificationsDetailAdapter(NotificationsRetweetData.getInstance().getRetweetList(), getContext(), clickListener);

        final NotificationsDetailViewModel viewModel = new NotificationsDetailViewModel(mAdapter, getContext());
        binding.setModel(viewModel);

        NotificationsRetweetData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                viewModel.setState(NotificationsRetweetData.getInstance().getRetweetList().size() == 0 ?
                    AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
                mAdapter.notifyDataSetChanged();
                if (mParent.getUpdateHandler() != null)
                    mParent.getUpdateHandler().onUpdate();
            }
        });

        if (NotificationsRetweetData.getInstance().getRetweetList().size() == 0) {
            loadCache();
        }
    }

    private void loadCache() {
        final Handler handler = new Handler();
        Type resultType = new TypeToken<List<NotificationDetailModel>>() {
        }.getType();
        Reservoir.getAsync(Cache.NotificationsRetweet + String.valueOf(AppData.ME.getId()), resultType,
                new ReservoirGetCallback<List<NotificationDetailModel>>() {
                    @Override
                    public void onSuccess(final List<NotificationDetailModel> notificationDetailModels) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationsRetweetData.getInstance().getRetweetList().addAll(notificationDetailModels);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (NotificationsRetweetData.getInstance().getUpdateHandler() != null)
                                            NotificationsRetweetData.getInstance().getUpdateHandler().onUpdate();
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Exception e) {

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

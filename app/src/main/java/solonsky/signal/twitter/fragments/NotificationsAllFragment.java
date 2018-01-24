package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ConcurrentModificationException;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.NotificationsMainAdapter;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.data.NotificationsAllData;
import solonsky.signal.twitter.databinding.FragmentNotificationsAllBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.models.NotificationModel;
import solonsky.signal.twitter.viewmodels.NotificationsMainViewModel;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsAllFragment extends Fragment implements FragmentCounterListener {
    private NotificationsMainAdapter mAdapter;
    private final String TAG = NotificationsAllFragment.class.getSimpleName();
    private FragmentNotificationsAllBinding binding;
    private ActivityListener mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new NotificationsMainAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_notifications_all, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NotificationsFragment mParentFragment = (NotificationsFragment) getParentFragment();

        mAdapter.setData(NotificationsAllData.getInstance().getDataList());
        mAdapter.attachListener(clickListener);
        NotificationsMainViewModel viewModel = new NotificationsMainViewModel(mAdapter, getContext());

        binding.setModel(viewModel);
        binding.recyclerNotificationsAll.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    NotificationsAllData.getInstance().saveCache(TAG);
                                } catch (ConcurrentModificationException | NullPointerException e) {
                                    Log.e(TAG, "Not this time luke");
                                }
                            }
                        }).start();
                        break;
                }
            }
        });

        binding.recyclerNotificationsAll.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

//        binding.recyclerNotificationsAll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerNotificationsAll.getLayoutManager();
//                int hiddenPosition = linearLayoutManager.findFirstVisibleItemPosition();
//
//                if (hiddenPosition > 0 && NotificationsAllData.getInstance().getDataList().get(hiddenPosition - 1).isHighlighted()) {
//                    NotificationsAllData.getInstance().getDataList().get(hiddenPosition - 1).setHighlighted(false);
//                    NotificationsAllData.getInstance().decEntryCount();
//
//                    if (NotificationsAllData.getInstance().getEntryCount() == 0) {
//                        LoggedData.getInstance().setNewActivity(false);
//                        LoggedData.getInstance().getUpdateHandler().onUpdate();
//                    }
//
//                    if (mParentFragment.getUpdateHandler() != null) {
//                        mParentFragment.getUpdateHandler().onUpdate();
//                    }
//                }
//            }
//        });

        binding.srlNotificationAll.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NotificationsAllData.getInstance().clearNew();
                LoggedData.getInstance().setNewActivity(false);
                LoggedData.getInstance().getUpdateHandler().onUpdate();

                if (mCallback != null)
                    mCallback.updateCounter(0);

                binding.srlNotificationAll.setRefreshing(false);
            }
        });
        binding.srlNotificationAll.setProgressViewOffset(false, 0, (int) Utilities.convertDpToPixel(16, getContext()));

        NotificationsAllData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                mAdapter.setData(NotificationsAllData.getInstance().getDataList());
                binding.txtNotificationNoData.setVisibility(NotificationsAllData.getInstance()
                            .getDataList().size() == 0 ? View.VISIBLE : View.GONE);

                if (mParentFragment.getUpdateHandler() != null)
                    mParentFragment.getUpdateHandler().onUpdate();
            }
        });
    }

    private NotificationsMainAdapter.ItemClickListener clickListener = new NotificationsMainAdapter.ItemClickListener() {
        @Override
        public void onItemClick(NotificationModel model, View v) {
            switch (model.getType()) {
                case NotificationModel.TYPE_REPLY:
                    if (model.getStatusModel() != null) {
                        AppData.CURRENT_STATUS_MODEL = (model.getStatusModel());
                        getActivity().startActivity(new Intent(getContext(), DetailActivity.class));
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;

                case NotificationModel.TYPE_RT:
                    if (model.getStatusModel() != null) {
                        AppData.CURRENT_STATUS_MODEL = (model.getStatusModel());
                        getActivity().startActivity(new Intent(getContext(), DetailActivity.class));
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;

                case NotificationModel.TYPE_LIKE:
                    if (model.getStatusModel() != null) {
                        AppData.CURRENT_STATUS_MODEL = (model.getStatusModel());
                        getActivity().startActivity(new Intent(getContext(), DetailActivity.class));
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;

                case NotificationModel.TYPE_FOLLOW:
                    if (model.getUser() != null) {
                        Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                        profileIntent.putExtra(Flags.PROFILE_DATA, model.getUser());
                        getActivity().startActivity(profileIntent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;

                case NotificationModel.TYPE_LIST:
                    if (model.getUser() != null) {
                        Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                        profileIntent.putExtra(Flags.PROFILE_DATA, model.getUser());
                        getActivity().startActivity(profileIntent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;

                case NotificationModel.TYPE_QUOTE:
                    if (model.getStatusModel() != null) {
                        AppData.CURRENT_STATUS_MODEL = (model.getStatusModel());
                        getActivity().startActivity(new Intent(getContext(), DetailActivity.class));
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    break;
            }
        }

        @Override
        public void onAvatarClick(NotificationModel model, View v) {
            if (model.getUser() != null) {
                Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                profileIntent.putExtra(Flags.PROFILE_DATA, model.getUser());
                getActivity().startActivity(profileIntent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ActivityListener) context;
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
        if (hidden) {
            binding.srlNotificationAll.setRefreshing(false);
        } else {
            onScrollToTop();
        }
    }

    @Override
    public void onScrollToTop() {
        binding.recyclerNotificationsAll.scrollToPosition(0);
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, int pauseDuration, int endDuration) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerNotificationsAll.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        if (!isHidden()) {
            if (position > 8) {
                binding.recyclerNotificationsAll.scrollToPosition(8);
                binding.recyclerNotificationsAll.smoothScrollToPosition(0);
            } else {
                binding.recyclerNotificationsAll.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    public void onBackToPosition() {

    }

    @Override
    public void onUpdate() {

    }
}

package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.RemoveAdapter;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.databinding.FragmentMuteBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.viewmodels.MuteViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.User;

/**
 * Created by neura on 26.05.17.
 */

public class MuteFragment extends Fragment {
    private final String TAG = MuteFragment.class.getSimpleName();
    private MuteViewModel viewModel;

    private RemoveAdapter mUsersAdapter;
    private RemoveAdapter mHashtagsAdapter;
    private RemoveAdapter mKeywordsAdapter;
    private RemoveAdapter mClientsAdapter;

    private ActivityListener mCallback;

    private boolean isFirst = true;
    private FragmentMuteBinding binding;

    private RemoveAdapter.RemoveClickListener usersClickHandler = new RemoveAdapter.RemoveClickListener() {
        @Override
        public void onDeleteClick(View view, RemoveModel removeModel) {
            int position = MuteData.getInstance().getmUsersList().indexOf(removeModel);
            MuteData.getInstance().getmUsersList().remove(removeModel);
            mUsersAdapter.notifyItemRemoved(position);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.recyclerMuteUsers.invalidate();
                    binding.recyclerMuteUsers.requestLayout();
                    viewModel.setHasUsers(MuteData.getInstance().getmUsersList().size() != 0);
                }
            }, 200);

            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.destroyMute(removeModel.getId());
        }
    };

    private RemoveAdapter.RemoveClickListener keywordsClickHandler = new RemoveAdapter.RemoveClickListener() {
        @Override
        public void onDeleteClick(View view, RemoveModel removeModel) {
            int position = MuteData.getInstance().getmKeywordsList().indexOf(removeModel);
            MuteData.getInstance().getmKeywordsList().remove(removeModel);
            mKeywordsAdapter.notifyItemRemoved(position);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.recyclerMuteKeywords.invalidate();
                    binding.recyclerMuteKeywords.requestLayout();
                    viewModel.setHasKeywords(MuteData.getInstance().getmKeywordsList().size() != 0);
                }
            }, 200);

            MuteData.getInstance().saveCache();
        }
    };

    private RemoveAdapter.RemoveClickListener hashtagClickHandler = new RemoveAdapter.RemoveClickListener() {
        @Override
        public void onDeleteClick(View view, RemoveModel removeModel) {
            int position = MuteData.getInstance().getmHashtagsList().indexOf(removeModel);
            MuteData.getInstance().getmHashtagsList().remove(removeModel);
            mHashtagsAdapter.notifyItemRemoved(position);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.recyclerMuteHashtags.invalidate();
                    binding.recyclerMuteHashtags.requestLayout();
                    viewModel.setHasHashs(MuteData.getInstance().getmHashtagsList().size() != 0);
                }
            }, 200);

            MuteData.getInstance().saveCache();
        }
    };

    private RemoveAdapter.RemoveClickListener clientsClickHandler = new RemoveAdapter.RemoveClickListener() {
        @Override
        public void onDeleteClick(View view, RemoveModel removeModel) {
            int position = MuteData.getInstance().getmClientsList().indexOf(removeModel);
            MuteData.getInstance().getmClientsList().remove(removeModel);
            mClientsAdapter.notifyItemRemoved(position);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.recyclerMuteClients.invalidate();
                    binding.recyclerMuteClients.requestLayout();
                    viewModel.setHasClients(MuteData.getInstance().getmClientsList().size() != 0);
                }
            }, 200);

            MuteData.getInstance().saveCache();
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mute, container, false);

        mUsersAdapter = new RemoveAdapter(MuteData.getInstance().getmUsersList(), getContext(), usersClickHandler);
        mKeywordsAdapter = new RemoveAdapter(MuteData.getInstance().getmKeywordsList(), getContext(), keywordsClickHandler);
        mHashtagsAdapter = new RemoveAdapter(MuteData.getInstance().getmHashtagsList(), getContext(), hashtagClickHandler);
        mClientsAdapter = new RemoveAdapter(MuteData.getInstance().getmClientsList(), getContext(), clientsClickHandler);

        mUsersAdapter.setHasStableIds(true);
        mKeywordsAdapter.setHasStableIds(true);
        mHashtagsAdapter.setHasStableIds(true);
        mClientsAdapter.setHasStableIds(true);

        viewModel = new MuteViewModel(mUsersAdapter, mKeywordsAdapter, mHashtagsAdapter, mClientsAdapter, getContext());
        binding.setModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mCallback != null) {
            mCallback.updateCounter(0);
            mCallback.updateSettings(R.string.title_mute, true, true);
            mCallback.updateToolbarState(AppData.TOOLBAR_LOOGED_MUTE, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ActivityListener) context;
        } catch (ClassCastException e) {
            // Do nothing
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
            if (mCallback != null) {
                mCallback.updateCounter(0);
                mCallback.updateSettings(R.string.title_mute, false, false);
                mCallback.updateToolbarState(AppData.TOOLBAR_LOOGED_MUTE, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFirst) {
            loadData();
        }
        isFirst = false;
    }

    private void loadData() {
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!MuteData.getInstance().isCacheLoaded()) {
                    MuteData.getInstance().loadCache();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUsersAdapter.notifyDataSetChanged();
                        mClientsAdapter.notifyDataSetChanged();
                        mHashtagsAdapter.notifyDataSetChanged();
                        mKeywordsAdapter.notifyDataSetChanged();

                        viewModel.setHasClients(MuteData.getInstance().getmClientsList().size() > 0);
                        viewModel.setHasKeywords(MuteData.getInstance().getmKeywordsList().size() > 0);
                        viewModel.setHasHashs(MuteData.getInstance().getmHashtagsList().size() > 0);
                        viewModel.setHasUsers(MuteData.getInstance().getmUsersList().size() > 0);

                        asyncTwitter.getMutesList(-1);
                    }
                });
            }
        }).start();


        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotMutesList(ResponseList<User> blockingUsers) {
                super.gotMutesList(blockingUsers);
                for (User user : blockingUsers) {
                    RemoveModel removeModel = new RemoveModel(user.getId(), "@" + user.getScreenName());

                    if (!MuteData.getInstance().getmUsersList().contains(removeModel)) {
                        MuteData.getInstance().getmUsersList().add(removeModel);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUsersAdapter.notifyDataSetChanged();
                        viewModel.setHasUsers(MuteData.getInstance().getmUsersList().size() > 0);
                    }
                });
            }
        });
    }
}

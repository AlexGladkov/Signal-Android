package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.common.reflect.TypeToken;

import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.DirectAdapter;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.data.DirectData;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.databinding.FragmentDirectBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.DirectModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.viewmodels.DirectViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 23.05.17.
 */

public class DirectFragment extends Fragment {
    private final String TAG = DirectFragment.class.getSimpleName();
    private int newCount = 0;
    private DirectAdapter mAdapter;
    private DirectViewModel viewModel;
    private ActivityListener mCallback;

    private RecyclerView.OnScrollListener directScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mCallback != null) {
                mCallback.updateBars(dy);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDirectBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_direct, container, false);

        mAdapter = new DirectAdapter(DirectData.getInstance().getmMessagesList(), getContext(), (AppCompatActivity) getActivity(),
                new DirectAdapter.DirectClickHandler() {
                    @Override
                    public void onItemClick(View v, final DirectModel model) {
                        AppData.DM_POSITION = DirectData.getInstance().getmMessagesList().indexOf(model);
                        AppData.DM_SELECTED_USER = model.getUsername();
                        AppData.DM_OTHER_ID = model.getOtherId();
                        Flags.DM_IS_NEW = model.getMessageCount() > 0;

                        DirectApi.getInstance().clear();
                        DirectApi.getInstance().setUserId(model.getOtherId());
                        getActivity().startActivity(new Intent(getContext(), ChatActivity.class));
                        if (model.isHighlighted()) {
                            getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
                        } else {
                            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                model.setHighlighted(false);

                                model.setMessageCount(0);
                                boolean hasNew = false;
                                for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                                    if (directModel.getMessageCount() > 0) {
                                        hasNew = true;
                                        break;
                                    }
                                }

                                if (!hasNew) {
                                    LoggedData.getInstance().setNewMessage(false);
                                    LoggedData.getInstance().getUpdateHandler().onUpdate();
                                }

                                saveCache();
                            }
                        }, 300);
                    }

                    @Override
                    public void onAvatarClick(View v, DirectModel directModel) {
                        Intent profileIntent = new Intent(getContext(), MVPProfileActivity.class);
                        profileIntent.putExtra(Flags.PROFILE_ID, directModel.getOtherId());
                        getActivity().startActivity(profileIntent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
        mAdapter.setHasStableIds(true);

        if (DirectData.getInstance().getmMessagesList().size() == 0) {
            viewModel = new DirectViewModel(mAdapter, getContext(), AppData.UI_STATE_LOADING);
        } else {
            viewModel = new DirectViewModel(mAdapter, getContext(), AppData.UI_STATE_VISIBLE);
        }
        binding.setModel(viewModel);

        if (mCallback != null) {
            mCallback.updateCounter(DirectData.getInstance().getEntryCount());
            mCallback.updateSettings(R.string.title_direct, false, false);
            mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_DM, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
        }

        boolean isNight = App.getInstance().isNightEnabled();

//        for (int i = 0; i < mMessagesList.size(); i++) {
//            if (i == mMessagesList.size() - 1) {
//                mMessagesList.get(i).setDivideState(AppData.DIVIDER_LONG);
//            } else {
//                mMessagesList.get(i).setDivideState(
//                        mMessagesList.get(i).isHighlighted() && mMessagesList.get(i + 1).isHighlighted()
//                                || !mMessagesList.get(i).isHighlighted() && !mMessagesList.get(i + 1).isHighlighted()
//                                ? AppData.DIVIDER_SHORT : isNight ? AppData.DIVIDER_NONE : AppData.DIVIDER_LONG);
//            }
//        }

        binding.recyclerDirect.setOnScrollListener(directScrollListener);
        DirectData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                mAdapter.notifyDataSetChanged();
                saveCache();
            }
        });

        if (DirectData.getInstance().getmMessagesList().size() == 0) {
            loadCache();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (isAdded() && Flags.needsToRedrawDirect) {
                Flags.needsToRedrawDirect = false;
                mAdapter.notifyDataSetChanged();
            }

            if (Flags.isUpdated) {
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
            }
            if (Flags.DELETE_THREAD) {
                Flags.DELETE_THREAD = false;
                mAdapter.notifyItemRemoved(AppData.DM_POSITION);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (ActivityListener) context;
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
                mCallback.updateCounter(DirectData.getInstance().getEntryCount());
                mCallback.updateSettings(R.string.title_direct, true, true);
                mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_DM, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
            }
        }
    }

    /**
     * Load cache from disk
     *
     * @serialData tweets array
     */
    private void loadCache() {
        Type resultType = new TypeToken<List<DirectModel>>() {
        }.getType();
        Reservoir.getAsync(Cache.DirectsList + String.valueOf(AppData.ME.getId()), resultType,
                new ReservoirGetCallback<List<DirectModel>>() {
                    @Override
                    public void onFailure(Exception e) {
                        loadApi(new Paging(1, 200));
                    }

                    @Override
                    public void onSuccess(List<DirectModel> directModels) {
                        for (DirectModel directModel : directModels) {
                            DirectData.getInstance().getmMessagesList().add(directModel);
                        }

                        mAdapter.notifyDataSetChanged();
                        viewModel.setState(DirectData.getInstance().getmMessagesList().size() == 0 ?
                                AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
                        loadApi(new Paging(1, 200));
                    }
                });
    }

    /**
     * Save cache
     *
     * @serialData bytecode to disk
     */
    private void saveCache() {
        try {
            Reservoir.put(Cache.DirectsList + String.valueOf(AppData.ME.getId()), DirectData.getInstance().getmMessagesList());
        } catch (IOException e) {
            Log.e(TAG, "Error caching directs - " + e.getLocalizedMessage());
        }
    }

    /**
     * Loading direct messages from twitter backend
     */
    public void loadApi(final Paging paging) {
        final Handler handler = new Handler();
        final ArrayList<twitter4j.DirectMessage> loadedDirects = new ArrayList<>();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotDirectMessages(ResponseList<twitter4j.DirectMessage> messages) {
                super.gotDirectMessages(messages);
                loadedDirects.addAll(messages);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        asyncTwitter.getSentDirectMessages(paging);
                    }
                });
            }

            @Override
            public void gotSentDirectMessages(ResponseList<twitter4j.DirectMessage> messages) {
                super.gotSentDirectMessages(messages);
                loadedDirects.addAll(messages);

                Collections.sort(loadedDirects, new Comparator<twitter4j.DirectMessage>() {
                    @Override
                    public int compare(twitter4j.DirectMessage o1, twitter4j.DirectMessage o2) {
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    }
                });

                List<twitter4j.DirectMessage> newDirects = new ArrayList<>();
                List<DirectMessage> newUserDirects = new ArrayList<>();

                if (DirectData.getInstance().getmMessagesList().size() == 0) {
                    for (twitter4j.DirectMessage directMessage : loadedDirects) {
                        long id = directMessage.getSenderId() == AppData.ME.getId() ? directMessage.getRecipientId() : directMessage.getSenderId();
                        solonsky.signal.twitter.models.User user = User.getFromUserInstance(directMessage.getSenderId() == AppData.ME.getId() ?
                                directMessage.getRecipient() : directMessage.getSender());

                        DirectModel directModel = new DirectModel(directMessage.getId(), id, user.getOriginalProfileImageURL(),
                                user.getName(), directMessage.getText(), new LocalDateTime(directMessage.getCreatedAt()),
                                false);
                        if (!DirectData.getInstance().getmMessagesList().contains(directModel)) {
                            DirectData.getInstance().getmMessagesList().add(directModel);
                        }
                    }
                } else {
                    for (twitter4j.DirectMessage directMessage : loadedDirects) {
                        boolean hasId = false;
                        int position = 0;
                        for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                            if (directMessage.getSenderId() == directModel.getOtherId() ||
                                    directMessage.getRecipientId() == directModel.getOtherId()) {
                                hasId = true;
                                position = DirectData.getInstance().getmMessagesList().indexOf(directModel);
                                break;
                            }
                        }

                        if (hasId) {
                            if (directMessage.getId() > DirectData.getInstance().getmMessagesList().get(position).getId()) {
                                newDirects.add(directMessage);
                            }
                        } else {
                            newUserDirects.add(directMessage);
                        }
                    }

                    for (twitter4j.DirectMessage directMessage : newDirects) {
                        long id = AppData.ME.getId() == directMessage.getSenderId() ?
                                directMessage.getRecipientId() : directMessage.getSenderId();

                        solonsky.signal.twitter.models.User user = AppData.ME.getId() == directMessage.getSenderId() ?
                                User.getFromUserInstance(directMessage.getRecipient()) :
                                User.getFromUserInstance(directMessage.getSender());

                        int entryCount = 0;
                        for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                            if (directModel.getOtherId() == directMessage.getSenderId()
                                    || directModel.getOtherId() == directMessage.getRecipientId()) {
                                entryCount = directModel.getMessageCount();
                                DirectData.getInstance().getmMessagesList().remove(
                                        DirectData.getInstance().getmMessagesList().indexOf(directModel));
                                break;
                            }
                        }

                        DirectModel directModel = new DirectModel(
                                directMessage.getId(), id, user.getOriginalProfileImageURL(),
                                user.getName(), directMessage.getText(), new LocalDateTime(directMessage.getCreatedAt()), false);
                        if (directMessage.getSenderId() != AppData.ME.getId())
                            directModel.setMessageCount(entryCount + 1);
                        DirectData.getInstance().getmMessagesList().add(0, directModel);
                    }

                    for (DirectMessage directMessage : newUserDirects) {
                        long id = AppData.ME.getId() == directMessage.getSenderId() ?
                                directMessage.getRecipientId() : directMessage.getSenderId();
                        boolean hasId = false;

                        for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                            if (directModel.getOtherId() == id) {
                                hasId = true;
                                break;
                            }
                        }

                        if (!hasId) {
                            twitter4j.User parsedUser = AppData.ME.getId() == directMessage.getSenderId() ?
                                    directMessage.getRecipient() : directMessage.getSender();
                            User user = User.getFromUserInstance(parsedUser);
                            DirectModel directModel = new DirectModel(
                                    directMessage.getId(), id, user.getOriginalProfileImageURL(),
                                    user.getName(), directMessage.getText(), new LocalDateTime(directMessage.getCreatedAt()), false);
                            if (!DirectData.getInstance().getmMessagesList().contains(directModel)) {
                                DirectData.getInstance().getmMessagesList().add(directModel);
                            }
                        }
                    }
                }

                boolean hasNew = false;
                for (DirectModel directModel : DirectData.getInstance().getmMessagesList()) {
                    if (directModel.getMessageCount() > 0) {
                        hasNew = true;
                        break;
                    }
                }

                LoggedData.getInstance().setNewMessage(hasNew);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        viewModel.setState(DirectData.getInstance().getmMessagesList().size() == 0 ?
                                AppData.UI_STATE_NO_ITEMS : AppData.UI_STATE_VISIBLE);
                        LoggedData.getInstance().getUpdateHandler().onUpdate();
                    }
                });

//                saveCache();
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                Log.e(TAG, "Error while loading data - " + te.getLocalizedMessage());
//                Toast.makeText(getContext(), "Error while loading data - " + te.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        asyncTwitter.getDirectMessages(paging);
    }
}

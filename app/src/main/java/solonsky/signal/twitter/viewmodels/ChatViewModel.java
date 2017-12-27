package solonsky.signal.twitter.viewmodels;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.adapters.ChatAdapter;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.models.ChatModel;

/**
 * Created by neura on 23.05.17.
 */

public class ChatViewModel extends BaseObservable {
    private final String TAG = ChatViewModel.class.getSimpleName();
    private ChatAdapter chatAdapter;
    private ListConfig listConfig;
    private String title;
    private int state;
    private boolean isEnabled;

    public interface ChatClickHandler {
        void onSendClick(View v);
        void onBackClick(View v);
        void onDestroyClick(View v);
    }

    public ChatViewModel(final ChatActivity mActivity, String title, int state) {
        this.title = title;
        this.state = state;
        this.isEnabled = false;
        this.chatAdapter = new ChatAdapter(DirectApi.getInstance().getChatModels(),
                mActivity.getApplicationContext(), mActivity, new ChatAdapter.ChatClickListener() {
            @Override
            public void onItemClick(View v, final ChatModel chatModel) {
            }

            @Override
            public void onAvatarClick(View v, ChatModel chatModel) {
                Intent intent = new Intent(mActivity, MVPProfileActivity.class);
                intent.putExtra(Flags.PROFILE_ID, chatModel.getSenderId());
                mActivity.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        this.listConfig = new ListConfig.Builder(chatAdapter)
                .setHasNestedScroll(false)
                .setHasFixedSize(true)
                .setDefaultDividerEnabled(true)
                .build(mActivity.getApplicationContext());
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setEnabled(start + count > 0);
    }

    @Bindable
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public ListConfig getListConfig() {
        return listConfig;
    }

    public void setListConfig(ListConfig listConfig) {
        this.listConfig = listConfig;
        notifyPropertyChanged(BR.listConfig);
    }

    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }
}

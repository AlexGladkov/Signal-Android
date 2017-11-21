package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.ProfileActivity;
import solonsky.signal.twitter.adapters.ChatAdapter;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.ChatModel;
import solonsky.signal.twitter.overlays.ImageOverlay;
import twitter4j.Twitter;
import twitter4j.TwitterException;

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
//                switch (chatModel.getMediaType()) {
//                    case IMAGE:
//                        if (!TextUtils.isEmpty(chatModel.getImageUrl())) {
//                            final Handler handler = new Handler();
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Twitter twitter = Utilities.getTwitterInstance();
//                                    try {
//                                        InputStream stream = twitter.getDMImageAsStream(chatModel.getImageUrl());
//                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
//                                        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
//                                        final ArrayList<Bitmap> bitmaps = new ArrayList<>();
//                                        bitmaps.add(bmp);
//
//                                        handler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                final solonsky.signal.twitter.overlays.ImageOverlay imageOverlay = new ImageOverlay(bitmaps, mActivity);
//                                                imageOverlay.setImageOverlayClickHandler(new ImageOverlay.ImageOverlayClickHandler() {
//                                                    @Override
//                                                    public void onBackClick(View v) {
//                                                        imageOverlay.getImageViewer().onDismiss();
//                                                    }
//
//                                                    @Override
//                                                    public void onSaveClick(View v, String url) {
//                                                        DownloadFiles downloadFiles = new DownloadFiles(mActivity);
//                                                        downloadFiles.saveFile(url, mActivity.getString(R.string.app_name));
//                                                    }
//                                                });
//                                            }
//                                        });
//                                    } catch (TwitterException e) {
//                                        Log.e(TAG, "Error to load - " + e.getLocalizedMessage());
//                                    }
//                                }
//                            }).start();
//                        }
//                        break;
//
//                    case VIDEO:
//                        break;
//
//                    case YOUTUBE:
//                        break;
//                }
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

package solonsky.signal.twitter.activities;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.google.android.exoplayer.util.Util;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.databinding.ActivityChatBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.UpdateAddHandler;
import solonsky.signal.twitter.models.ChatModel;
import solonsky.signal.twitter.viewmodels.ChatViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 23.05.17.
 * Performs chat interaction activity
 */

public class ChatActivity extends AppCompatActivity {
    private final String TAG = ChatActivity.class.getSimpleName();
    private ActivityChatBinding binding;
    private ChatActivity mActivity;
    private int _xDelta;
    private int _yDelta;
    boolean orientationLocked = false;
    boolean orientationHorizontal = true;
    private int THRESHOLD = 0;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        THRESHOLD = (int) Utilities.convertDpToPixel(2, getApplicationContext());

        final boolean isNight = App.getInstance().isNightEnabled();
        mActivity = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        setSupportActionBar(binding.tbChat);

        if (Flags.DM_IS_NEW) {
            binding.txtChatMessage.requestFocus();
            Utilities.showKeyboard(binding.txtChatMessage);
        } else {
            binding.chatRlMain.requestFocus();
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(isNight ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        final ChatViewModel viewModel = new ChatViewModel(this,
                TextUtils.isEmpty(DirectApi.getInstance().getUserName()) ?
                        DirectApi.getInstance().getScreenName() : DirectApi.getInstance().getUserName(), AppData.UI_STATE_LOADING);
        binding.setModel(viewModel);
        binding.setClick(new ChatViewModel.ChatClickHandler() {
            @Override
            public void onSendClick(View v) {
                AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void onException(TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                        Log.e(TAG, "Error sending direct " + te.getLocalizedMessage());
                    }
                });

                if (DirectApi.getInstance().getUserId() > -1) {
                    asyncTwitter.sendDirectMessage(DirectApi.getInstance().getUserId(), binding.txtChatMessage.getText().toString());
                } else {
                    asyncTwitter.sendDirectMessage(DirectApi.getInstance().getScreenName(), binding.txtChatMessage.getText().toString());
                }
                binding.txtChatMessage.setText("");
            }

            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }

            @Override
            public void onDestroyClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mActivity, v, Gravity.BOTTOM, 0, R.style.popup_no_overlap_menu);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_direct, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.direct_delete:
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        DirectApi.getInstance().setUpdateAddHandler(new UpdateAddHandler() {
            @Override
            public void onUpdate() {
                viewModel.getChatAdapter().notifyDataSetChanged();
                viewModel.setState(DirectApi.getInstance().getChatModels().size() > 0 ?
                        AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
                viewModel.setTitle(TextUtils.isEmpty(DirectApi.getInstance().getUserName()) ?
                        DirectApi.getInstance().getScreenName() : DirectApi.getInstance().getUserName());
                if (DirectApi.getInstance().getChatModels().size() > 0)
                    binding.recyclerChat.scrollToPosition(DirectApi.getInstance().getChatModels().size() - 1);
            }

            @Override
            public void onAdd() {
                viewModel.getChatAdapter().notifyDataSetChanged();
                viewModel.setState(DirectApi.getInstance().getChatModels().size() > 0 ?
                        AppData.UI_STATE_VISIBLE : AppData.UI_STATE_NO_ITEMS);
                if (DirectApi.getInstance().getChatModels().size() > 0)
                    binding.recyclerChat.smoothScrollToPosition(DirectApi.getInstance().getChatModels().size() - 1);
            }

            @Override
            public void onError() {
                viewModel.setState(AppData.UI_STATE_NO_ITEMS);
            }

            @Override
            public void onDelete(int position) {

            }
        });

        DirectApi.getInstance().loadData();
        binding.chatSendDivider.setBackgroundColor(isNight ? Color.parseColor("#15191D") : Color.parseColor("#DFE4E7"));
        binding.txtChatMessage.setHintTextColor(isNight ? Color.parseColor("#4DBEC8D2") : Color.parseColor("#4D3D454C"));

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
        final int startMargin = params.leftMargin;
        final int endMargin = params.rightMargin;

        binding.recyclerChat.setOnScrollListener(onScrollListener);
        binding.recyclerChat.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
                        _xDelta = X - lParams.leftMargin;
                        _yDelta = Y - lParams.topMargin;

                        orientationLocked = false;
                        orientationHorizontal = true;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        return Math.abs(Y - _yDelta) <= (Math.abs(X - _xDelta) - THRESHOLD);
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        binding.recyclerChat.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
                        _xDelta = X - lParams.leftMargin;
                        _yDelta = Y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
                        final int oldLeftMargin = params.leftMargin;
                        final int oldRightMargin = params.rightMargin;

                        Animation a = new Animation() {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
                                params.leftMargin = oldLeftMargin + ((int) (-oldLeftMargin * interpolatedTime));
                                params.rightMargin = (int) ((oldRightMargin * (1 - interpolatedTime)) + (endMargin * interpolatedTime));
                                binding.recyclerChat.setLayoutParams(params);
                            }
                        };

                        a.setDuration(150); // in ms
                        if (oldLeftMargin < 0) {
                            binding.recyclerChat.startAnimation(a);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(Y - _yDelta) > Math.abs(X - _xDelta) || !orientationHorizontal) {
                            orientationHorizontal = false;
                            return false;
                        }

                        orientationLocked = true;

                        if ((X - _xDelta) <= -THRESHOLD) {
                            float alpha = 1.0f + ((float) 1 - (endMargin - (X - _xDelta))) / (float) endMargin;
                            setAlpha(alpha);
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.recyclerChat.getLayoutParams();
                            layoutParams.leftMargin = (int) Math.max(X - _xDelta, -Utilities.convertDpToPixel(50, getApplicationContext()));
                            layoutParams.rightMargin = Math.min(endMargin - (X - _xDelta), 0);
                            binding.recyclerChat.setLayoutParams(layoutParams);
                        }
                        break;
                }

                binding.getRoot().invalidate();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        Utilities.hideKeyboard(mActivity);
        if (Flags.DM_IS_NEW) {
            overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out);
        } else {
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        }
    }

    private void setAlpha(float alpha) {
        for (ChatModel chatModel : DirectApi.getInstance().getChatModels()) {
            chatModel.setAlpha(alpha);
        }
    }

    public ActivityChatBinding getBinding() {
        return binding;
    }
}

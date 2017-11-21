package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.adapters.UserDetailAdapter;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.ActivityChatSelectBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.viewmodels.ChatSelectViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class ChatSelectActivity extends AppCompatActivity {
    private final String TAG = ChatSelectActivity.class.getSimpleName();
    private ChatSelectViewModel viewModel;
    private ArrayList<UserModel> mUsersList;
    private UserDetailAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        final ActivityChatSelectBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_select);
        setSupportActionBar(binding.tbChatSelect);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Drawable back = getResources().getDrawable(R.drawable.ic_icons_toolbar_back);
            back.setColorFilter(getResources().getColor(App.getInstance().isNightEnabled() ?
                    R.color.dark_tint_color : R.color.light_tint_color), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(back);
        }

        mUsersList = new ArrayList<>();
        for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
            UserModel userModel = new UserModel(user.getId(), user.getBiggerProfileImageURL(),
                    user.getName(), "@" + user.getScreenName(), true, false, false);
            mUsersList.add(userModel);
        }

        binding.txtChatSelect.addTextChangedListener(searchTextWatcher);
        mAdapter = new UserDetailAdapter(mUsersList, getApplicationContext(), this, new UserDetailAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {
                AppData.DM_SELECTED_USER = model.getUsername();
                AppData.DM_OTHER_ID = model.getId();
                DirectApi.getInstance().clear();
                DirectApi.getInstance().setUserId(model.getId());
                DirectApi.getInstance().setScreenName(model.getTwitterName().replace("@", ""));
                startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
                finish();
            }
        });
        viewModel = new ChatSelectViewModel(mAdapter, getApplicationContext());
        binding.setModel(viewModel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        finish();
        overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out);
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            mUsersList.clear();
            for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
                if (user.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                    UserModel userModel = new UserModel(user.getId(), user.getBiggerProfileImageURL(),
                            user.getName(), "@" + user.getScreenName(), true, false, false);
                    mUsersList.add(userModel);
                }
            }

            mAdapter.notifyDataSetChanged();
        }
    };
}

package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.models.UserModel;
import twitter4j.AsyncTwitter;
import twitter4j.PagableResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserList;

/**
 * Created by neura on 08.07.17.
 */

public class StatsListedActivity extends AppCompatActivity {

    private User user;
    private RecyclerView mRvStats;
    private ArrayList<UserModel> userModels;
    private UserAdapter mAdapter;
    private StatsListedActivity mActivity;
    private AVLoadingIndicatorView mLoader;
    private boolean isLoading = false;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        mActivity = this;

        setContentView(R.layout.activity_stats);

        ImageView mBtnBack = (ImageView) findViewById(R.id.btn_stats_back);
        ImageView mImgBanner = (ImageView) findViewById(R.id.img_profile_test_header);
        TextView mTxtTitle = (TextView) findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) findViewById(R.id.txt_stats_subtitle);

        user = AppData.CURRENT_USER == null ? AppData.ME : AppData.CURRENT_USER;
        mTxtSubtitle.setText(getString(R.string.stats_listed) + ": " + Utilities.parseFollowers(user.getListedCount(), ""));
        mTxtTitle.setText(user.getName());
        Picasso.with(getApplicationContext())
                .load(user.getProfileBannerImageUrl())
                .resize(Utilities.getScreenWidth(this), (int) Utilities.convertDpToPixel(80, getApplicationContext()))
                .centerCrop()
                .into(mImgBanner);

        mRvStats = (RecyclerView) findViewById(R.id.recycler_stats);
        mRvStats.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRvStats.getLayoutManager();
                if (dy > 0) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isLoading = true;
                            loadData();
                        }
                    }
                }
            }
        });

        mLoader = (AVLoadingIndicatorView) findViewById(R.id.loader_stats);
        userModels = new ArrayList<>();
        mAdapter = new UserAdapter(userModels, getApplicationContext(), this, new UserAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {
                if (model.getUser() != null) {
                    Intent profileIntent = new Intent(getApplicationContext(), MVPProfileActivity.class);
                    profileIntent.putExtra(Flags.PROFILE_DATA, model.getUser());
                    StatsListedActivity.this.startActivity(profileIntent);
                    StatsListedActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .setDefaultDividerEnabled(true)
                .build(getApplicationContext());

        listConfig.applyConfig(getApplicationContext(), mRvStats);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setUI(true);
        loadData();
    }

    private void setUI(boolean isLoading) {
        mLoader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mRvStats.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private long nextCursor = -1;
    private long[] currentIds = null;
    private int loadedSize = 0;
    private int MAX_USERS = 50;

    private void loadData() {
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        if (te.getErrorMessage() != null)
//                            Toast.makeText(getApplicationContext(), "Error loading " + te.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        setUI(false);
                        isLoading = false;
                    }
                });
            }

            @Override
            public void gotUserListMemberships(PagableResponseList<UserList> userLists) {
                super.gotUserListMemberships(userLists);
                nextCursor = userLists.getNextCursor();
                for (UserList userList : userLists) {
                    UserModel userModel = new UserModel(userList.getUser().getId(),
                            userList.getUser().getOriginalProfileImageURL(),
                            userList.getUser().getName(),
                            userList.getUser().getScreenName(),
                            UsersData.getInstance().getFollowingList().contains(userList.getUser().getId()),
                            false, false);
                    userModel.setUser(User.getFromUserInstance(userList.getUser()));

                    if (!userModels.contains(userModel))
                        userModels.add(userModel);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        setUI(false);
                        isLoading = false;
                    }
                });
            }
        });

        asyncTwitter.getUserListMemberships(user.getScreenName(), nextCursor);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}

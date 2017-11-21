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

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

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
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 08.07.17.
 */

public class StatsFollowingActivity extends AppCompatActivity {

    private User user;
    private RecyclerView mRvStats;
    private ArrayList<UserModel> userModels;
    private UserAdapter mAdapter;
    private CircularProgressView mLoader;
    private boolean isLoading = false;
    private StatsFollowingActivity mActivity;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        setContentView(R.layout.activity_stats);
        mActivity = this;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ImageView mBtnBack = (ImageView) findViewById(R.id.btn_stats_back);
        ImageView mImgBanner = (ImageView) findViewById(R.id.img_profile_test_header);
        TextView mTxtTitle = (TextView) findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) findViewById(R.id.txt_stats_subtitle);

        user = AppData.CURRENT_USER == null ? AppData.ME : AppData.CURRENT_USER;
        mTxtSubtitle.setText(getString(R.string.stats_following) + ": " + Utilities.parseFollowers(user.getFriendsCount(), ""));
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

        mLoader = (CircularProgressView) findViewById(R.id.loader_stats);
        userModels = new ArrayList<>();
        mAdapter = new UserAdapter(userModels, getApplicationContext(), this, new UserAdapter.UserClickHandler() {
            @Override
            public void onItemClick(UserModel model, View v) {
                if (model.getUser() != null) {
                    AppData.CURRENT_USER = model.getUser();
                    Flags.userDirection = Flags.Directions.FROM_RIGHT;
                    Flags.userSource = Flags.UserSource.data;
                    Flags.homeUser = AppData.CURRENT_USER.getId() == AppData.ME.getId();
                    mActivity.startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
            public void lookedupUsers(ResponseList<twitter4j.User> users) {
                super.lookedupUsers(users);
                for (twitter4j.User user : users) {
                    UserModel userModel = new UserModel(user.getId(), user.getOriginalProfileImageURL(), user.getName(),
                            user.getScreenName(), UsersData.getInstance().getFollowingList().contains(user.getId()),
                            false, false);
                    userModel.setUser(User.getFromUserInstance(user));
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

            @Override
            public void gotFriendsIDs(IDs ids) {
                super.gotFriendsIDs(ids);
                nextCursor = ids.getNextCursor();
                currentIds = ids.getIDs();

                if (ids.getIDs().length > MAX_USERS) {
                    long[] loadIds = Arrays.copyOfRange(ids.getIDs(), loadedSize, loadedSize + MAX_USERS - 1);
                    loadedSize += MAX_USERS;
                    asyncTwitter.lookupUsers(loadIds);
                } else {
                    asyncTwitter.lookupUsers(ids.getIDs());
                }
            }
        });

        if (currentIds == null || loadedSize >= currentIds.length) {
            asyncTwitter.getFriendsIDs(user.getScreenName(), nextCursor);
        } else {
            long[] loadIds = Arrays.copyOfRange(currentIds, loadedSize, loadedSize + MAX_USERS - 1);
            loadedSize += MAX_USERS;
            asyncTwitter.lookupUsers(loadIds);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}

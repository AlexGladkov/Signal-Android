package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Keys;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 08.07.17.
 */

public class StatsLikesActivity extends AppCompatActivity {

    private StatusAdapter mAdapter;
    private ArrayList<StatusModel> mMentionsList;
    private RecyclerView mRvStats;
    private AVLoadingIndicatorView mLoader;
    private boolean isLoading = false;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        setContentView(R.layout.activity_stats);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ImageView mBtnBack = (ImageView) findViewById(R.id.btn_stats_back);
        ImageView mImgBanner = (ImageView) findViewById(R.id.img_profile_test_header);
        TextView mTxtTitle = (TextView) findViewById(R.id.txt_stats_title);
        TextView mTxtSubtitle = (TextView) findViewById(R.id.txt_stats_subtitle);

        user = AppData.CURRENT_USER == null ? AppData.ME : AppData.CURRENT_USER;
        mTxtSubtitle.setText(getString(R.string.stats_likes) + ": " + Utilities.parseFollowers(user.getFavouritesCount(), ""));
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
                            loadTweets();
                        }
                    }
                }
            }
        });
        mLoader =  findViewById(R.id.loader_stats);
        mMentionsList = new ArrayList<>();
        mAdapter = new StatusAdapter(mMentionsList, this, true, true, new StatusAdapter.StatusClickListener() {
            @Override
            public void onSearch(String searchText, View v) {
                Intent searchIntent = new Intent(getApplicationContext(), MVPSearchActivity.class);
                searchIntent.putExtra(Keys.SearchQuery.getValue(), searchText);
                startActivity(searchIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, new TweetActions.MoreCallback() {
            @Override
            public void onDelete(StatusModel statusModel) {
                int position = mMentionsList.indexOf(statusModel);
                mMentionsList.remove(statusModel);
                mAdapter.notifyItemRemoved(position);
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
        loadTweets();
    }

    private void setUI(boolean isLoading) {
        mLoader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mRvStats.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void loadTweets() {
        final Handler handler = new Handler();
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();

        Paging paging;

        if (mMentionsList.size() > 0) {
            paging = new Paging(1, 50);
            paging.setMaxId(mMentionsList.get(mMentionsList.size() - 1).getId());
        } else {
            paging = new Paging(1, 50);
        }

        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (te.getErrorMessage() != null)
                            Toast.makeText(getApplicationContext(), "Error loading " + te.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        setUI(false);
                        isLoading = false;
                    }
                });
            }


            @Override
            public void gotFavorites(ResponseList<Status> statuses) {
                super.gotUserTimeline(statuses);
                for (Status status : statuses) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    if (!mMentionsList.contains(statusModel))
                        mMentionsList.add(statusModel);
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

        asyncTwitter.getFavorites(user.getScreenName(), paging);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}

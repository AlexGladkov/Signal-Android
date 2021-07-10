package solonsky.signal.twitter.activities;

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
import solonsky.signal.twitter.adapters.MediaStaggeredAdapter;
import solonsky.signal.twitter.api.ProfileDataApi;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.ImageModel;
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

public class StatsImagesActivity extends AppCompatActivity {

    private MediaStaggeredAdapter mAdapter;
    private ArrayList<ImageModel> mImagesList;
    private RecyclerView mRvStats;
    private AVLoadingIndicatorView mLoader;
    private boolean isLoading = false;
    private long maxId = -1;
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
        mTxtSubtitle.setText(getString(R.string.stats_images));
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
        mLoader = (AVLoadingIndicatorView) findViewById(R.id.loader_stats);
        mImagesList = new ArrayList<>();
        mAdapter = new MediaStaggeredAdapter(mImagesList, StatsImagesActivity.this,
                new MediaStaggeredAdapter.ImageStaggeredListener() {
            @Override
            public void onClick(ImageModel imageModel, View v) {
                ArrayList<String> urls = new ArrayList<>();
                for (ImageModel imageModel1 : ProfileDataApi.getInstance().getImages()) {
                    if (imageModel1.getImageUrl() != null)
                        urls.add(imageModel1.getImageUrl());
                }

                final solonsky.signal.twitter.overlays.ImageOverlay imageOverlay = new solonsky.signal.twitter.overlays.ImageOverlay(urls,
                        StatsImagesActivity.this, ProfileDataApi.getInstance().getImages().indexOf(imageModel));
                imageOverlay.setImageOverlayClickHandler(new solonsky.signal.twitter.overlays.ImageOverlay.ImageOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(StatsImagesActivity.this);
                        downloadFiles.saveFile(url, getString(R.string.download_url));
                    }
                });
            }
        });

        GridLayoutManager manager = new GridLayoutManager(getApplicationContext(), 2, GridLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0 ? (ProfileDataApi.getInstance().getImages().size() % 2 == 0) ? 1 : 2 : 1);
            }
        });

        mRvStats.setHasFixedSize(true);
        mRvStats.setNestedScrollingEnabled(false);
        mRvStats.setLayoutManager(manager);
        mRvStats.setAdapter(mAdapter);
        mRvStats.addItemDecoration(new ListConfig.SpacesItemDecoration((int)
                Utilities.convertDpToPixel(2, getApplicationContext())));

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

        if (maxId > 0) {
            paging = new Paging(1, 50);
            paging.setMaxId(maxId);
        } else {
            paging = new Paging(1, 50);
        }

        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitterMediaOnly();
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
            public void gotUserTimeline(ResponseList<Status> statuses) {
                super.gotUserTimeline(statuses);
                for (Status status : statuses) {
                    for (twitter4j.MediaEntity mediaEntity : status.getMediaEntities()) {
                        mImagesList.add(new ImageModel(mediaEntity.getMediaURL()));
                    }
                }

                maxId = statuses.size() > 0 ? statuses.get(statuses.size() - 1).getId() : maxId;

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

        asyncTwitter.getUserTimeline(user.getScreenName(), paging);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}

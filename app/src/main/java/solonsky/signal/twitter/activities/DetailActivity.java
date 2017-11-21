package solonsky.signal.twitter.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mopub.common.util.Json;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDateTime;
import org.w3c.dom.Text;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.DetailStaggeredAdapter;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.ActivityDetailBinding;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.fragments.DetailReplyFragment;
import solonsky.signal.twitter.fragments.DetailRtFragment;
import solonsky.signal.twitter.fragments.DummyFragment;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.DateConverter;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ImageAnimation;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.overlays.ImageActionsOverlay;
import solonsky.signal.twitter.viewmodels.ComposeViewModel;
import solonsky.signal.twitter.viewmodels.DetailViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 27.06.17.
 */

public class DetailActivity extends AppCompatActivity implements SmartTabLayout.TabProvider {
    private static final long THREAD_DURATION = 300;
    private int CURRENT_POSITION = 1;
    private final String TAG = DetailActivity.class.getSimpleName();
    private DetailActivity mActivity;
    public ActivityDetailBinding binding;
    private int threadHeight;
    private int statusHeight;
    private int imageHeight;

    private long replyCount = 0;
    private long favCount = 0;
    private long rtCount = 0;

    private final int TOOLBAR_MARGIN = 96;
    private DetailViewModel viewModel;

    private DetailReplyFragment detailReplyFragment = new DetailReplyFragment();
    //    private DetailLikeFragment detailLikeFragment = new DetailLikeFragment();
    private DetailRtFragment detailRtFragment = new DetailRtFragment();

    private Fragment lastFragment = null;
    private ArrayList<StatusModel> threadModels;
    private TextView mTxtReply = null;
    private ImageView mImgReply = null;
    private TextView mTxtRt = null;
    private ImageView mImgRt = null;
    private boolean isOpen = false;

    View.OnTouchListener scrollTouchListener = new View.OnTouchListener() {
        int startY = -1;
        int oldDiff = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!viewModel.isLoaded()) return false;
            final int Y = (int) event.getRawY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = Y;
                    oldDiff = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isOpen) {
                        changeThreadReadStatus();
                        binding.recyclerDetailConversation.animate().setDuration(300).translationY(0);
                        binding.rlDetailMain.animate().setDuration(300).translationY(threadHeight);
                        isOpen = true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (startY == -1) startY = Y;
                    int diff = Y - startY;
                    boolean isDown = diff > oldDiff;

                    if (((diff < threadHeight && isDown) || (diff < threadHeight && !isDown)) && !isOpen) {
                        binding.rlDetailMain.setTranslationY(diff);
                        binding.recyclerDetailConversation.setTranslationY(-threadHeight + diff);
                    }

                    if (diff >= threadHeight && !isOpen) {
                        isOpen = true;
                        changeThreadReadStatus();
                    }
                    oldDiff = diff;
                    break;
            }
            binding.getRoot().invalidate();
            return !isOpen;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.TranslucentDark);
        }

        final Handler handler = new Handler();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        if (AppData.CURRENT_STATUS_MODEL == null) finish();

        setupSlideBack();
        setupStatusBar();

        binding.scrollDetail.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == 0 && isOpen && viewModel.isLoaded()) {
                    if (!viewModel.isExpanded()) {
                        viewModel.setExpanded(true);
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(19, 0);
                        valueAnimator.setDuration(THREAD_DURATION);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, (Integer) animation.getAnimatedValue());
                            }
                        });
                        valueAnimator.start();
                    }
                }

                setupStatus(scrollY);
                setupTabs(scrollY);
            }
        });

        binding.llDetailStatus.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.llDetailStatus.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusHeight = binding.llDetailStatus.getHeight();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.llDetailContent.getLayoutParams();
                        params.topMargin = statusHeight;
                        binding.llDetailContent.setLayoutParams(params);
                    }
                });
            }
        });

        binding.recyclerDetailMedia.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.llDetailContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageHeight = binding.llDetailContent.getHeight();
                    }
                });
            }
        });

        mActivity = this;

        ViewGroup.LayoutParams shadowParams = binding.viewDetailShadow.getLayoutParams();
        shadowParams.height = AppData.CURRENT_STATUS_MODEL.getMediaEntities().size() > 0 ?
                (int) Utilities.convertDpToPixel(4, getApplicationContext()) : 0;
        binding.viewDetailShadow.setLayoutParams(shadowParams);

        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("", DummyFragment.class)
                .add("", DummyFragment.class)
                .create());

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        in.setDuration(THREAD_DURATION / 2);
        out.setDuration(THREAD_DURATION / 2);

        binding.txtDetailSubtitle.setInAnimation(in);
        binding.txtDetailSubtitle.setOutAnimation(out);
        binding.scrollDetail.setOnTouchListener(scrollTouchListener);

        binding.stbDetail.setCustomTabView(this);
        binding.vpDetail.setAdapter(adapter);
        binding.stbDetail.setViewPager(binding.vpDetail);
        binding.stbDetail.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                View oldTab = binding.stbDetail.getTabAt(CURRENT_POSITION);
                View currentTab = binding.stbDetail.getTabAt(position);

                RelativeLayout oldView = (RelativeLayout) oldTab.findViewById(R.id.tab_layout);
                RelativeLayout currentView = (RelativeLayout) currentTab.findViewById(R.id.tab_layout);

                TextView textView = (TextView) currentView.findViewById(R.id.tab_txt);

                switch (position) {
                    case 0:
                        textView.setText(String.valueOf(replyCount));
                        selectFragment(detailReplyFragment);
                        break;

                    case 1:
                        textView.setText(String.valueOf(rtCount));
                        selectFragment(detailRtFragment);
                        break;
                }

                oldView.setBackground(getResources().getDrawable(R.drawable.tab_shape_transparent));
                currentView.setBackground(getResources().getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light));

                CURRENT_POSITION = position;
            }
        });

        checkQuote();

        final StatusModel currentStatus = AppData.CURRENT_STATUS_MODEL;
        String screenName = currentStatus.getInReplyToStatusId() > -1 ?
                getString(R.string.in_reply) : "@" + currentStatus.getUser().getScreenName();

        final String dateClient = "  •  via " + AppData.CURRENT_STATUS_MODEL.getSource().replace(">",
                "special").replace("<", "special").split("special")[2]
                + (currentStatus.getGeoLocation() == null ? "" : "  •  ");
        String postTime = new LocalDateTime(currentStatus.getCreatedAt()).toString("dd.MM.YY, HH:mm");
        String location = currentStatus.getPlace() == null ? "" : currentStatus.getPlace().get("fullName").getAsString();

        viewModel = new DetailViewModel(currentStatus.getUser().getName(), screenName,
                currentStatus.getUser().getOriginalProfileImageURL(),
                currentStatus.getText(), currentStatus.getUser().getLocation(),
                postTime, postTime + dateClient + location,
                mActivity, currentStatus.isFavorited(),
                UsersData.getInstance().getFollowingList().contains(currentStatus.getUser().getId()) ||
                currentStatus.getUser().getId() == AppData.ME.getId(),
                currentStatus.getInReplyToStatusId() > -1,
                currentStatus.getQuotedStatus() != null,
                currentStatus.getMediaEntities());


        final ArrayList<ImageModel> imagesFiltered = new ArrayList<>();
        for (JsonElement image : currentStatus.getMediaEntities()) {
            String url = ((JsonObject) image).get("mediaURL").getAsString();
            ImageModel imageModel = new ImageModel(url);
            switch (((JsonObject) image).get("type").getAsString()) {
                case "image":
                    imageModel.setMediaType(Flags.MEDIA_TYPE.IMAGE);
                    break;

                case "video":
                    String videoUrl = "";
                    for (JsonElement variants : ((JsonObject) image).get("videoVariants").getAsJsonArray()) {
                        videoUrl = variants.getAsJsonObject().get("url").getAsString();
                    }

                    imageModel.setPreviewUrl(imageModel.getImageUrl());
                    imageModel.setImageUrl(videoUrl);
                    imageModel.setMediaType(Flags.MEDIA_TYPE.VIDEO);
                    break;

                case "animated_gif":
                    videoUrl = "";
                    for (JsonElement variants : ((JsonObject) image).get("videoVariants").getAsJsonArray()) {
                        videoUrl = variants.getAsJsonObject().get("url").getAsString();
                    }

                    imageModel.setPreviewUrl(imageModel.getImageUrl());
                    imageModel.setImageUrl(videoUrl);
                    imageModel.setMediaType(Flags.MEDIA_TYPE.GIF);
                    break;

                case "youtube":
                    imageModel.setPreviewUrl(((JsonObject) image).get("mediaURLHttps").getAsString());
                    imageModel.setImageUrl(((JsonObject) image).get("url").getAsString());
                    imageModel.setMediaType(Flags.MEDIA_TYPE.YOUTUBE);
                    break;
            }

            imagesFiltered.add(imageModel);
        }

        DetailStaggeredAdapter imagesStaggeredAdapter = new DetailStaggeredAdapter(imagesFiltered, mActivity,
                new DetailStaggeredAdapter.ImageStaggeredListener() {
                    @Override
                    public void onClick(ImageModel imageModel, View v) {
                        if (imageModel.getMediaType().equals(Flags.MEDIA_TYPE.IMAGE)) {
                            ArrayList<String> urls = new ArrayList<>();
                            for (ImageModel image : imagesFiltered) {
                                urls.add(image.getImageUrl());
                            }

                            final ImageActionsOverlay imageActionsOverlay = new ImageActionsOverlay(mActivity, urls,
                                    AppData.CURRENT_STATUS_MODEL, imagesFiltered.indexOf(imageModel));
                            imageActionsOverlay.setImageActionsOverlayClickHandler(new ImageActionsOverlay.ImageActionsOverlayClickHandler() {
                                @Override
                                public void onBackClick(View v) {
                                    imageActionsOverlay.getImageViewer().onDismiss();
                                }

                                @Override
                                public void onSaveClick(View v, String url) {
                                    DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                                    downloadFiles.saveFile(url, mActivity.getString(R.string.download_url));
                                }

                                @Override
                                public void onShareImageClick(View v, String url) {

                                }

                                @Override
                                public void onReplyClick(View v) {
                                    TweetActions.reply(AppData.CURRENT_STATUS_MODEL, mActivity);
                                    imageActionsOverlay.getImageViewer().onDismiss();
                                }

                                @Override
                                public void onRtClick(View v) {
                                    TweetActions.retweetPopup(mActivity, v, AppData.CURRENT_STATUS_MODEL, new TweetActions.ActionCallback() {
                                        @Override
                                        public void onException(String error) {
                                            Toast.makeText(mActivity.getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onLikeClick(View v) {
                                    final StatusModel statusModel = AppData.CURRENT_STATUS_MODEL;
                                    statusModel.setFavorited(!statusModel.isFavorited());
                                    imageActionsOverlay.setFavorited(statusModel.isFavorited());
                                    imageActionsOverlay.changeFavorited();
                                    TweetActions.favorite(statusModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                                        @Override
                                        public void onException(String error) {
                                            statusModel.setFavorited(!statusModel.isFavorited());
                                            imageActionsOverlay.setFavorited(statusModel.isFavorited());
                                            imageActionsOverlay.changeFavorited();
                                        }
                                    });
                                }

                                @Override
                                public void onShareClick(View v) {

                                }

                                @Override
                                public void onMoreClick(View v) {

                                }
                            });
                        } else if (imageModel.getMediaType().equals(Flags.MEDIA_TYPE.YOUTUBE)) {
                            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                    (imageModel.getImageUrl())));
                        } else {
                            AppData.MEDIA_URL = imageModel.getImageUrl();
                            AppData.MEDIA_TYPE = imageModel.getMediaType();
                            mActivity.startActivity(new Intent(mActivity.getApplicationContext(), MediaActivity.class));
                        }
                    }
                });

        GridLayoutManager manager = new GridLayoutManager(mActivity.getApplicationContext(), 2, GridLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0 ? (imagesFiltered.size() % 2 == 0) ? 1 : 2 : 1);
            }
        });

        binding.recyclerDetailMedia.setHasFixedSize(true);
        binding.recyclerDetailMedia.setNestedScrollingEnabled(false);
        binding.recyclerDetailMedia.setLayoutManager(manager);
        binding.recyclerDetailMedia.setAdapter(imagesStaggeredAdapter);
        binding.recyclerDetailMedia.addItemDecoration(
                new ListConfig.SpacesItemDecoration((int) Utilities.convertDpToPixel(2,
                        mActivity.getApplicationContext())));

        binding.setModel(viewModel);
        binding.setClick(new DetailViewModel.DetailClickHandler() {
            @Override
            public void onReplyClick(View v) {
                TweetActions.reply(currentStatus, mActivity);
            }

            @Override
            public void onRtClick(View v) {
                TweetActions.retweetPopup(mActivity, v, currentStatus, new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {

                    }
                });
            }

            @Override
            public void onLikeClick(View v) {
                currentStatus.setFavorited(!currentStatus.isFavorited());
                viewModel.setFavorite(currentStatus.isFavorited());
                TweetActions.favorite(currentStatus.isFavorited(), currentStatus.getId(), new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {
                        currentStatus.setFavorited(!currentStatus.isFavorited());
                    }
                });
            }

            @Override
            public void onShareClick(View v) {
                TweetActions.share(currentStatus, mActivity);
            }

            @Override
            public void onMoreClick(View v) {
                TweetActions.morePopup(mActivity, v, currentStatus, new TweetActions.MoreCallback() {
                    @Override
                    public void onDelete(StatusModel statusModel) {
                        int position = FeedData.getInstance().getFeedStatuses().indexOf(statusModel);
                        FeedData.getInstance().getFeedStatuses().remove(statusModel);
                        FeedData.getInstance().saveCache(TAG);
                        FeedData.getInstance().getUpdateHandler().onDelete(position);
                        Toast.makeText(getApplicationContext(), getString(R.string.success_deleted), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onThreadClick(View v) {
//                binding.scrollDetail.smoothScrollTo(0, viewModel.isExpanded() ? 0 : threadHeight);
//                binding.rlDetailMain.animate().translationY(!viewModel.isExpanded() ? 0 : threadHeight).setDuration(300).start();
                if (isOpen) {
                    changeArrowStatus();
                    ObjectAnimator.ofInt(binding.scrollDetail, "scrollY", viewModel.isExpanded() ? 0 : threadHeight)
                            .setDuration(THREAD_DURATION).start();
                } else {
                    if (viewModel.isLoaded()) {
                        binding.rlDetailMain.animate().setDuration(THREAD_DURATION).translationY(threadHeight).start();
                        binding.recyclerDetailConversation.setTranslationY(-threadHeight);
                        binding.recyclerDetailConversation.animate().setDuration(THREAD_DURATION).translationY(0).start();
                        changeThreadReadStatus();
                        isOpen = true;
                    }
                }
            }

            @Override
            public void onClientClick(View v) {
                String link = AppData.CURRENT_STATUS_MODEL.getSource().split("\\s+")[1];
                link = link.substring(6, link.length() - 1);
                Utilities.openLink(link, DetailActivity.this);
            }

            @Override
            public void onFollowClick(View v) {

            }

            @Override
            public void onQuoteMediaClick(View v) {
                performContent(AppData.CURRENT_STATUS_MODEL.getQuotedStatus());
            }
        });

        binding.txtDetailStatus.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG,
                AutoLinkMode.MODE_MENTION,
                AutoLinkMode.MODE_URL,
                AutoLinkMode.MODE_SHORT
        );

        boolean isNight = App.getInstance().isNightEnabled();
        binding.txtDetailStatus.setHashtagModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_tag_color : R.color.light_tag_color));
        binding.txtDetailStatus.setMentionModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.txtDetailStatus.setUrlModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.txtDetailStatus.setSelectedStateColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_secondary_text_color : R.color.light_secondary_text_color));

        String[] urls = new String[currentStatus.getUrlEntities().size()];
        for (int i = 0; i < currentStatus.getUrlEntities().size(); i++) {
            urls[i] = ((JsonObject) currentStatus.getUrlEntities().get(0)).get("displayURL").getAsString();
        }

        binding.txtDetailStatus.setShortUrls(urls);
        binding.txtDetailStatus.setAutoLinkText(currentStatus.getText());
        binding.txtDetailStatus.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                if (autoLinkMode.equals(AutoLinkMode.MODE_HASHTAG)) {
                    AppData.searchQuery = matchedText;
                    mActivity.startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (autoLinkMode.equals(AutoLinkMode.MODE_MENTION)) {
                    mActivity.startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    Flags.userSource = Flags.UserSource.screenName;
                    AppData.CURRENT_SCREEN_NAME = matchedText;
                } else if (autoLinkMode.equals(AutoLinkMode.MODE_SHORT)) {
                    for (JsonElement jsonElement : currentStatus.getUrlEntities()) {
                        if (jsonElement.getAsJsonObject().get("displayURL").getAsString().equals(matchedText)) {
                            Utilities.openLink(jsonElement.getAsJsonObject().get("expandedURL").getAsString(), mActivity);
                        }
                    }
                }
            }

            @Override
            public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                Log.e(TAG, "autolink long click - " + matchedText);
            }
        });

        if (!detailReplyFragment.isAdded())
            getSupportFragmentManager().beginTransaction().add(R.id.fl_detail,
                    detailReplyFragment, detailReplyFragment.getTag()).commit();
            getSupportFragmentManager().beginTransaction().hide(detailReplyFragment).commit();
        if (!detailRtFragment.isAdded())
            getSupportFragmentManager().beginTransaction().add(R.id.fl_detail,
                    detailRtFragment, detailRtFragment.getTag()).commit();

        lastFragment = detailRtFragment;
        binding.vpDetail.setCurrentItem(1);

        detailRtFragment.loadApi();
        detailReplyFragment.loadApi();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    @Override
    public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        Resources res = container.getContext().getResources();
        View tab = inflater.inflate(R.layout.tab_item, container, false);
        ImageView imageView = (ImageView) tab.findViewById(R.id.tab_iv);
        TextView textView = (TextView) tab.findViewById(R.id.tab_txt);
        RelativeLayout layoutView = (RelativeLayout) tab.findViewById(R.id.tab_layout);

        textView.setTextColor(res.getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_primary_text_color : R.color.light_primary_text_color));
        layoutView.setBackground(CURRENT_POSITION == position ?
                res.getDrawable(App.getInstance().isNightEnabled() ? R.drawable.tab_shape_dark : R.drawable.tab_shape_light)
                : res.getDrawable(R.drawable.tab_shape_transparent));

        boolean isNightEnabled = App.getInstance().isNightEnabled();
        switch (position) {
            case 0:
                textView.setText(String.valueOf(0));
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_reply));
                imageView.setColorFilter(res.getColor(isNightEnabled ?
                        R.color.dark_reply_tint_color : R.color.light_reply_tint_color));

                mTxtReply = textView;
                mImgReply = imageView;
                break;

            case 1:
                textView.setText(String.valueOf(0));
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_rt));
                imageView.setColorFilter(res.getColor(isNightEnabled ?
                        R.color.dark_rt_tint_color : R.color.light_rt_tint_color));

                mTxtRt = textView;
                mImgRt = imageView;
                break;
        }

        imageView.setAlpha(0.3f);
        textView.setAlpha(0.3f);

        return tab;
    }

    private void changeThreadReadStatus() {
        changeArrowStatus();
        viewModel.setTwitterName("@" + AppData.CURRENT_STATUS_MODEL.getUser().getScreenName());
        viewModel.setShowThread(true);
    }

    private void changeArrowStatus() {
        ValueAnimator valueAnimator = !viewModel.isExpanded() ? ValueAnimator.ofInt(19, 0) : ValueAnimator.ofInt(0, 19);
        valueAnimator.setDuration(THREAD_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, (Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
        viewModel.setExpanded(!viewModel.isExpanded());
    }

    private void selectFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragment.isAdded()) {
            fragmentTransaction.hide(lastFragment)
                    .show(fragment).commit();
        }

        lastFragment = fragment;
    }

    private boolean isShadow = false;
    private void setupStatus(int scrollY) {
//        int threadDiffHeight = viewModel.isExpanded() ? threadHeight : 0;
        int diff = (int) (Utilities.convertDpToPixel(TOOLBAR_MARGIN, getApplicationContext()) + threadHeight
                - scrollY - Utilities.convertDpToPixel(24, getApplicationContext()));
        int init = (int) (Utilities.convertDpToPixel(TOOLBAR_MARGIN, getApplicationContext()) + threadHeight
                - Utilities.convertDpToPixel(24, getApplicationContext()));
        int shadowHeight = (int) Utilities.convertDpToPixel(4, getApplicationContext());
        int step = ((init - diff) / shadowHeight) - shadowHeight;

        if (diff <= 0) {
            if (viewModel.isExpanded()) {
                viewModel.setExpanded(false);
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 19);
                valueAnimator.setDuration(THREAD_DURATION);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, (Integer) animation.getAnimatedValue());
                    }
                });
                valueAnimator.start();
            }

            binding.llDetailStatus.animate().translationY(-diff).setDuration(0).start();
            binding.viewDetailShadow.animate().translationY(-diff).setDuration(0).start();
        } else {
            binding.llDetailStatus.animate().translationY(0).setDuration(0).start();
            binding.viewDetailShadow.animate().translationY(0).setDuration(0).start();
        }

        if ((isShadow && diff > 0) || (!isShadow && diff <= 0)) {
            isShadow = !isShadow;
            ValueAnimator valueAnimator = ValueAnimator.ofInt(diff > 0 ?
                            (int) Utilities.convertDpToPixel(4, getApplicationContext()) : 0,
                    diff > 0 ? 0 : (int) Utilities.convertDpToPixel(4, getApplicationContext()));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    binding.viewDetailShadow.getLayoutParams().height = (int) animation.getAnimatedValue();
                    binding.viewDetailShadow.requestLayout();
                }
            });
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setDuration(150);
            valueAnimator.start();
        }
    }

    private StatusAdapter.StatusClickListener statusClickListener = new StatusAdapter.StatusClickListener() {
        @Override
        public void onSearch(String searchText, View v) {
            Log.e(TAG, "Search start - " + searchText);
        }
    };

    int tabY = 100000;
    boolean isChange = false;

    private void setupTabs(int scrollY) {
        tabY = isChange ? tabY : (int) binding.stbDetail.getY();
        int diff = (-statusHeight + tabY + threadHeight - scrollY);

        if (diff < 0) {
            isChange = true;
            binding.stbDetail.animate().translationY(-diff).setDuration(0).start();
        } else {
            binding.stbDetail.animate().translationY(0).setDuration(0).start();
        }
    }

    private void performContent(final StatusModel statusModel) {
        if (statusModel.getMediaEntities().size() > 0) {
            String type = statusModel.getMediaEntities().get(0).getAsJsonObject().get("type").getAsString();
            if (type.equals(Flags.MEDIA_GIF)) {
                AppData.MEDIA_URL = statusModel.getMediaEntities().get(0).getAsJsonObject()
                        .get("videoVariants").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.GIF;
                mActivity.startActivity(new Intent(getApplicationContext(), MediaActivity.class));
            } else if (type.equals(Flags.MEDIA_VIDEO)) {
                AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.VIDEO;
                AppData.MEDIA_URL = statusModel.getMediaEntities().get(0).getAsJsonObject()
                        .get("videoVariants").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                mActivity.startActivity(new Intent(getApplicationContext(), MediaActivity.class));
            } else if (type.equals(Flags.MEDIA_PHOTO)) {
                final ArrayList<String> urls = new ArrayList<>();

                for (JsonElement media : statusModel.getMediaEntities()) {
                    JsonObject mediaEntity = (JsonObject) media;
                    urls.add(mediaEntity.get("mediaURLHttps").getAsString());
                }

                final ImageActionsOverlay imageActionsOverlay = new ImageActionsOverlay(mActivity, urls, statusModel, 0);
                imageActionsOverlay.setImageActionsOverlayClickHandler(new ImageActionsOverlay.ImageActionsOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageActionsOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                        downloadFiles.saveFile(url, mActivity.getString(R.string.download_url));
                    }

                    @Override
                    public void onShareImageClick(View v, String url) {

                    }

                    @Override
                    public void onReplyClick(View v) {
                        binding.getClick().onReplyClick(v);
                        imageActionsOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onRtClick(View v) {
                        TweetActions.morePopup(mActivity, v, statusModel, new TweetActions.MoreCallback() {
                            @Override
                            public void onDelete(StatusModel statusModel) {

                            }
                        });
                    }

                    @Override
                    public void onLikeClick(View v) {
                        statusModel.setFavorited(!statusModel.isFavorited());
                        imageActionsOverlay.setFavorited(statusModel.isFavorited());
                        imageActionsOverlay.changeFavorited();
                        TweetActions.favorite(statusModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                            @Override
                            public void onException(String error) {
                                statusModel.setFavorited(!statusModel.isFavorited());
                                imageActionsOverlay.setFavorited(statusModel.isFavorited());
                                imageActionsOverlay.changeFavorited();
                            }
                        });
                    }

                    @Override
                    public void onShareClick(View v) {
                        binding.getClick().onShareClick(v);
                    }

                    @Override
                    public void onMoreClick(View v) {
                        binding.getClick().onMoreClick(v);
                    }
                });
            }
        }
    }

    /**
     * Setup slide back function to whole screen
     */

    private void setupSlideBack() {
        binding.slDetail.setShowMode(SwipeLayout.ShowMode.LayDown);
        binding.slDetail.addDrag(SwipeLayout.DragEdge.Left, binding.viewBottomLeft);
        binding.slDetail.addDrag(SwipeLayout.DragEdge.Right, binding.viewBottomRight);
        binding.slDetail.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                float alpha = 0.7f - (leftOffset / Utilities.convertDpToPixel(250, getApplicationContext()));
                binding.viewBottomLeft.setAlpha(alpha);
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    /**
     * Setup status bar color
     */
    private void setupStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        binding.viewStatusBarScrim.setBackgroundColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_background_color : R.color.light_status_bar_detail_color));

        if (AppData.CURRENT_STATUS_MODEL.getInReplyToStatusId() > -1)
            binding.tbDetail.setBackgroundColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                    R.color.dark_background_detail_highlight_color : R.color.light_background_detail_highlight_color));
    }

    /**
     * Setup quote if it exist
     */
    private void checkQuote() {
        if (AppData.CURRENT_STATUS_MODEL.getQuotedStatus() != null) {
            binding.txtDetailQuoteCreatedAt.setText(new DateConverter(getApplicationContext())
                    .parseTime(new LocalDateTime(AppData.CURRENT_STATUS_MODEL
                    .getQuotedStatus().getCreatedAt())));

            binding.txtDetailQuoteTitle.setText(AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getUser().getName());
            binding.txtDetailQuoteText.setText(AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getText());

//            Dont delete that
//            for (JsonElement jsonElement : AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities()) {
//                binding.txtDetailQuoteText.setText(binding.txtDetailQuoteText.getText().toString() + "\n" +
//                        jsonElement.getAsJsonObject().get("mediaURL").getAsString());
//            }

            if (AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities().size() > 0) {
                String type = AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities().get(0)
                        .getAsJsonObject().get("type").getAsString();

                switch (type) {
                    case Flags.MEDIA_PHOTO:
                        binding.imgDetailQuoteBadge.setVisibility(View.GONE);
                        break;
                    case Flags.MEDIA_GIF:
                        binding.imgDetailQuoteBadge.setVisibility(View.VISIBLE);
                        binding.imgDetailQuoteBadge.setImageDrawable(getDrawable(R.drawable.ic_badges_media_gif));
                        break;
                    case Flags.MEDIA_VIDEO:
                        binding.imgDetailQuoteBadge.setVisibility(View.VISIBLE);
                        binding.imgDetailQuoteBadge.setImageDrawable(getDrawable(R.drawable.ic_badges_media_video));
                        break;
                }

                Picasso.with(getApplicationContext())
                        .load(AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities().get(0)
                                .getAsJsonObject().get("mediaURL").getAsString())
                        .resize((int) Utilities.convertDpToPixel(64f, getApplicationContext()),
                                (int) Utilities.convertDpToPixel(64f, getApplicationContext()))
                        .centerCrop()
                        .transform(new CirclePicasso(
                                Utilities.convertDpToPixel(4, getApplicationContext()),
                                Utilities.convertDpToPixel(0.5f, getApplicationContext()),
                                25, R.color.black))
                        .into(binding.imgDetailQuoteImage);

                if (AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities().size() > 1) {
                    binding.detailQuoteImgMediaCount.setVisibility(View.VISIBLE);
                    binding.detailQuoteTxtMediaCount.setVisibility(View.VISIBLE);
                    binding.detailQuoteTxtMediaCount.setText(String.valueOf(
                            AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities().size()));
                } else {
                    binding.detailQuoteImgMediaCount.setVisibility(View.INVISIBLE);
                    binding.detailQuoteTxtMediaCount.setVisibility(View.INVISIBLE);
                }
            } else {
                binding.detailFlQuoteMedia.setVisibility(View.GONE);
                binding.imgDetailQuoteBadge.setVisibility(View.GONE);
                binding.detailQuoteImgMediaCount.setVisibility(View.INVISIBLE);
                binding.detailQuoteTxtMediaCount.setVisibility(View.INVISIBLE);
            }
        }
    }

    /* Measurements section */

    public void measureThreadHeight() {
        binding.recyclerDetailConversation.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.recyclerDetailConversation.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        threadHeight = binding.recyclerDetailConversation.getHeight();
                        int scrollHeight = binding.scrollDetail.getChildAt(0).getHeight();
                        int screenHeight = Utilities.getScreenHeight(mActivity);
                        int offset = (int) (Utilities.convertDpToPixel(24, getApplicationContext()) + screenHeight
                                                        + threadHeight - scrollHeight);
                        Log.e(TAG, "thread height - " + threadHeight);
                        Log.e(TAG, "screenHeight - " + Utilities.getScreenHeight(mActivity));
                        Log.e(TAG, "Scroll height - " + binding.scrollDetail.getChildAt(0).getHeight());
                        viewModel.setDummyHeight(offset < 0 ? 0 : offset);
                    }
                });
            }
        });
    }

    public StatusAdapter.StatusClickListener getStatusClickListener() {
        return statusClickListener;
    }

    public void setReplyCount(long replyCount) {
        this.replyCount = replyCount;
        mTxtReply.setAlpha(replyCount == 0 ? 0.3f : 1.0f);
        mImgReply.setAlpha(replyCount == 0 ? 0.3f : 1.0f);
        mTxtReply.setText(String.valueOf(replyCount));
    }

    public void setRtCount(long rtCount) {
        this.rtCount = rtCount;
        mTxtRt.setAlpha(rtCount == 0 ? 0.3f : 1.0f);
        mImgRt.setAlpha(rtCount == 0 ? 0.3f : 1.0f);
        mTxtRt.setText(String.valueOf(rtCount));
    }
}

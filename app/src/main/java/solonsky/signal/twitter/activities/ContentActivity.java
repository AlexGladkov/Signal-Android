package solonsky.signal.twitter.activities;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivityContentBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Permission;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.ContentViewModel;

/**
 * Created by neura on 01.06.17.
 */

public class ContentActivity extends AppCompatActivity {
    public ArrayList<String> urls = new ArrayList<>();
    public ActivityContentBinding binding;
    private final String TAG = ContentActivity.class.getSimpleName();
    private ContentActivity mActivity;
    private ImageClick imageClick = ImageClick.Share;
    private ContentViewModel viewModel;

    private enum ImageClick {
        Share, Save
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDark);
        }

        mActivity = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        final StatusModel statusModel = AppData.CURRENT_STATUS_MODEL;
        viewModel = new ContentViewModel(statusModel.getUser().getProfileImageUrl(),
                statusModel.getUser().getName(), statusModel.getUser().getFollowersCount() + " followers",
                statusModel.getText(), "1/" + statusModel.getMediaEntities().size(), statusModel.isFavorited(),
                new LocalDateTime(statusModel.getCreatedAt()));

        for (JsonElement media : statusModel.getMediaEntities()) {
            JsonObject mediaEntity = (JsonObject) media;
            urls.add(mediaEntity.get("mediaURLHttps").getAsString());
        }

//        final ArrayList<Fragment> fragments = new ArrayList<>();
//        for (int i = 0; i < urls.size(); i++) {
//            fragments.add(PagerImageFragment.getInstance(urls.get(i), i));
//        }

//        final PagerAdapter pagerAdapter = new SimplePagerAdapter(fragments, getSupportFragmentManager());
//        binding.vpImageMain.setAdapter(pagerAdapter);
//        binding.vpImageMain.setPageTransformer(false, new ParallaxPageTransformer(R.id.img_pager_image));
//        binding.vpImageMain.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
////                ((PagerImageFragment) fragments.get(position)).loadImage(urls.get(position));
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//
//        pagerAdapter.notifyDataSetChanged();

        binding.setModel(viewModel);
        binding.setClick(new ContentViewModel.ContentClickHandler() {
            @Override
            public void onReplyClick(View v) {
                TweetActions.reply(statusModel, mActivity);
            }

            @Override
            public void onRtClick(View v) {
                TweetActions.retweetPopup(mActivity, v, statusModel, new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {
                        Log.e(TAG, "Error while loading data - " + error);
                        Toast.makeText(getApplicationContext(), "Error while loading data - " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onLikeClick(View v) {
                viewModel.setFavorited(!viewModel.isFavorited());
                TweetActions.favorite(viewModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {
                        viewModel.setFavorited(!viewModel.isFavorited());
                    }
                });
            }

            @Override
            public void onShareClick(View v) {
                imageClick = ImageClick.Share;
//                if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    ((PagerImageFragment) fragments.get(binding.vpImageMain.getCurrentItem())).shareImage();
//                } else {
//                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.WRITE_EXTERNAL_REQUEST);
//                }
            }

            @Override
            public void onShareTweetClick(View v) {
                TweetActions.share(statusModel, mActivity);
            }

            @Override
            public void onMoreClick(View v) {
                TweetActions.morePopup(mActivity, v, statusModel, new TweetActions.MoreCallback() {
                    @Override
                    public void onDelete(StatusModel statusModel) {
                        //Do nothing
                    }
                });
            }

            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }

            @Override
            public void onBrowserClick(View v) {
                Utilities.openLink(urls.get(binding.vpImageMain.getCurrentItem()), mActivity);
            }

            @Override
            public void onSaveClick(View v) {
                imageClick = ImageClick.Save;
//                if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    ((PagerImageFragment) fragments.get(binding.vpImageMain.getCurrentItem())).saveImage();
//                } else {
//                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.WRITE_EXTERNAL_REQUEST);
//                }
            }

            @Override
            public void onBottomClick(View v) {
                viewModel.setCollapsed(!viewModel.isCollapsed());
            }
        });

//        binding.txtContentText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                binding.txtContentText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                viewModel.setCollapsed(!viewModel.isCollapsed());
//                changeBottomHeight(10);
//            }
//        });

        new ImageViewer.Builder<>(this, urls)
                .setStartPosition(0)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Permission.WRITE_EXTERNAL_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (imageClick == ImageClick.Share) {
                        binding.getClick().onShareClick(binding.getRoot());
                    } else {
                        binding.getClick().onSaveClick(binding.getRoot());
                    }
                }
                break;
        }
    }

    public void overlayClick() {
//        viewModel.setOverlayed(!viewModel.isOverlayed());
//        binding.tbContent.animate().alpha(viewModel.isOverlayed() ? 1 : 0).setDuration(200).start();
//        binding.llContentContainer.animate().alpha(viewModel.isOverlayed() ? 1 : 0).setDuration(200).start();
//        binding.llContentBottom.animate().alpha(viewModel.isOverlayed() ? 1 : 0).setDuration(200).start();
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                binding.tbContent.setVisibility(viewModel.isOverlayed() ? View.VISIBLE : View.INVISIBLE);
//                binding.llContentContainer.setVisibility(viewModel.isOverlayed() ? View.VISIBLE : View.INVISIBLE);
//                binding.llContentBottom.setVisibility(viewModel.isOverlayed() ? View.VISIBLE : View.INVISIBLE);
//            }
//        }, 200);
    }
}

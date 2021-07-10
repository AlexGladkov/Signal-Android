package solonsky.signal.twitter.activities;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.ImageAnimation;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 17.08.17.
 */

public class ImageOverlay {
    private final String TAG = ImageOverlay.class.getSimpleName();
    private AppCompatActivity appCompatActivity;
    private StatusAdapter.StatusViewHolder statusViewHolder;
    private StatusModel statusModel;
    private ImageViewer imageViewer;
    private int position = 0;
    private boolean isCollapsed = true;
    private ArrayList<String> urls;
    private TextSwitcher txtFollowers;
    private TextView txtText;
    private LinearLayout mLayoutContainer;
    private LinearLayout mLayoutBottom;
    private ImageView imgArrow;

    public ImageOverlay(AppCompatActivity appCompatActivity, StatusAdapter.StatusViewHolder statusViewHolder,
                        StatusModel statusModel, ArrayList<String> urls) {
        this.appCompatActivity = appCompatActivity;
        this.statusViewHolder = statusViewHolder;
        this.statusModel = statusModel;
        this.urls = urls;
    }

    private void changeBottomHeight(int duration) {
        ValueAnimator valueAnimator = isCollapsed ? ValueAnimator.ofInt(19, 0) : ValueAnimator.ofInt(0, 19);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ImageAnimation.setupDisclosureArrow(imgArrow, (Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

        txtFollowers.setText(isCollapsed ?
                Utilities.parseFollowers(statusModel.getUser().getFollowersCount(), "followers") :
                "@" + statusModel.getUser().getScreenName());
        mLayoutBottom.animate().translationY(isCollapsed ?
                mLayoutBottom.getHeight() : 0)
                .setDuration(duration).start();
        mLayoutContainer.animate().translationY(isCollapsed ?
                (txtText.getHeight() + mLayoutBottom.getHeight()) : 0)
                .setDuration(duration).start();
        txtText.animate().alpha(isCollapsed ? 0 : 1).setDuration(duration).start();
    }

    public void setupOverlayWithoutHolder(final View overlay) {
        mLayoutContainer = (LinearLayout) overlay.findViewById(R.id.ll_content_container);
        mLayoutBottom = (LinearLayout) overlay.findViewById(R.id.ll_content_bottom);

        final TextView txtCounter = (TextView) overlay.findViewById(R.id.txt_content_counter);
        final TextView txtUsername = (TextView) overlay.findViewById(R.id.txt_content_username);
        txtFollowers = (TextSwitcher) overlay.findViewById(R.id.txt_content_followers);

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(overlay.getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(overlay.getContext(), android.R.anim.fade_out);

        txtFollowers.setInAnimation(in);
        txtFollowers.setOutAnimation(out);

        txtText = (TextView) overlay.findViewById(R.id.txt_content_text);

        final ImageView btnLike = (ImageView) overlay.findViewById(R.id.img_content_like);
        final ImageView btnReply = (ImageView) overlay.findViewById(R.id.img_content_reply);
        final ImageView btnRt = (ImageView) overlay.findViewById(R.id.img_content_rt);
        final ImageView btnShare = (ImageView) overlay.findViewById(R.id.img_content_share);
        final ImageView btnMore = (ImageView) overlay.findViewById(R.id.img_content_more);
        final ImageView btnChrome = (ImageView) overlay.findViewById(R.id.img_content_chrome);
        final ImageView btnShareImage = (ImageView) overlay.findViewById(R.id.img_content_share_image);
        final ImageView btnBack = (ImageView) overlay.findViewById(R.id.img_content_back);
        final ImageView btnSave = (ImageView) overlay.findViewById(R.id.img_content_save);

        mLayoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCollapsed = !isCollapsed;
                changeBottomHeight(300);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appCompatActivity.getApplicationContext(), "Saving image", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewer == null) return;
                imageViewer.onDismiss();
            }
        });

        btnShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink(urls.get(position), appCompatActivity);
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statusViewHolder.getmBinding().getClick().onLikeClick(v);
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statusViewHolder.getmBinding().getClick().onReplyClick(v);
            }
        });

        btnRt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statusViewHolder.getmBinding().getClick().onRetweetClick(v);
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statusViewHolder.getmBinding().getClick().onShareClick(v);
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                statusViewHolder.getmBinding().getClick().onMoreClick(v);
            }
        });

        final ImageView imgAvatar = (ImageView) overlay.findViewById(R.id.img_content_avatar);

        boolean isNight = App.getInstance().isNightEnabled();

        btnLike.setColorFilter(appCompatActivity.getResources().getColor(isNight ?
                statusModel.isFavorited() ? R.color.dark_like_tint_color : R.color.dark_hint_text_color :
                statusModel.isFavorited() ? R.color.light_like_tint_color : R.color.light_hint_text_color));

        txtCounter.setText("1 of " + urls.size());
        txtUsername.setText(statusModel.getUser().getName());
        txtFollowers.setText(String.valueOf(statusModel.getUser().getFollowersCount()) + " followers");
        txtText.setText(statusModel.getText());

        Picasso.with(appCompatActivity.getApplicationContext()).load(statusModel.getUser().getProfileImageUrl()).into(imgAvatar);

        txtText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                txtText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                changeBottomHeight(10);
            }
        });
    }

    public void setupOverlay(final View overlay) {
        mLayoutContainer = (LinearLayout) overlay.findViewById(R.id.ll_content_container);
        mLayoutBottom = (LinearLayout) overlay.findViewById(R.id.ll_content_bottom);

        final TextView txtCounter = (TextView) overlay.findViewById(R.id.txt_content_counter);
        final TextView txtUsername = (TextView) overlay.findViewById(R.id.txt_content_username);
        txtFollowers = (TextSwitcher) overlay.findViewById(R.id.txt_content_followers);
        imgArrow = (ImageView) overlay.findViewById(R.id.img_content_arrow);

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(overlay.getContext(), R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(overlay.getContext(), R.anim.fade_out);
        in.setDuration(300);
        out.setDuration(300);

        final boolean isNight = App.getInstance().isNightEnabled();

        txtFollowers.setInAnimation(in);
        txtFollowers.setOutAnimation(out);

        txtText = (TextView) overlay.findViewById(R.id.txt_content_text);

        final ImageView imgLike = (ImageView) overlay.findViewById(R.id.img_content_like);
        final FrameLayout btnLike = (FrameLayout) overlay.findViewById(R.id.fl_content_like);
        final FrameLayout btnReply = (FrameLayout) overlay.findViewById(R.id.fl_content_reply);
        final FrameLayout btnRt = (FrameLayout) overlay.findViewById(R.id.fl_content_rt);
        final FrameLayout btnShare = (FrameLayout) overlay.findViewById(R.id.fl_content_share);
        final FrameLayout btnMore = (FrameLayout) overlay.findViewById(R.id.fl_content_more);
        final ImageView btnChrome = (ImageView) overlay.findViewById(R.id.img_content_chrome);
        final ImageView btnShareImage = (ImageView) overlay.findViewById(R.id.img_content_share_image);
        final ImageView btnBack = (ImageView) overlay.findViewById(R.id.img_content_back);
        final ImageView btnSave = (ImageView) overlay.findViewById(R.id.img_content_save);

        mLayoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCollapsed = !isCollapsed;
                changeBottomHeight(300);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewer == null) return;
                imageViewer.onDismiss();
            }
        });

        btnShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink(urls.get(position), appCompatActivity);
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusViewHolder.getmBinding().getClick().onLikeClick(v);
                imgLike.setColorFilter(appCompatActivity.getResources().getColor(isNight ?
                        statusModel.isFavorited() ? R.color.dark_like_tint_color : R.color.dark_hint_text_color :
                        statusModel.isFavorited() ? R.color.light_like_tint_color : R.color.light_hint_text_color));
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusViewHolder.getmBinding().getClick().onReplyClick(v);
            }
        });

        btnRt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusViewHolder.getmBinding().getClick().onRetweetClick(v);
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusViewHolder.getmBinding().getClick().onShareClick(v);
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusViewHolder.getmBinding().getClick().onMoreClick(v);
            }
        });

        final ImageView imgAvatar = (ImageView) overlay.findViewById(R.id.img_content_avatar);

        imgLike.setColorFilter(appCompatActivity.getResources().getColor(isNight ?
                statusModel.isFavorited() ? R.color.dark_like_tint_color : R.color.dark_hint_text_color :
                statusModel.isFavorited() ? R.color.light_like_tint_color : R.color.light_hint_text_color));

        txtCounter.setText("1 of " + urls.size());
        txtUsername.setText(statusModel.getUser().getName());
        txtFollowers.setText(String.valueOf(statusModel.getUser().getFollowersCount()) + " followers");
        txtText.setText(statusModel.getText());

        Picasso.with(appCompatActivity.getApplicationContext()).load(statusModel.getUser().getProfileImageUrl()).into(imgAvatar);

        txtText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                txtText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                changeBottomHeight(10);
            }
        });
    }

    public void setCounter(View overlay) {
        ((TextView) overlay.findViewById(R.id.txt_content_counter)).setText((position + 1) + " of " + urls.size());
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setImageViewer(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }
}

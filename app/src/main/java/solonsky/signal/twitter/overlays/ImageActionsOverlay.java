package solonsky.signal.twitter.overlays;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.ImageAnimation;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 06.09.17.
 */

public class ImageActionsOverlay {
    private final Activity mActivity;
    private final StatusModel statusModel;
    private int imagePosition;
    private ArrayList<String> urls;
    private ImageViewer imageViewer;
    private ImageActionsOverlayClickHandler imageActionsOverlayClickHandler;
    private View overlay;
    private boolean isFavorited;
    private boolean isCollapsed = true;
    private TextView mTxtText;
    private LinearLayout mLayoutContainer;
    private LinearLayout mLayoutBottom;
    private ImageView mImgArrow;
    private TextSwitcher mTxtSubTitle;
    private TextView mTxtCounter;

    public interface ImageActionsOverlayClickHandler {
        void onBackClick(View v);

        void onSaveClick(View v, String url);

        void onShareImageClick(View v, String url);

        void onReplyClick(View v);

        void onRtClick(View v);

        void onLikeClick(View v);

        void onShareClick(View v);

        void onMoreClick(View v);
    }

    public ImageActionsOverlay(Activity mActivity, ArrayList<String> urls, StatusModel statusModel,
                               int startPosition) {
        this.mActivity = mActivity;
        this.urls = urls;
        this.statusModel = statusModel;
        setupImageViewer(startPosition);
    }

    private void changeBottomHeight(int duration) {
        ValueAnimator valueAnimator = isCollapsed ? ValueAnimator.ofInt(19, 0) : ValueAnimator.ofInt(0, 19);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ImageAnimation.setupDisclosureArrow(mImgArrow, (Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

        mTxtSubTitle.setText(isCollapsed ?
                Utilities.parseFollowers(statusModel.getUser().getFollowersCount(), "followers") :
                "@" + statusModel.getUser().getScreenName());
        mLayoutBottom.animate().translationY(isCollapsed ?
                mLayoutBottom.getHeight() : 0)
                .setDuration(duration).start();
        mLayoutContainer.animate().translationY(isCollapsed ?
                (mTxtText.getHeight() + mLayoutBottom.getHeight()) : 0)
                .setDuration(duration).start();
        mTxtText.animate().alpha(isCollapsed ? 0 : 1).setDuration(duration).start();
    }

    public void setupImageViewer(int startPosition) {
        overlay = mActivity.getLayoutInflater().inflate(R.layout.overlay_content_image, null);
        this.imageViewer = new ImageViewer.Builder<>(mActivity, urls)
                .setOverlayView(overlay)
                .setStartPosition(startPosition)
                .setImageChangeListener(new ImageViewer.OnImageChangeListener() {
                    @Override
                    public void onImageChange(int position) {
                        imagePosition = position;
                        if (mTxtCounter != null)
                            mTxtCounter.setText(String.valueOf(position + 1) + " of " + urls.size());
                    }
                })
                .build();

        imageViewer.show();

        Picasso.with(mActivity.getApplicationContext())
                .load(statusModel.getUser().getOriginalProfileImageURL())
                .into((ImageView) overlay.findViewById(R.id.img_content_avatar));

        final TextView mTxtTitle = (TextView) overlay.findViewById(R.id.txt_content_username);
        mTxtText = (TextView) overlay.findViewById(R.id.txt_content_text);
        mLayoutContainer = (LinearLayout) overlay.findViewById(R.id.ll_content_container);
        mLayoutBottom = (LinearLayout) overlay.findViewById(R.id.ll_content_bottom);
        mTxtSubTitle = (TextSwitcher) overlay.findViewById(R.id.txt_content_followers);
        mTxtCounter = (TextView) overlay.findViewById(R.id.txt_content_counter);
        mImgArrow = (ImageView) overlay.findViewById(R.id.img_content_arrow);

        mTxtText.setText(statusModel.getText());
        mTxtTitle.setText(statusModel.getUser().getName());
        mTxtCounter.setText(String.valueOf(startPosition + 1) + " of " + urls.size());
        mTxtSubTitle.setText(Utilities.parseFollowers(statusModel.getUser().getFollowersCount(), "followers"));
        mImgArrow.setImageDrawable(mActivity.getDrawable(R.drawable.disclosure_19));

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(overlay.getContext(), R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(overlay.getContext(), R.anim.fade_out);
        in.setDuration(300);
        out.setDuration(300);

        mTxtSubTitle.setInAnimation(in);
        mTxtSubTitle.setOutAnimation(out);

        mLayoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCollapsed = !isCollapsed;
                changeBottomHeight(300);
            }
        });

        overlay.findViewById(R.id.img_content_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onBackClick(v);
                }
            }
        });

        overlay.findViewById(R.id.img_content_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onSaveClick(v, urls.get(imagePosition));
                }
            }
        });

        overlay.findViewById(R.id.img_content_share_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onShareImageClick(v, urls.get(imagePosition));
                }
            }
        });

        overlay.findViewById(R.id.img_content_chrome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink(urls.get(imagePosition), mActivity);
            }
        });

        overlay.findViewById(R.id.fl_content_reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onReplyClick(v);
                }
            }
        });

        overlay.findViewById(R.id.fl_content_rt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onRtClick(v);
                }
            }
        });

        overlay.findViewById(R.id.fl_content_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onLikeClick(v);
                }
            }
        });

        overlay.findViewById(R.id.fl_content_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onShareClick(v);
                }
            }
        });

        overlay.findViewById(R.id.fl_content_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageActionsOverlayClickHandler != null) {
                    imageActionsOverlayClickHandler.onMoreClick(v);
                }
            }
        });

        mTxtText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTxtTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                changeBottomHeight(10);
            }
        });
    }

    public void changeFavorited() {
        ImageView imageView = (ImageView) overlay.findViewById(R.id.img_content_like);
        imageView.setColorFilter(mActivity.getResources().getColor(isFavorited() ?
                App.getInstance().isNightEnabled() ? R.color.dark_like_tint_color : R.color.light_like_tint_color
                : R.color.light_hint_text_color));
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        isCollapsed = collapsed;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    public ImageViewer getImageViewer() {
        return imageViewer;
    }

    public void setImageViewer(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    public ImageActionsOverlayClickHandler getImageActionsOverlayClickHandler() {
        return imageActionsOverlayClickHandler;
    }

    public void setImageActionsOverlayClickHandler(ImageActionsOverlayClickHandler imageActionsOverlayClickHandler) {
        this.imageActionsOverlayClickHandler = imageActionsOverlayClickHandler;
    }
}

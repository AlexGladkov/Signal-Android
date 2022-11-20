package solonsky.signal.twitter.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.joda.time.LocalDateTime;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.draw.RoundedTransformation;

/**
 * Created by neura on 22.05.17.
 * Implements binding adapter interface
 */
public class Converters {

    private static final String TAG = Converters.class.getSimpleName();

    @BindingAdapter("scrollTo")
    public static void scrollTo(RecyclerView recyclerView, int position) {
        if (recyclerView == null) return;
        recyclerView.scrollToPosition(position);
    }

    @BindingAdapter({"maxLength", "text"})
    public static void setMaxLength(TextView textView, int maxLength, String text) {
        if (textView == null || maxLength == 0 || text == null) return;
        if (text.length() > maxLength) {
            textView.setText(text.substring(0, maxLength - 3) + "...");
        } else {
            textView.setText(text);
        }
    }

    @BindingAdapter({"listConfig"})
    public static void configRecyclerView(RecyclerView recyclerView, ListConfig config) {
        if (config == null) return;
        config.applyConfig(recyclerView.getContext(), recyclerView);
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView imageView, String v) {
        if (v == null || v.equals("")) return;
        Picasso.get().load(v).into(imageView);
    }

    @BindingAdapter({"imageSlightlyUrl"})
    public static void loadImageSlightly(ImageView imageView, String v) {
        if (v == null || v.equals("")) return;
        Picasso.get()
                .load(v)
                .transform(new CirclePicasso(Utilities.convertDpToPixel(2, imageView.getContext()),
                            Utilities.convertDpToPixel(0.5f, imageView.getContext()),
                            25, R.color.black))
                .resize((int) Utilities.convertDpToPixel(64, imageView.getContext())
                        , (int) Utilities.convertDpToPixel(64, imageView.getContext()))
                .centerCrop()
                .into(imageView);
    }

    @BindingAdapter({"imageRoundUrl"})
    public static void loadRoundImage(ImageView imageView, String v) {
        if (v == null || v.equals("")) return;
        Picasso.get().load(v)
                .tag(imageView.getContext())
                .transform(new RoundedTransformation(50, (int) Utilities.convertDpToPixel(0.5f,
                        imageView.getContext())))
                .resize(100, 100)
                .centerCrop()
                .into(imageView);
    }

    @BindingAdapter({"imageGhost"})
    public static void loadGhostImage(ImageView imageView, String v) {
        if (v == null || v.equals("")) return;
        if (v.equals("")) {
            imageView.setVisibility(View.GONE);
            return;
        }
        Picasso.get().load(v)
                .resize((int) Utilities.convertDpToPixel(240, imageView.getContext()),
                        (int) Utilities.convertDpToPixel(135, imageView.getContext()))
                .centerCrop()
                .transform(new RoundedCornersTransformation((int) Utilities.convertDpToPixel(12, imageView.getContext()), 0,
                        RoundedCornersTransformation.CornerType.ALL))
                .into(imageView);
    }

    @BindingAdapter({"parseTime"})
    public static void getCommentTime(TextView textView, LocalDateTime v) {
        if (v == null) {
            textView.setText("");
            return;
        }

        textView.setText(new DateConverter(textView.getContext()).parseTime(v));
    }

    @BindingAdapter({"tintColor"})
    public static void setTintColor(ImageView imageView, int colorResource) {
        imageView.setColorFilter(imageView.getResources().getColor(colorResource));
    }

    @BindingAdapter({"drawableResource"})
    public static void setDrawableResource(ImageView imageView, int drawableResource) {
        imageView.setImageDrawable(imageView.getResources().getDrawable(drawableResource));
    }

    @BindingAdapter({"textColor"})
    public static void setTextColor(TextView textView, int colorResource) {
        textView.setTextColor(textView.getResources().getColor(colorResource));
    }

    @BindingAdapter({"textDifference"})
    public static void setDifferenceText(TextView textView, int count) {
        if (count > 0) {
            textView.setText("+" + count);
        } else {
            textView.setText(String.valueOf(count));
        }
    }

    @BindingAdapter({"imageSystemUrl"})
    public static void setImageSystemUrl(ImageView imageView, int resource) {
        if (resource == 0) return;
        imageView.setImageDrawable(imageView.getResources().getDrawable(resource));
    }

    @BindingAdapter({"backgroundAttr"})
    public static void setBackgroundAttributes(View view, int attr) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = view.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        view.setBackgroundColor(colorInt);
    }

    @BindingAdapter({"backgroundAttrResource"})
    public static void setBackgroundResource(View view, int attr) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = view.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        view.setBackground(view.getResources().getDrawable(colorInt));
    }

    @BindingAdapter({"isHighlight", "isExpand"})
    public static void setBackgroundDrawable(View view, boolean isHighlight, boolean isExpand)  {
        if (view == null) return;
        boolean isNight = App.getInstance().isNightEnabled();
        view.setBackground(view.getResources().getDrawable(
                isHighlight ? isNight ?
                        R.drawable.dark_highlight_swipe_background : R.drawable.light_highlight_swipe_background
                        : isExpand ? isNight ?
                            R.drawable.dark_expand_swipe_background : R.drawable.light_expand_swipe_background
                        : isNight ? R.drawable.dark_status_swipe_background : R.drawable.light_status_swipe_background

        ));
    }

    @BindingAdapter("viewHeight")
    public static void setHeight(View view, int height) {
        if (view == null) return;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter({"backgroundAttrAnimation", "durationRange"})
    public static void setBackgroundAnimation(View view, int attr, int durationRange) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = view.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        int startColor = 0;

        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            startColor = ((ColorDrawable) background).getColor();
        } else {
            startColor = colorInt;
        }

        solonsky.signal.twitter.libs.Animator animator = new solonsky.signal.twitter.libs.Animator(view.getContext());
        animator.changeColorFromAttrs(startColor, colorInt, durationRange, view);
    }

    @BindingAdapter({"tintAttrAnimation", "durationRange"})
    public static void setBackgroundAnimation(ImageView view, int attr, int durationRange) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = view.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        int startColor = 0;

        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            startColor = ((ColorDrawable) background).getColor();
        } else {
            startColor = colorInt;
        }

        solonsky.signal.twitter.libs.Animator animator = new solonsky.signal.twitter.libs.Animator(view.getContext());
        animator.changeColorFilterFromAttrs(startColor, colorInt, durationRange, view);
    }

    @BindingAdapter({"tintColorAttr"})
    public static void setTintColorAttributes(ImageView imageView, int attr) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = imageView.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        imageView.setColorFilter(colorInt);
    }

    @BindingAdapter({"textColorAttr"})
    public static void setTextColorAttributes(TextView textView, int attr) {
        if (attr == 0) return;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = textView.getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        int colorInt = typedValue.data;
        textView.setTextColor(colorInt);
    }

    @BindingAdapter("isVisible")
    public static void setIsVisible(final View view, boolean isVisible) {
        if (isVisible) {
            view.animate().alpha(1).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });
        } else {
            view.animate().alpha(0).setDuration(100).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
        }
    }

    @BindingAdapter("isExpand")
    public static void setExpand(final LinearLayout linearLayout, final boolean isExpand) {
        final solonsky.signal.twitter.libs.Animator animator = new solonsky.signal.twitter.libs.Animator(linearLayout.getContext());
        final int expandDuration = 150;
        final int alphaDuration = 150;
        final int bottomSize = 44;

        if (isExpand) {
            linearLayout.setVisibility(View.VISIBLE);
            animator.changeHeight(0, (int) Utilities.convertDpToPixel(bottomSize, linearLayout.getContext()),
                    expandDuration, linearLayout);
            linearLayout.animate().alpha(1).setDuration(alphaDuration).start();
        } else {
            linearLayout.animate().alpha(0).setDuration(alphaDuration).start();
            animator.changeHeight((int) Utilities.convertDpToPixel(bottomSize, linearLayout.getContext()), 0,
                    expandDuration, linearLayout);
        }
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setStartmMargin(View view, float startMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(Math.round(startMargin), layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:typeface")
    public static void setTypeface(TextView v, String style) {
        switch (style) {
            case "bold":
                v.setTypeface(null, Typeface.BOLD);
                break;

            case "medium":
                v.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                break;

            default:
                v.setTypeface(null, Typeface.NORMAL);
                break;
        }
    }

    public static Flags.MEDIA_TYPE convertType(String type) {
        switch (type.toLowerCase()) {
            case "animated_gif":
                return Flags.MEDIA_TYPE.GIF;

            case "video":
                return Flags.MEDIA_TYPE.VIDEO;

            case "youtube":
                return Flags.MEDIA_TYPE.YOUTUBE;

            default:
                return Flags.MEDIA_TYPE.IMAGE;
        }
    }
}

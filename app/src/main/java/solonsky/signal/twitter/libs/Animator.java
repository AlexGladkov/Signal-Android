package solonsky.signal.twitter.libs;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by neura on 28.05.17.
 * Animator library class for changing height and
 * other stuff with core animation
 */
public class Animator {
    private Context mContext;

    public Animator(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Change color with animation
     * @param startColor - start color R.color
     * @param endColor - end color R.color
     * @param duration - time of transition
     * @param view - view to apply
     */
    public void changeColor(int startColor, int endColor, int duration, final View view) {
        int colorFrom = mContext.getResources().getColor(startColor);
        int colorTo = mContext.getResources().getColor(endColor);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    /**
     * Change color with animation
     * @param startColor - start color R.color
     * @param endColor - end color R.color
     * @param duration - time of transition
     * @param view - view to apply
     */
    public void changeColorFromAttrs(int startColor, int endColor, int duration, final View view) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(duration); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    /**
     * Change color with animation
     * @param startColor - start color R.color
     * @param endColor - end color R.color
     * @param duration - time of transition
     * @param view - view to apply
     */
    public void changeColorFilterFromAttrs(int startColor, int endColor, int duration, final ImageView view) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(duration); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setColorFilter((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    public void changeHeight(int startHeight, int endHeight, int duration, final View v) {
        ValueAnimator va = ValueAnimator.ofInt(startHeight, endHeight);

        va.setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        va.setInterpolator(new DecelerateInterpolator());
        va.start();
    }

    public void changeMarginBottom(int startBottom, int endBottom, int duration, final View v) {
        ValueAnimator va = ValueAnimator.ofInt(startBottom, endBottom);

        va.setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ((RelativeLayout.LayoutParams) v.getLayoutParams()).bottomMargin = (Integer) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        va.start();
    }
}

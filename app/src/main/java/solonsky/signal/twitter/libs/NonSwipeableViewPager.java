package solonsky.signal.twitter.libs;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by neura on 27.05.17.
 */

public class NonSwipeableViewPager extends ViewPager {
    private int mCurrentPagePosition = 0;

    public static class FadePageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            view.setTranslationX(view.getWidth() * -position);

            if(position <= -1.0F || position >= 1.0F) {
                view.setAlpha(0.0F);
            } else if( position == 0.0F ) {
                view.setAlpha(1.0F);
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.setAlpha(1.0F - Math.abs(position));
            }
        }
    }

    public NonSwipeableViewPager(Context context) {
        super(context);
        setMyScroller();
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super has to be called in the beginning so the child views can be
        // initialized.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        if (getChildCount() <= 0)
//            return;
//
//        // Check if the selected layout_height mode is set to wrap_content
//        // (represented by the AT_MOST constraint).
//        boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec)
//                == MeasureSpec.AT_MOST;
//
//        int width = getMeasuredWidth();
//
//        View firstChild = getChildAt(0);
//
//        // Initially set the height to that of the first child - the
//        // PagerTitleStrip (since we always know that it won't be 0).
//        int height = firstChild.getMeasuredHeight();
//
//        if (wrapHeight) {
//
//            // Keep the current measured width.
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
//
//        }
//
//        int fragmentHeight = 0;
//        fragmentHeight = measureFragment(((Fragment) getAdapter().instantiateItem(this, getCurrentItem())).getView());
//
//        // Just add the height of the fragment:
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height + fragmentHeight,
//                MeasureSpec.EXACTLY);
//
//        // super has to be called again so the new specs are treated as
//        // exact measurements.
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureHeight(int measureSpec, View view) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            // set the height from the base view if available
            if (view != null) {
                result = view.getMeasuredHeight();
            }
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    public int measureFragment(View view) {
        if (view == null)
            return 0;

        view.measure(0, 0);
        return view.getMeasuredHeight();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    //down one is added for smooth scrolling

    private void setMyScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyScroller extends Scroller {
        public MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, 1 /*1 secs*/);
        }
    }
}

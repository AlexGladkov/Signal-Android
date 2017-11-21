package solonsky.signal.twitter.libs;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.daimajia.swipe.SwipeLayout;

/**
 * Created by neura on 13.07.17.
 */

public class CustomViewPager extends ViewPager {

    private SwipeLayout swipeLayout = null;
    private final String TAG = CustomViewPager.class.getSimpleName();

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        if (swipeLayout != null) {
//            swipeLayout.setLeftSwipeEnabled(false);
//        }

//        if (childId > 0) {
//            View scroll = findViewById(childId);
//            if (scroll != null) {
//                Rect rect = new Rect();
//                scroll.getHitRect(rect);
//                if (rect.contains((int) event.getX(), (int) event.getY())) {
//                    return false;
//                }
//            }
//        }
        return super.onInterceptTouchEvent(event);
    }

    public SwipeLayout getSwipeLayout() {
        return swipeLayout;
    }

    public void setSwipeLayout(SwipeLayout swipeLayout) {
        this.swipeLayout = swipeLayout;
    }
}

package solonsky.signal.twitter.libs;

import android.content.Context;
import android.util.AttributeSet;

import com.daimajia.swipe.SwipeLayout;

/**
 * Created by neura on 05.09.17.
 */

public class MySwipeLayout extends SwipeLayout {
    private SwipeListener layout = null;

    public MySwipeLayout(Context context) {
        super(context);
    }

    public MySwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void addSwipeListener(SwipeListener l) {
        if (layout != null) {
            removeSwipeListener(layout);
        }

        super.addSwipeListener(l);
        this.layout = l;
    }

    @Override
    public void removeSwipeListener(SwipeListener l) {
        super.removeSwipeListener(l);
    }
}

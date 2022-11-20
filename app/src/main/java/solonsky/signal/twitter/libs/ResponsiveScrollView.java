package solonsky.signal.twitter.libs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

/**
 * Created by neura on 29.06.17.
 */

public class ResponsiveScrollView extends NestedScrollView {

    public interface OnEndScrollListener {
        public void onEndScroll(int scrollY);
    }

    private boolean mIsFling;
    private OnEndScrollListener mOnEndScrollListener;

    public ResponsiveScrollView(Context context) {
        this(context, null, 0);
    }

    public ResponsiveScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResponsiveScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
        mIsFling = true;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (mIsFling) {
            if (Math.abs(y - oldY) < 2 || y >= getMeasuredHeight() || y == 0) {
                if (mOnEndScrollListener != null) {
                    mOnEndScrollListener.onEndScroll(y);
                }
                mIsFling = false;
            }
        }
    }

    public OnEndScrollListener getOnEndScrollListener() {
        return mOnEndScrollListener;
    }

    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.mOnEndScrollListener = mOnEndScrollListener;
    }
}

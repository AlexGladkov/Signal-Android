package solonsky.signal.twitter.libs;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by neura on 31.10.17.
 */

public class ChatRecyclerView extends RecyclerView {
    private static final String TAG = ChatRecyclerView.class.getSimpleName();

    public ChatRecyclerView(Context context) {
        super(context);
    }

    public ChatRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }
}

package solonsky.signal.twitter.draw;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Created by neura on 03.09.17.
 */

public class FancyLinearLayoutManager extends LinearLayoutManager {

    public FancyLinearLayoutManager(Context context) {
        super(context);
    }

    public FancyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public FancyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return super.supportsPredictiveItemAnimations();
    }
}

package solonsky.signal.twitter.libs;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by neura on 23.05.17.
 */

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final String TAG = OnSwipeTouchListener.class.getSimpleName();
    //    private final GestureDetector gestureDetector;
    private SwipeHandler swipeHandler;

    public OnSwipeTouchListener(SwipeHandler swipeHandler) {
        this.swipeHandler = swipeHandler;
    }

    public interface SwipeHandler {
        void onSwipeX(float diffX);

        void onSwipeBottom();

        void onSwipeTop();

        void onHandleRelease(int duration);
    }

    private boolean isPressed = false;
    private boolean isMinTreshold = false;
    private float diffX = -1;
    private float startPosition = -1;
    private float oldX = -1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                isMinTreshold = false;
                startPosition = -1;
                break;

            case MotionEvent.ACTION_UP:
                isPressed = false;
                swipeHandler.onHandleRelease(100);
                break;

            case MotionEvent.ACTION_MOVE:
                if (isPressed) {
                    if (startPosition == -1) startPosition = event.getX();

                    diffX = startPosition - event.getX();
                    int MIN_THRESHOLD = 200;

                    if (Math.abs(diffX) > MIN_THRESHOLD) isMinTreshold = true;
//                    && ((Math.abs(diffX) - Math.abs(oldX)) > 5 || oldX == -1)

                    if (Math.abs(diffX) > MIN_THRESHOLD && diffX > 0) {
                        if (Math.abs(diffX - oldX) > 1)
                            swipeHandler.onSwipeX(Math.abs(diffX) - MIN_THRESHOLD);
                    } else {
                        swipeHandler.onHandleRelease(0);
                    }

                    oldX = diffX;
                }
                break;
        }

        return false;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private SwipeHandler swipeHandler;
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        public GestureListener(SwipeHandler swipeHandler) {
            this.swipeHandler = swipeHandler;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffX > 0) {
//                            swipeHandler.onSwipeRight(diffX);
//                        } else {
//                            swipeHandler.onSwipeLeft(diffX);
//                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        swipeHandler.onSwipeBottom();
                    } else {
                        swipeHandler.onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}

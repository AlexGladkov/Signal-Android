package solonsky.signal.twitter.draw;

import android.support.v4.view.ViewPager;
import android.view.View;

import solonsky.signal.twitter.R;

/**
 * Created by neura on 08.08.17.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer {
    private final String TAG = ParallaxPageTransformer.class.getSimpleName();

    private final int viewToParallax;

    public ParallaxPageTransformer(final int viewToParallax) {
        this.viewToParallax = viewToParallax;

    }

    @Override
    public void transformPage(View view, float position) {
        if (position > -1 && position < 1) {
            int pageWidth = view.getWidth();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(1);
            } else if (position <= 1) { // [-1,1]
                view.findViewById(R.id.zdv_image).setAlpha(1 - position);
                view.findViewById(R.id.zdv_image).setTranslationX(-position * (pageWidth / 2)); //Half the normal speed
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(1);
            }
        }

//        Log.e(TAG, "position = " + position);
//        if (position < -1) { // [-Infinity,-1)
////            view.setAlpha(-position);
//        } else if (position <= 1) { // [-1,1]
//            if (position > 0 && position < 1) {
//                view.setAlpha(1.0f - position);
//                view.setTranslationX(-position * (((float) pageWidth) / 1.5f)); //Half the normal speed
////                if (position == 1) view.animate().alpha(0).setDuration(50).start();
//            }
//        } else { // (1,+Infinity]
//            // This page is way off-screen to the right.
//        }
    }
}

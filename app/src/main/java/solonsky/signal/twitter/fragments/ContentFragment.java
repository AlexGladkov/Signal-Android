package solonsky.signal.twitter.fragments;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daimajia.swipe.SwipeLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ContentActivity;
import solonsky.signal.twitter.helpers.Utilities;

/**
 * Created by neura on 15.07.17.
 */

public class ContentFragment extends Fragment {
    private final String TAG = ContentFragment.class.getSimpleName();
    private ImageView mImgContent;
    private ContentActivity mActivity;
    private View mViewBackground;
    private String path;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        mActivity = (ContentActivity) getActivity();

        SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(R.id.sl_content);

        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.view_content_left));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, view.findViewById(R.id.view_content_right));
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                mActivity.onBackPressed();
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                mViewBackground.setAlpha(1.0f + ((float) leftOffset / 1080.0f));
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        mImgContent = (ImageView) view.findViewById(R.id.img_content);
        mViewBackground = view.findViewById(R.id.view_background);
        setContentImage(path);
        return view;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContentImage(String path) {
        if (mImgContent != null) {
            Log.e(TAG, "path - " + path);
            Picasso.with(getContext()).load(path).into(mImgContent, new Callback() {
                @Override
                public void onSuccess() {
                    mViewBackground.setBackgroundColor(Utilities.getAverageColor(((BitmapDrawable)mImgContent.getDrawable()).getBitmap()));
                }

                @Override
                public void onError(Exception e) {

                }

            });
//            Picasso.with(getContext()).load(path).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    Log.e(TAG, "loaded");
//                    mImgContent.setImageBitmap(bitmap);
//                }
//
//                @Override
//                public void onBitmapFailed(Drawable errorDrawable) {
//                    Log.e(TAG, "Failed");
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                    Log.e(TAG, "Prepare");
//                }
//            });
        }
    }
}

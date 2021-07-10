package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.wingsofts.dragphotoview.DragPhotoView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ContentActivity;

/**
 * Created by neura on 01.06.17.
 */

public class PagerImageFragment extends Fragment {
    private final String TAG = PagerImageFragment.class.getSimpleName();
    private DragPhotoView imageView;
    private String url;
    private Bitmap extracted = null;
    private String extractedPath = null;
    private int tag;
    private ContentActivity mActivity;

    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;

    private boolean isBottomEdge = false;
    private boolean isTopEdge = false;

    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    private View viewHierarchy;
    private RelativeLayout backView;

    public static PagerImageFragment getInstance(String url, int tag) {
        PagerImageFragment imageFragment = new PagerImageFragment();
        imageFragment.setUrl(url, tag);
        return imageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewHierarchy = inflater.inflate(R.layout.fragment_pager_image, container, false);
        viewHierarchy.setTag(tag);

        mActivity = (ContentActivity) getActivity();

        imageView = (DragPhotoView) viewHierarchy.findViewById(R.id.img_pager_image);
        imageView.setOnExitListener(new DragPhotoView.OnExitListener() {
            @Override
            public void onExit(DragPhotoView dragPhotoView, float v, float v1, float v2, float v3) {
                mActivity.finish();
            }
        });

//        backView = (RelativeLayout) viewHierarchy.findViewById(R.id.rl_pager_image);
//        final SwipeableLayout swipeableLayout = (SwipeableLayout) viewHierarchy.findViewById(R.id.sl_pager_image);
//        swipeableLayout.setOnLayoutPercentageChangeListener(new OnLayoutPercentageChangeListener() {
//            private float lastAlpha = 1.0f;
//
//            @Override
//            public void percentageY(float percentage) {
//                float alphaCorrector = percentage / 2;
//                AlphaAnimation alphaAnimation = new AlphaAnimation(lastAlpha, 1 - alphaCorrector);
//                alphaAnimation.setDuration(300);
//                backView.startAnimation(alphaAnimation);
//                lastAlpha = 1 - alphaCorrector;
//            }
//        });
//
//        swipeableLayout.setOnSwipedListener(new OnLayoutSwipedListener() {
//            @Override
//            public void onLayoutSwiped() {
//                mActivity.finish();
//            }
//        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.overlayClick();
            }
        });
//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
//        imageView.setOnTouchChangeListener(new OnTouchChangedListener() {
//            @Override
//            public void onTouch(View v, MotionEvent event) {
//                final int Y = (int) event.getRawY();
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                        // save default base layout height
//                        defaultViewHeight = viewHierarchy.getHeight();
//                        isBottomEdge = false;
//                        isTopEdge = false;
//
//                        // init finger and view position
//                        previousFingerPosition = Y;
//                        baseLayoutPosition = (int) viewHierarchy.getY();
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        // If user was doing a scroll up
//                        if (isScrollingUp) {
//                            // Reset baselayout position
//                            viewHierarchy.setY(0);
//                            // We are not in scrolling up mode anymore
//                            isScrollingUp = false;
//                        }
//
//                        // If user was doing a scroll down
//                        if (isScrollingDown) {
//                            // Reset baselayout position
//                            viewHierarchy.setY(0);
//                            // Reset base layout size
//                            viewHierarchy.getLayoutParams().height = defaultViewHeight;
//                            viewHierarchy.requestLayout();
//                            // We are not in scrolling down mode anymore
//                            isScrollingDown = false;
//                        }
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        if (!isClosing) {
//                            int currentYPosition = (int) viewHierarchy.getY();
//                            Log.e(TAG, "height - " + viewHierarchy.getHeight());
//                            Log.e(TAG, "Y - " + Y);
//                            Log.e(TAG, "alpha - " + (1 - Math.abs((Y - (viewHierarchy.getHeight() / 2f)) / (viewHierarchy.getHeight() / 2))));
//
//                            float alpha = 1 - Math.abs((Y - (viewHierarchy.getHeight() / 2f)) / (viewHierarchy.getHeight() / 2));
//
//                            backView.setAlpha(alpha);
//                            mActivity.binding.llContentBottom.setAlpha(alpha);
//                            mActivity.binding.llContentContainer.setAlpha(alpha);
//                            mActivity.binding.tbContent.setAlpha(alpha);
//
//                            // If we scroll up
//                            if (previousFingerPosition > Y) {
//                                // First time android rise an event for "up" move
//                                if (!isScrollingUp) {
//                                    isScrollingUp = true;
//                                }
//
//                                // Has user scroll down before -> view is smaller than it's default size -> resize it instead of change it position
//                                if (viewHierarchy.getHeight() < defaultViewHeight) {
//                                    viewHierarchy.getLayoutParams().height = viewHierarchy.getHeight() - (Y - previousFingerPosition);
//                                    viewHierarchy.requestLayout();
//                                } else {
//                                    // Has user scroll enough to "auto close" popup ?
//                                    if ((baseLayoutPosition - currentYPosition) > defaultViewHeight / 4) {
//                                        isTopEdge = true;
//                                        return;
//                                    }
//
//                                    //
//                                }
//                                viewHierarchy.setY(viewHierarchy.getY() + (Y - previousFingerPosition));
//
//                            }
//                            // If we scroll down
//                            else {
//
//                                // First time android rise an event for "down" move
//                                if (!isScrollingDown) {
//                                    isScrollingDown = true;
//                                }
//
//                                // Has user scroll enough to "auto close" popup ?
//                                if (Math.abs(baseLayoutPosition - currentYPosition) > defaultViewHeight / 2) {
//                                    isBottomEdge = true;
//                                    return;
//                                }
//
//                                // Change base layout size and position (must change position because view anchor is top left corner)
//                                viewHierarchy.setY(viewHierarchy.getY() + (Y - previousFingerPosition));
//                                viewHierarchy.getLayoutParams().height = viewHierarchy.getHeight() - (Y - previousFingerPosition);
//                                viewHierarchy.requestLayout();
//                            }
//
//                            // Update position
//                            previousFingerPosition = Y;
//                        }
//                        break;
//                }
//            }
//        });

        Picasso.with(getContext()).load(url).into(imageView);
        return viewHierarchy;
    }

    private void closeUpAndDismissDialog(int currentYPosition) {

    }

    private void closeDownAndDismissDialog(int currentYPosition) {

    }

    public void setUrl(String url, int tag) {
        this.url = url;
        this.tag = tag;
    }

    public void saveImage() {
        if (extracted == null) {
            extracted = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        if (extractedPath == null) {
            String[] splitted = url.split("/");
            String fileName = splitted.length == 0 ? "signal_temporary_image" : splitted[splitted.length - 1];

            extractedPath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
        }

        createFile(true);
    }

    public void shareImage() {
        if (extracted == null) {
            extracted = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        if (extractedPath == null) {
            String[] splitted = url.split("/");
            String fileName = splitted.length == 0 ? "signal_temporary_image" : splitted[splitted.length - 1];

            extractedPath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
        }

        createFile(false);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(extractedPath));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void createFile(boolean isSave) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        extracted.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(extractedPath);
        try {
            boolean created = f.createNewFile();
            if (created) {
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                if (isSave)
                    Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "can't create file");
            }
        } catch (IOException e) {
            Log.e(TAG, "can't create file " + e.getLocalizedMessage());
        }
    }
}

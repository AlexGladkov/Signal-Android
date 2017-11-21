package solonsky.signal.twitter.overlays;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import solonsky.signal.twitter.R;

/**
 * Created by neura on 06.09.17.
 */

public class ImageOverlay {
    private final AppCompatActivity mActivity;
    private ArrayList<String> urls;
    private ArrayList<Bitmap> bitmaps;
    private ImageViewer imageViewer;
    private ImageOverlayClickHandler imageOverlayClickHandler;
    private int imagePosition = 0;

    public interface ImageOverlayClickHandler {
        void onBackClick(View v);
        void onSaveClick(View v, String url);
    }

    public ImageOverlay(ArrayList<String> urls, AppCompatActivity mActivity, int startPosition) {
        this.mActivity = mActivity;
        this.urls = urls;
        setupImageViewer(startPosition, 0);
    }

    public ImageOverlay(ArrayList<Bitmap> bitmaps, AppCompatActivity mActivity) {
        this.mActivity = mActivity;
        this.bitmaps = bitmaps;
        setupImageViewer(0, 1);
    }

    private void setupImageViewer(int startPosition, int type) {
        View overlay = mActivity.getLayoutInflater().inflate(R.layout.overlay_profile, null);
        overlay.findViewById(R.id.img_profile_content_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageOverlayClickHandler != null) {
                    imageOverlayClickHandler.onBackClick(v);
                }
            }
        });

        overlay.findViewById(R.id.img_profile_content_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageOverlayClickHandler != null) {
                    imageOverlayClickHandler.onSaveClick(v, urls.get(imagePosition));
                }
            }
        });

        if (type == 0) {
            this.imageViewer = new ImageViewer.Builder<>(mActivity, urls)
                    .setOverlayView(overlay)
                    .setStartPosition(startPosition)
                    .setImageChangeListener(new ImageViewer.OnImageChangeListener() {
                        @Override
                        public void onImageChange(int position) {
                            imagePosition = position;
                        }
                    })
                    .build();
        } else {
            this.imageViewer = new ImageViewer.Builder<>(mActivity, bitmaps)
                    .setOverlayView(overlay)
                    .setStartPosition(startPosition)
                    .setImageChangeListener(new ImageViewer.OnImageChangeListener() {
                        @Override
                        public void onImageChange(int position) {
                            imagePosition = position;
                        }
                    })
                    .build();
        }
        imageViewer.show();
    }

    public void setImageOverlayClickHandler(ImageOverlayClickHandler imageOverlayClickHandler) {
        this.imageOverlayClickHandler = imageOverlayClickHandler;
    }

    public ImageViewer getImageViewer() {
        return imageViewer;
    }

    public void setImageViewer(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }
}

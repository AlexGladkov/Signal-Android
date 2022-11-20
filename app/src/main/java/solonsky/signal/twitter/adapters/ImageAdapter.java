package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellImageBinding;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.overlays.ImageOverlay;

/**
 * Created by neura on 28.05.17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final String TAG = "IMAGEADAPTER";
    private final ArrayList<ImageModel> models;
    private final Context mContext;
    private final AppCompatActivity mActivity;

    public ImageAdapter(ArrayList<ImageModel> models, Context mContext, AppCompatActivity mActivity) {
        this.models = models;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellImageBinding binding = CellImageBinding.inflate(inflater, parent, false);
        return new ImageAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ImageAdapter.ViewHolder holder, int position) {
        final ImageModel model = models.get(position);
        holder.mBinding.setModel(model);
        int containerWidth = Utilities.getScreenWidth(mActivity) / 2;

        Picasso.get().load(model.getPreviewUrl().equals("") ?
                model.getImageUrl() : model.getPreviewUrl())
                .resize(containerWidth, containerWidth)
                .centerCrop()
//                .transform(new CirclePicasso(
//                        Utilities.convertDpToPixel(4, mContext),
//                        Utilities.convertDpToPixel(0.5f, mContext),
//                        25, Color.BLACK
//
//                ))
                .into(holder.mBinding.imgImageMain);

        holder.mBinding.setClick(new ImageModel.ImageClickHandler() {
            @Override
            public void onItemClick(View v) {
                ArrayList<String> urls = new ArrayList<>();
                int position = 0;
                int i = 0;
                for (ImageModel imageModel : models) {
                    if (imageModel.getMediaType().equals(Flags.MEDIA_TYPE.IMAGE)) {
                        urls.add(imageModel.getImageUrl());
                        if (model == imageModel) position = i;
                    }
                    i++;
                }

                final ImageOverlay imageOverlay = new ImageOverlay(urls, mActivity, position);
                imageOverlay.setImageOverlayClickHandler(new ImageOverlay.ImageOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                        downloadFiles.saveFile(url, mActivity.getString(R.string.download_url));
                    }
                });
            }

            @Override
            public boolean onLongItemClick(View v) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellImageBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

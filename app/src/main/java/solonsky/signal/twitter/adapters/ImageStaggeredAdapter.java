package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellImageStaggeredBinding;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ImageModel;

/**
 * Created by neura on 28.05.17.
 */

public class ImageStaggeredAdapter extends RecyclerView.Adapter<ImageStaggeredAdapter.ViewHolder> {
    private final String TAG = "IMAGEADAPTER";
    private final ArrayList<ImageModel> models;
    private final ImageStaggeredListener clickListener;
    private final int width;
    private final Context mContext;

    public interface ImageStaggeredListener {
        void onClick(ImageModel imageModel, View v);
    }

    public ImageStaggeredAdapter(ArrayList<ImageModel> models, Context context,
                                 ImageStaggeredListener clickListener, int width) {
        this.models = models;
        this.mContext = context;
        this.clickListener = clickListener;
        this.width = width;
    }

    @Override
    public ImageStaggeredAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellImageStaggeredBinding binding = CellImageStaggeredBinding.inflate(inflater, parent, false);
        return new ImageStaggeredAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(ImageStaggeredAdapter.ViewHolder holder, int position) {
        final ImageModel model = models.get(position);
        int containerWidth = width;
        int containerHeight = (int) (containerWidth * 0.6);

        int imageWidth = 0;
        int imageHeight = 0;

        switch (models.size()) {
            case 1:
                imageHeight = containerHeight;
                imageWidth = containerWidth;
                break;

            case 2:
                imageWidth = containerWidth / 2;
                imageHeight = containerHeight;
                break;

            case 3:
                imageWidth = position == 0 ? containerWidth : containerWidth / 2;
                imageHeight = containerHeight / 2;
                break;

            case 4:
                imageHeight = containerHeight / 2;
                imageWidth = containerWidth / 2;
                break;
        }


        Context context = holder.mBinding.getRoot().getContext();
        if (imageHeight > 0 || imageWidth > 0) {
            Picasso.get().load(model.getImageUrl())
                    .resize(imageWidth, imageHeight).centerCrop()
                    .transform(new CirclePicasso(
                            Utilities.convertDpToPixel(4, context),
                            Utilities.convertDpToPixel(1f, context),
                            25, R.color.black))
                    .into(holder.mBinding.imgStaggeredMain);
        }

        if (model.getMediaType().equals(Flags.MEDIA_TYPE.IMAGE)) {
            holder.mBinding.imgStaggeredMedia.setVisibility(View.GONE);
        } else {
            holder.mBinding.imgStaggeredMedia.setVisibility(View.VISIBLE);
        }

        holder.mBinding.imgStaggeredMedia.setImageResource(model.getMediaType().equals(Flags.MEDIA_TYPE.VIDEO) ?
                R.drawable.ic_badges_media_video : model.getMediaType().equals(Flags.MEDIA_TYPE.GIF) ?
                R.drawable.ic_badges_media_gif : R.drawable.ic_badges_media_youtube);
        holder.mBinding.setModel(model);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(model, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellImageStaggeredBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

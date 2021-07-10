package solonsky.signal.twitter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellImageStaggeredBinding;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ImageModel;

/**
 * Created by neura on 28.05.17.
 */
public class MediaStaggeredAdapter extends RecyclerView.Adapter<MediaStaggeredAdapter.ViewHolder> {
    private final String TAG = MediaStaggeredAdapter.class.getSimpleName();
    private final ArrayList<ImageModel> models;
    private final ImageStaggeredListener clickListener;
    private final AppCompatActivity mActivity;

    public interface ImageStaggeredListener {
        void onClick(ImageModel imageModel, View v);
    }

    public MediaStaggeredAdapter(ArrayList<ImageModel> models, AppCompatActivity mActivity,
                                 ImageStaggeredListener clickListener) {
        this.models = models;
        this.mActivity = mActivity;
        this.clickListener = clickListener;
    }

    @Override
    public MediaStaggeredAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellImageStaggeredBinding binding = CellImageStaggeredBinding.inflate(inflater, parent, false);
        return new MediaStaggeredAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final MediaStaggeredAdapter.ViewHolder holder, int position) {
        final ImageModel model = models.get(position);
        int containerWidth = Utilities.getScreenWidth(mActivity);
        int containerHeight = (containerWidth);

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

            default:
                imageHeight = containerHeight / 2;
                imageWidth = containerWidth / 2;
                break;
        }

        Picasso.with(mActivity.getApplicationContext()).load(model.getPreviewUrl().equals("") ?
                model.getImageUrl() : model.getPreviewUrl())
                .resize(imageWidth, imageHeight).centerCrop()
                .into(holder.mBinding.imgStaggeredMain, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mBinding.imgStaggeredMedia.setVisibility(
                                model.getMediaType().equals(Flags.MEDIA_TYPE.IMAGE) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }


                });

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

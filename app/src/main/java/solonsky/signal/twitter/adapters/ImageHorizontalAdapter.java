package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.util.ArrayList;

import solonsky.signal.twitter.activities.MediaActivity;
import solonsky.signal.twitter.databinding.CellImageHorizontalBinding;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.helpers.VideoRequestHandler;
import solonsky.signal.twitter.models.ImageModel;

/**
 * Created by neura on 28.05.17.
 */

public class ImageHorizontalAdapter extends RecyclerView.Adapter<ImageHorizontalAdapter.ViewHolder> {
    private final String TAG = ImageHorizontalAdapter.class.getSimpleName();
    private final ArrayList<ImageModel> models;
    private final Context mContext;
    private final ImageClickListener imageClickListener;
    private final AppCompatActivity mActivity;

    public interface ImageClickListener {
        void onItemClick(View v, ImageModel model);
    }

    public ImageHorizontalAdapter(ArrayList<ImageModel> models, AppCompatActivity mActivity, ImageClickListener imageClickListener) {
        this.models = models;
        this.mActivity = mActivity;
        this.mContext = mActivity.getApplicationContext();
        this.imageClickListener = imageClickListener;
    }

    @Override
    public ImageHorizontalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellImageHorizontalBinding binding = CellImageHorizontalBinding.inflate(inflater, parent, false);
        return new ImageHorizontalAdapter.ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ImageHorizontalAdapter.ViewHolder holder, int position) {
        final ImageModel model = models.get(position);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.mBinding.imageLlMain.getLayoutParams();
        params.rightMargin = position == models.size() - 1 ? (int) Utilities.convertDpToPixel(8, mContext) : 0;
        holder.mBinding.imageLlMain.setLayoutParams(params);

        final File imageFile = new File(model.getImageUrl());

        if (imageFile.canRead() && imageFile.exists()) {
            Picasso picassoInstance;
            if (model.getMediaType().equals(Flags.MEDIA_TYPE.VIDEO)) {
                picassoInstance = new Picasso.Builder(mContext)
                        .addRequestHandler(new VideoRequestHandler())
                        .build();

                picassoInstance.load(VideoRequestHandler.SCHEME_VIDEO + ":" + imageFile.getAbsolutePath())
                        .resize((int) Utilities.convertDpToPixel(64, mContext),
                                (int) Utilities.convertDpToPixel(64, mContext))
                        .centerCrop()
                        .transform(new CirclePicasso(
                                Utilities.convertDpToPixel(4, mContext),
                                Utilities.convertDpToPixel(0.5f, mContext),
                                25, Color.BLACK

                        ))
                        .into(holder.mBinding.imageImgMain);
            } else {
                Picasso.with(mContext).load(imageFile)
                        .resize((int) Utilities.convertDpToPixel(64, mContext),
                                (int) Utilities.convertDpToPixel(64, mContext))
                        .centerCrop()
                        .transform(new CirclePicasso(
                                Utilities.convertDpToPixel(4, mContext),
                                Utilities.convertDpToPixel(0.5f, mContext),
                                25, Color.BLACK

                        ))
                        .into(holder.mBinding.imageImgMain);
            }
        } else {
            return;
        }

        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new ImageModel.ImageClickHandler() {
            @Override
            public void onItemClick(View v) {
                imageClickListener.onItemClick(v, model);
            }

            @Override
            public boolean onLongItemClick(View v) {
                if (model.getMediaType().equals(Flags.MEDIA_TYPE.IMAGE)) {
                    ArrayList<String> urls = new ArrayList<>();
                    urls.add(model.getImageUrl());
                    new ImageViewer.Builder<>(mActivity, urls)
                            .setStartPosition(0)
                            .build()
                            .show();
                } else {
                    AppData.MEDIA_URL = (model.getImageUrl());
                    AppData.MEDIA_TYPE = (model.getMediaType());
                    mActivity.startActivity(new Intent(mActivity.getApplicationContext(), MediaActivity.class));
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CellImageHorizontalBinding mBinding;

        public ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellDirectBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Styling;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.DirectModel;

/**
 * Created by neura on 23.05.17.
 */

public class DirectAdapter extends RecyclerView.Adapter<DirectAdapter.DirectViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<DirectModel> mDirectModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;
    private final DirectClickHandler mDirectClick;

    public interface DirectClickHandler {
        void onItemClick(View v, DirectModel directModel);
        void onAvatarClick(View v, DirectModel directModel);
    }

    public DirectAdapter(ArrayList<DirectModel> mDirectModels, Context mContext,
                         AppCompatActivity mActivity, DirectClickHandler directClickHandler) {
        this.mDirectModels = mDirectModels;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mDirectClick = directClickHandler;
    }

    @Override
    public DirectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellDirectBinding binding = CellDirectBinding.inflate(inflater, parent, false);
        return new DirectViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(DirectViewHolder holder, int position) {
        final DirectModel model = mDirectModels.get(position);
        final String createdAt = model.getDateTime().toString("MMM dd");

        boolean isNight = App.getInstance().isNightEnabled();
        holder.mBinding.setModel(model);

        applyStyle(holder);
        holder.mBinding.directCreatedAt.setText(createdAt);
        holder.mBinding.directDivider.setBackgroundColor(mContext.getResources().getColor(model.isHighlighted() ? isNight ?
                    R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color :
                isNight ? R.color.dark_divider_color : R.color.light_divider_color));

//        if (AppData.appConfiguration.isRoundAvatars()) {
//            holder.mBinding.directCivAvatar.setVisibility(View.VISIBLE);
//            holder.mBinding.directImgAvatar.setVisibility(View.GONE);
//        } else {
//            holder.mBinding.directCivAvatar.setVisibility(View.GONE);
//            holder.mBinding.directImgAvatar.setVisibility(View.VISIBLE);
//
//            Picasso.with(mContext)
//                    .load(model.getIconUrl())
//                    .resize((int) Utilities.convertDpToPixel(40, mContext),
//                            (int) Utilities.convertDpToPixel(40, mContext))
//                    .centerCrop()
//                    .transform(new CirclePicasso(
//                            Utilities.convertDpToPixel(4, mContext),
//                            Utilities.convertDpToPixel(0.5f, mContext),
//                            25, R.color.black))
//                    .into(holder.mBinding.directImgAvatar);
//        }

        holder.mBinding.directCivAvatar.setBorderOverlay(!isNight);
        holder.mBinding.directCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        holder.mBinding.directCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, mContext));

        holder.mBinding.setClick(new DirectModel.DirectClickHandler() {
            @Override
            public void onItemClick(View v) {
                mDirectClick.onItemClick(v, model);
            }
        });
        holder.mBinding.directCivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDirectClick.onAvatarClick(v, model);
            }
        });
    }

    private void applyStyle(DirectViewHolder holder) {
        Styling styling = new Styling(mContext, Styling.convertFontToStyle(AppData.appConfiguration.getFontSize()));
        holder.mBinding.directTxtLastMessage.setLineSpacing(styling.getTextExtra(), 1);
        holder.mBinding.directTxtLastMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        holder.mBinding.directTxtUsername.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        holder.mBinding.directCreatedAt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getCreatedAtSize());

        // Square avatar
        LinearLayout.LayoutParams baseParams = (LinearLayout.LayoutParams) holder.mBinding.directImgAvatar.getLayoutParams();
        baseParams.bottomMargin = styling.getBaseMargin();
        baseParams.topMargin = styling.getSquareAvatarMarginTop();
        holder.mBinding.directImgAvatar.setLayoutParams(baseParams);

        // Round avatar
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.directCivAvatar.getLayoutParams();
        baseParams.topMargin = styling.getBaseMargin();
        baseParams.bottomMargin = styling.getBaseMargin();
        holder.mBinding.directCivAvatar.setLayoutParams(baseParams);
    }

    @Override
    public long getItemId(int position) {
        return mDirectModels.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mDirectModels.size();
    }

    public static class DirectViewHolder extends RecyclerView.ViewHolder {
        CellDirectBinding mBinding;

        public DirectViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

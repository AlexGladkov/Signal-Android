package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellNotificationsMainBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.NotificationModel;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsMainAdapter extends RecyclerView.Adapter<NotificationsMainAdapter.NotificationViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final List<NotificationModel> mModels = new ArrayList<>();
    private ItemClickListener clickListener;

    public interface ItemClickListener {
        void onItemClick(NotificationModel model, View v);
        void onAvatarClick(NotificationModel model, View v);
    }

    public void setData(List<NotificationModel> notificationModels) {
        mModels.clear();
        mModels.addAll(notificationModels);
        notifyDataSetChanged();
    }

    public void attachListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellNotificationsMainBinding binding = CellNotificationsMainBinding.inflate(inflater, parent, false);
        return new NotificationViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        final NotificationModel model = mModels.get(position);
        holder.bind(model, position == mModels.size() - 1, clickListener);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CellNotificationsMainBinding mBinding;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        public void bind(final NotificationModel model, boolean position, final ItemClickListener clickListener) {
            final boolean isNight = App.getInstance().isNightEnabled();
            final boolean isQuote = model.getType() == NotificationModel.TYPE_QUOTE;
            final Context mContext = itemView.getContext();

            model.setDivideState(position ? Flags.DIVIDER_LONG : Flags.DIVIDER_SHORT);

            mBinding.notificationMainQuote.setVisibility(isQuote ? View.VISIBLE : View.GONE);
            mBinding.txtNotificationMainQuoteTitle.setVisibility(isQuote ? View.VISIBLE : View.GONE);
            mBinding.txtNotificationMainQuoteText.setVisibility(isQuote ? View.VISIBLE : View.GONE);

            if (isQuote) {
                mBinding.txtNotificationMainQuoteTitle.setText(model.getStatusModel().getQuotedStatus().getUser().getName());
                mBinding.txtNotificationMainQuoteText.setText(model.getStatusModel().getQuotedStatus().getText());
            }

            switch (model.getType()) {
                case NotificationModel.TYPE_REPLY:
                    mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                            R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
                    mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                            R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
                    break;

                case NotificationModel.TYPE_QUOTE:
                case NotificationModel.TYPE_RT:
                    mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                            R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
                    mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                            R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
                    break;

                case NotificationModel.TYPE_LIKE:
                    mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                            R.color.dark_like_tint_color : R.color.light_like_tint_color));
                    mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                            R.color.dark_like_tint_color : R.color.light_like_tint_color));
                    break;

                case NotificationModel.TYPE_FOLLOW:
                case NotificationModel.TYPE_LIST:
                    mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                            R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                    mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                            R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                    break;
            }

            mBinding.setModel(model);
            mBinding.notificationDivider.setBackgroundColor(mContext.getResources().getColor(model.isHighlighted() ? isNight ?
                    R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color :
                    isNight ? R.color.dark_divider_color : R.color.light_divider_color));

            mBinding.notificationCivAvatar.setBorderOverlay(!isNight);
            mBinding.notificationCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            mBinding.notificationCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, mContext));

            mBinding.notificationCivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onAvatarClick(model, v);
                }
            });

            mBinding.setClick(new NotificationModel.NotificationClickHandler() {
                @Override
                public void onItemClick(View v) {
                    if (clickListener != null)
                        clickListener.onItemClick(model, v);
                }
            });
        }
    }
}

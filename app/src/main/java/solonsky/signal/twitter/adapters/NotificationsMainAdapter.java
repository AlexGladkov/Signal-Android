package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
    private final ArrayList<NotificationModel> notificationModels;
    private final Context mContext;
    private final ItemClickListener clickListener;

    public interface ItemClickListener {
        void onItemClick(NotificationModel model, View v);
    }

    public NotificationsMainAdapter(ArrayList<NotificationModel> notificationModels,
                                    Context mContext, ItemClickListener clickListener) {
        this.notificationModels = notificationModels;
        this.mContext = mContext;
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
        final NotificationModel model = notificationModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();
        final boolean isQuote = model.getType() == NotificationModel.TYPE_QUOTE;

        model.setDivideState(position == notificationModels.size() - 1 ? Flags.DIVIDER_LONG : Flags.DIVIDER_SHORT);

        holder.mBinding.notificationMainQuote.setVisibility(isQuote ? View.VISIBLE : View.GONE);
        holder.mBinding.txtNotificationMainQuoteTitle.setVisibility(isQuote ? View.VISIBLE : View.GONE);
        holder.mBinding.txtNotificationMainQuoteText.setVisibility(isQuote ? View.VISIBLE : View.GONE);

        if (isQuote) {
            holder.mBinding.txtNotificationMainQuoteTitle.setText(model.getStatusModel().getQuotedStatus().getUser().getName());
            holder.mBinding.txtNotificationMainQuoteText.setText(model.getStatusModel().getQuotedStatus().getText());
        }

        switch (model.getType()) {
            case NotificationModel.TYPE_REPLY:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
                holder.mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                        R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
                break;

            case NotificationModel.TYPE_QUOTE:
            case NotificationModel.TYPE_RT:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
                holder.mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                        R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
                break;

            case NotificationModel.TYPE_LIKE:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_like_tint_color : R.color.light_like_tint_color));
                holder.mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                        R.color.dark_like_tint_color : R.color.light_like_tint_color));
                break;

            case NotificationModel.TYPE_FOLLOW:
            case NotificationModel.TYPE_LIST:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                holder.mBinding.notificationTxtType.setTextColor(mContext.getResources().getColor(isNight ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;
        }

        holder.mBinding.setModel(model);
        holder.mBinding.notificationDivider.setBackgroundColor(mContext.getResources().getColor(model.isHighlighted() ? isNight ?
                R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color :
                isNight ? R.color.dark_divider_color : R.color.light_divider_color));

        holder.mBinding.notificationCivAvatar.setBorderOverlay(!isNight);
        holder.mBinding.notificationCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        holder.mBinding.notificationCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, mContext));

        holder.mBinding.setClick(new NotificationModel.NotificationClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickListener.onItemClick(model, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CellNotificationsMainBinding mBinding;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

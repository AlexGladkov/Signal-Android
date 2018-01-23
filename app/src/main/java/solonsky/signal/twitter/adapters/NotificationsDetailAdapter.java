package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellNotificationsDetailBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.NotificationDetailModel;
import solonsky.signal.twitter.models.NotificationModel;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsDetailAdapter extends RecyclerView.Adapter<NotificationsDetailAdapter.DetailViewHolder> {
    private final String TAG = NotificationDetailModel.class.getSimpleName();
    private final List<NotificationDetailModel> detailModels;
    private final Context mContext;
    private final DetailClickListener clickListener;

    public interface DetailClickListener {
        void onItemClick(NotificationDetailModel model, View v);
    }

    public NotificationsDetailAdapter(List<NotificationDetailModel> detailModels,
                                      Context mContext, NotificationsDetailAdapter.DetailClickListener clickListener) {
        this.detailModels = detailModels;
        this.mContext = mContext;
        this.clickListener = clickListener;
    }

    @Override
    public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellNotificationsDetailBinding binding = CellNotificationsDetailBinding.inflate(inflater, parent, false);
        return new DetailViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(DetailViewHolder holder, int position) {
        final NotificationDetailModel model = detailModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();
        model.setDivideState(position == detailModels.size() - 1 ? Flags.DIVIDER_LONG : Flags.DIVIDER_SHORT);

        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new NotificationDetailModel.DetailClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickListener.onItemClick(model, v);
            }

            @Override
            public void onFollowClick(View v) {
                model.setFollowed(true);
                AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void onException(TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                        model.setFollowed(false);
                    }
                });
                asyncTwitter.createFriendship(model.getId());
            }
        });

        holder.mBinding.detailCivAvatar.setBorderOverlay(!isNight);
        holder.mBinding.detailCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        holder.mBinding.detailCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, mContext));

        switch (model.getType()) {
            case NotificationDetailModel.NOTIFICATION_REPLY:
            case NotificationDetailModel.NOTIFICATION_MENTIONED:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
                break;

            case NotificationDetailModel.NOTIFICATION_RETWEET:
            case NotificationDetailModel.NOTIFICATION_QUOTED:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
                break;

            case NotificationDetailModel.NOTIFICATION_LIKE:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_like_tint_color : R.color.light_like_tint_color));
                break;

            case NotificationDetailModel.NOTIFICATION_FOLLOW:
                holder.mBinding.notificationImgBadge.setColorFilter(mContext.getResources().getColor(isNight ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return detailModels.size();
    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        CellNotificationsDetailBinding mBinding;

        public DetailViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

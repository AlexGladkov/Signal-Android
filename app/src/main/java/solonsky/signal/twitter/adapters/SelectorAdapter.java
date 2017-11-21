package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellSelectorBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;

/**
 * Created by neura on 24.05.17.
 */

public class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.SelectorViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<UserModel> mUserModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;
    private final SelectorClickHandler clickHandler;

    public interface SelectorClickHandler {
        void onItemClick(UserModel model, View v);
    }

    public SelectorAdapter(ArrayList<UserModel> mUserModels, Context mContext,
                           AppCompatActivity mActivity, SelectorClickHandler clickHandler) {
        this.mUserModels = mUserModels;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.clickHandler = clickHandler;
    }

    @Override
    public SelectorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellSelectorBinding binding = CellSelectorBinding.inflate(inflater, parent, false);
        return new SelectorViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(SelectorViewHolder holder, int position) {
        final UserModel model = mUserModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();

        holder.mBinding.userDividerBottom.setVisibility(position == mUserModels.size() - 1 ?
            View.GONE : View.VISIBLE);

        holder.mBinding.selectorImgAvatar.setBorderOverlay(!isNight);
        holder.mBinding.selectorImgAvatar.setBorderColor(isNight ?
                Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        holder.mBinding.selectorImgAvatar.setBorderWidth(isNight ?
                0 : (int) Utilities.convertDpToPixel(0.5f, mContext));


        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new UserModel.UserClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickHandler.onItemClick(model, v);
            }

            @Override
            public void onFollowClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserModels.size();
    }

    public static class SelectorViewHolder extends RecyclerView.ViewHolder {
        CellSelectorBinding mBinding;

        public SelectorViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

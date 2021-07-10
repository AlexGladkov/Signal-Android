package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellMuteUserBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;

/**
 * Created by neura on 24.05.17.
 */

public class MuteUserAdapter extends RecyclerView.Adapter<MuteUserAdapter.UserViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<UserModel> mUserModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;
    private final MuteUserClickHandler clickHandler;

    public interface MuteUserClickHandler {
        void onItemClick(UserModel model, View v);
    }

    public MuteUserAdapter(ArrayList<UserModel> mUserModels, Context mContext,
                           AppCompatActivity mActivity, MuteUserClickHandler clickHandler) {
        this.mUserModels = mUserModels;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.clickHandler = clickHandler;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellMuteUserBinding binding = CellMuteUserBinding.inflate(inflater, parent, false);
        return new UserViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        final UserModel model = mUserModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();

        holder.mBinding.userCivAvatar.setBorderOverlay(!isNight);
        holder.mBinding.userCivAvatar.setBorderColor(isNight ?
                Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        holder.mBinding.userCivAvatar.setBorderWidth(isNight ?
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

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CellMuteUserBinding mBinding;

        public UserViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

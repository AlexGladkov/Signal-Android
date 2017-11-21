package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.CellUserHorizontalBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;

/**
 * Created by neura on 24.05.17.
 */

public class UserHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<UserModel> mUserModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;
    private final UserClickHandler clickHandler;
    private final int SEARCH_TYPE = 0;
    private final int CELL_TYPE = 1;

    public interface UserClickHandler {
        void onItemClick(UserModel model, View v);
        void onSearchClick(UserModel model, View v);
    }

    public UserHorizontalAdapter(ArrayList<UserModel> mUserModels, Context mContext,
                                 AppCompatActivity mActivity, UserClickHandler clickHandler) {
        this.mUserModels = mUserModels;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.clickHandler = clickHandler;
    }

    @Override
    public long getItemId(int position) {
        return mUserModels.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return mUserModels.get(position).getTwitterName().equals("") ? SEARCH_TYPE : CELL_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == SEARCH_TYPE) {
            return new SearchViewHolder(inflater.inflate(R.layout.cell_user_search, parent, false));
        } else {
            CellUserHorizontalBinding binding = CellUserHorizontalBinding.inflate(inflater, parent, false);
            return new UserViewHolder(binding.getRoot());
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final UserModel model = mUserModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();

        if (holder instanceof SearchViewHolder) {
            ((SearchViewHolder) holder).bind(model, clickHandler);
        } else {
            UserViewHolder realHolder = (UserViewHolder) holder;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) realHolder.mBinding.userLlMain.getLayoutParams();
            params.rightMargin = position == mUserModels.size() - 1 ? (int) Utilities.convertDpToPixel(12, mContext) : 0;
            realHolder.mBinding.userLlMain.setLayoutParams(params);

            realHolder.mBinding.userCivAvatar.setBorderOverlay(!isNight);
            realHolder.mBinding.userCivAvatar.setBorderColor(isNight ?
                    Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            realHolder.mBinding.userCivAvatar.setBorderWidth(isNight ?
                    0 : (int) Utilities.convertDpToPixel(0.5f, mContext));

            if (model.getAvatarUrl().equals("")) model.setAvatarUrl("blabla"); // Path must not be empty
            Picasso.with(mContext).load(model.getAvatarUrl()).placeholder(isNight ?
                    R.drawable.ic_generic_avatar_t1 : R.drawable.ic_generic_avatar_t2)
                    .into(realHolder.mBinding.userCivAvatar);

            realHolder.mBinding.setModel(model);
            realHolder.mBinding.setClick(new UserModel.UserClickHandler() {
                @Override
                public void onItemClick(View v) {
                    clickHandler.onItemClick(model, v);
                }

                @Override
                public void onFollowClick(View v) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUserModels.size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTxtText;
        private final ImageView mImgImage;
        private final CircularProgressView mCpv;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mTxtText = (TextView) itemView.findViewById(R.id.user_search_text);
            mImgImage = (ImageView) itemView.findViewById(R.id.user_search_img);
            mCpv = (CircularProgressView) itemView.findViewById(R.id.user_search_cpv);
        }

        public void bind(final UserModel userModel, final UserClickHandler userClickHandler) {
            mTxtText.setText(userModel.getUsername());
            mImgImage.setVisibility(View.VISIBLE);
            mCpv.setVisibility(View.GONE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userClickHandler.onSearchClick(userModel, v);
                }
            });
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CellUserHorizontalBinding mBinding;

        public UserViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

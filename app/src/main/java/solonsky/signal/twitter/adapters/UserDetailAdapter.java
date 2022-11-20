package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.CellDetailUserBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.UserModel;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by neura on 24.05.17.
 */

public class UserDetailAdapter extends RecyclerView.Adapter<UserDetailAdapter.UserViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final List<UserModel> mUserModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;
    private final UserClickHandler clickHandler;

    public interface UserClickHandler {
        void onItemClick(UserModel model, View v);
    }

    public UserDetailAdapter(List<UserModel> mUserModels, Context mContext,
                             AppCompatActivity mActivity, UserClickHandler clickHandler) {
        this.mUserModels = mUserModels;
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.clickHandler = clickHandler;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CellDetailUserBinding binding = CellDetailUserBinding.inflate(inflater, parent, false);
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

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mBinding.userDividerBottom.getLayoutParams();
        params.leftMargin = position == mUserModels.size() - 1 ? 0 : (int) Utilities.convertDpToPixel(72, mActivity.getApplicationContext());
        holder.mBinding.userDividerBottom.setLayoutParams(params);

        holder.mBinding.setModel(model);
        holder.mBinding.setClick(new UserModel.UserClickHandler() {
            @Override
            public void onItemClick(View v) {
                clickHandler.onItemClick(model, v);
            }

            @Override
            public void onFollowClick(View v) {
                final Handler handler = new Handler();
                final Gson gson = new Gson();
                model.setFollowed(true);
                AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.createFriendship(model.getId());
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void onException(TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, mContext.getString(R.string.error_friend_add), Toast.LENGTH_SHORT).show();
                                model.setFollowed(false);
                            }
                        });
                    }

                    @Override
                    public void createdFriendship(User user) {
                        super.createdFriendship(user);
                        solonsky.signal.twitter.models.User userModel = solonsky.signal.twitter.models.User.getFromUserInstance(user);
                        UsersData.getInstance().getFollowingList().add(0, user.getId());
                        UsersData.getInstance().getUsersList().add(userModel);
                        UsersData.getInstance().saveFollowingList();
                        UsersData.getInstance().saveUsersList();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.success_followed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserModels.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CellDetailUserBinding mBinding;

        public UserViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

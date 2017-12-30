package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.databinding.CellChatOtherBinding;
import solonsky.signal.twitter.databinding.CellChatUserBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener;
import solonsky.signal.twitter.models.ChatModel;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by neura on 29.05.17.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<ChatModel> mChatModels;
    private final Context mContext;
    private final ChatActivity mActivity;
    private final ChatClickListener chatClickListener;

    public interface ChatClickListener {
        void onItemClick(View v, ChatModel chatModel);
        void onAvatarClick(View v, ChatModel chatModel);
    }

    public ChatAdapter(ArrayList<ChatModel> mChatModels, Context mContext, ChatActivity activity, ChatClickListener chatClickListener) {
        this.mChatModels = mChatModels;
        this.mContext = mContext;
        this.mActivity = activity;
        this.chatClickListener = chatClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AppData.CHAT_ME) {
            CellChatUserBinding binding = CellChatUserBinding.inflate(inflater, parent, false);
            return new UserViewHolder(binding.getRoot());
        } else {
            CellChatOtherBinding binding = CellChatOtherBinding.inflate(inflater, parent, false);
            return new OtherViewHolder(binding.getRoot());
        }
    }

    private AutoLinkOnClickListener autoLinkOnClickListener = new AutoLinkOnClickListener() {
        @Override
        public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
            if (autoLinkMode.equals(AutoLinkMode.MODE_SHORT) || autoLinkMode.equals(AutoLinkMode.MODE_URL)) {
                Utilities.openLink(matchedText, mActivity);
            } else if (autoLinkMode.equals(AutoLinkMode.MODE_EMAIL)) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");

                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{matchedText});
                i.putExtra(Intent.EXTRA_SUBJECT, "Email from Signal");

                try {
                    mActivity.startActivity(Intent.createChooser(i, "Send e-mail"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mActivity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } else if (autoLinkMode.equals(AutoLinkMode.MODE_MENTION)) {
                Intent intent = new Intent(mActivity, MVPProfileActivity.class);
                intent.putExtra(Flags.PROFILE_SCREEN_NAME, matchedText.replace("@", ""));
                mActivity.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }

        @Override
        public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {

        }
    };

    @Override
    public int getItemViewType(int position) {
        return mChatModels.get(position).getType();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ChatModel model = mChatModels.get(position);
        final boolean isNight = App.getInstance().isNightEnabled();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatClickListener.onItemClick(v, model);
            }
        });

        if (model.getType() == AppData.CHAT_ME) {
            ((UserViewHolder) holder).binding.chatUserCivAvatar.setBorderOverlay(!isNight);
            ((UserViewHolder) holder).binding.chatUserCivAvatar.setBorderColor(isNight ?
                    Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            ((UserViewHolder) holder).binding.chatUserCivAvatar.setBorderWidth(isNight ?
                    0 : (int) Utilities.convertDpToPixel(0.5f, mContext));
            ((UserViewHolder) holder).binding.chatUserCivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatClickListener.onAvatarClick(v, model);
                }
            });

            ((UserViewHolder) holder).binding.chatUserContent.addAutoLinkMode(
                    AutoLinkMode.MODE_MENTION, AutoLinkMode.MODE_URL,
                    AutoLinkMode.MODE_SHORT, AutoLinkMode.MODE_EMAIL);

            ((UserViewHolder) holder).binding.chatUserContent.setShortUrls(model.getShortUrls());
            ((UserViewHolder) holder).binding.chatUserContent.setAutoLinkOnClickListener(autoLinkOnClickListener);

            ((UserViewHolder) holder).binding.setModel(model);
            ((UserViewHolder) holder).binding.chatUserTxtTime.setText(model.getTime());
            ((UserViewHolder) holder).binding.chatUserContent.setAutoLinkText(model.getText());
//            ((UserViewHolder) holder).binding.chatUserSl.addSwipeListener(swipeListener);
        } else {
            ((OtherViewHolder) holder).binding.chatOtherCivAvatar.setBorderOverlay(!isNight);
            ((OtherViewHolder) holder).binding.chatOtherCivAvatar.setBorderColor(isNight ?
                    Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            ((OtherViewHolder) holder).binding.chatOtherCivAvatar.setBorderWidth(isNight ?
                    0 : (int) Utilities.convertDpToPixel(0.5f, mContext));
            ((OtherViewHolder) holder).binding.chatOtherCivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatClickListener.onAvatarClick(v, model);
                }
            });

            ((OtherViewHolder) holder).binding.chatOtherContent.addAutoLinkMode(
                    AutoLinkMode.MODE_MENTION, AutoLinkMode.MODE_URL,
                    AutoLinkMode.MODE_SHORT, AutoLinkMode.MODE_EMAIL);
            ((OtherViewHolder) holder).binding.chatOtherContent.setShortUrls(model.getShortUrls());
            ((OtherViewHolder) holder).binding.chatOtherContent.setAutoLinkOnClickListener(autoLinkOnClickListener);

            ((OtherViewHolder) holder).binding.setModel(model);
            ((OtherViewHolder) holder).binding.chatOtherTxtTime.setText(model.getTime());
            ((OtherViewHolder) holder).binding.chatOtherContent.setAutoLinkText(model.getText());
        }

        if (!model.getImageUrl().equals("")) {
            if (model.getType() == AppData.CHAT_ME) {
                switch (model.getMediaType()) {
                    case GIF:
                        ((UserViewHolder) holder).binding.chatUserType.
                                setImageResource(R.drawable.ic_badges_media_gif);
                        break;

                    case VIDEO:
                        ((UserViewHolder) holder).binding.chatUserType.
                                setImageResource(R.drawable.ic_badges_media_video);
                        break;

                    case YOUTUBE:
                        ((UserViewHolder) holder).binding.chatUserType.
                                setImageResource(R.drawable.ic_badges_media_youtube);
                        break;

                    default:
                        ((UserViewHolder) holder).binding.chatUserType.
                                setVisibility(View.GONE);
                        break;
                }
            } else {
                switch (model.getMediaType()) {
                    case GIF:
                        ((OtherViewHolder) holder).binding.chatOtherType.
                                setImageResource(R.drawable.ic_badges_media_gif);
                        break;

                    case VIDEO:
                        ((OtherViewHolder) holder).binding.chatOtherType.
                                setImageResource(R.drawable.ic_badges_media_video);
                        break;

                    case YOUTUBE:
                        ((OtherViewHolder) holder).binding.chatOtherType.
                                setImageResource(R.drawable.ic_badges_media_youtube);
                        break;

                    default:
                        ((OtherViewHolder) holder).binding.chatOtherType.
                                setVisibility(View.GONE);
                        break;
                }
            }

            if (model.getImageUrl().contains("ton.twitter.com")) {
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Twitter twitter = Utilities.getTwitterInstance();
                        try {
                            InputStream stream = twitter.getDMImageAsStream(model.getImageUrl());
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
                            Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                            Bitmap croppedBmp = ThumbnailUtils.extractThumbnail(bmp,
                                    (int) Utilities.convertDpToPixel(240, mContext),
                                    (int) Utilities.convertDpToPixel(135, mContext));
                            final Bitmap roundedBmp = Utilities.getRoundedCornerBitmap(croppedBmp,
                                    (int) Utilities.convertDpToPixel(12, mContext));

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (holder instanceof UserViewHolder) {
                                        ((UserViewHolder) holder).binding.chatUserImage.setImageBitmap(roundedBmp);
                                    } else {
                                        ((OtherViewHolder) holder).binding.chatOtherImage.setImageBitmap(roundedBmp);
                                    }

//                                    handler.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            mActivity.getBinding().recyclerChat.smoothScrollBy(0,
//                                                    mActivity.getBinding().recyclerChat.getHeight() * 4);
//                                        }
//                                    }, 100);
                                }
                            });
                        } catch (TwitterException e) {
                            Log.e(TAG, "Error to load - " + e.getLocalizedMessage());
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatModels.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {
        CellChatUserBinding binding;

        public UserViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    private static class OtherViewHolder extends RecyclerView.ViewHolder {
        CellChatOtherBinding binding;

        public OtherViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}

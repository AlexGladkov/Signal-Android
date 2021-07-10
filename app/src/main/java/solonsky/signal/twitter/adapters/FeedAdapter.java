package solonsky.signal.twitter.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.regex.Pattern;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.activities.ContentActivity;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.databinding.CellFeedGifBinding;
import solonsky.signal.twitter.databinding.CellFeedImageBinding;
import solonsky.signal.twitter.databinding.CellFeedQuoteBinding;
import solonsky.signal.twitter.databinding.CellFeedTextBinding;
import solonsky.signal.twitter.databinding.CellFeedVideoBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.libs.WordSpan;
import solonsky.signal.twitter.models.FeedGifModel;
import solonsky.signal.twitter.models.FeedImageModel;
import solonsky.signal.twitter.models.FeedModel;
import solonsky.signal.twitter.models.FeedQuoteModel;
import solonsky.signal.twitter.models.FeedTextModel;
import solonsky.signal.twitter.models.FeedVideoModel;

/**
 * Created by neura on 22.05.17.
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<FeedModel> mFeedModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;

    public FeedAdapter(ArrayList<FeedModel> feedModels, Context context, AppCompatActivity mActivity) {
        this.mFeedModels = feedModels;
        this.mContext = context;
        this.mActivity = mActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AppData.FEED_TYPE_IMAGE) {
            CellFeedImageBinding binding = CellFeedImageBinding.inflate(inflater, parent, false);
            return new ImageViewHolder(binding.getRoot());
        } else if (viewType == AppData.FEED_TYPE_VIDEO) {
            CellFeedVideoBinding binding = CellFeedVideoBinding.inflate(inflater, parent, false);
            return new VideoViewHolder(binding.getRoot());
        } else if (viewType == AppData.FEED_TYPE_GIF) {
            CellFeedGifBinding binding = CellFeedGifBinding.inflate(inflater, parent, false);
            return new GifViewHolder(binding.getRoot());
        } else if (viewType == AppData.FEED_TYPE_QUOTE) {
            CellFeedQuoteBinding binding = CellFeedQuoteBinding.inflate(inflater, parent, false);
            return new QuoteViewHolder(binding.getRoot());
        } else {
            CellFeedTextBinding binding = CellFeedTextBinding.inflate(inflater, parent, false);
            return new TextViewHolder(binding.getRoot());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mFeedModels.get(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final FeedModel feedModel = mFeedModels.get(position);
        final FeedModel.FeedClickHandler feedClickHandler = new FeedModel.FeedClickHandler() {
            @Override
            public void onItemClick(View v) {
                feedModel.setExpand(!feedModel.isExpand());
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onShareClick(View v) {
                performShare(feedModel.getText());
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onLikeClick(View v) {
                feedModel.setFavorite(!feedModel.isFavorite());
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onRetweetClick(View v) {
                performRetweet(v, feedModel);
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onReplyClick(View v) {
                performReply(feedModel);
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onMoreClick(View v) {
                performMore(v, feedModel);
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public void onContentClick(View v) {
                performContent(feedModel);
                if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
            }

            @Override
            public boolean longUserClick(View v) {
                final MaterialDialog materialDialog = new MaterialDialog.Builder(mContext)
                        .customView(R.layout.dialog_dark_user, false)
                        .build();

                setupProfileDialog(materialDialog, feedModel, feedModel.getUsername());
                materialDialog.show();
                return true;
            }

            @Override
            public boolean longItemClick(View v) {
                Toast.makeText(mContext, "Item long click", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        final SwipeLayout.SwipeListener swipeListener = new SwipeLayout.SwipeListener() {
            @Override public void onOpen(final SwipeLayout layout) {
                AppData.CURRENT_TWEET_MODEL = (feedModel);
                mActivity.startActivity(new Intent(mContext, DetailActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.close();
                    }
                }, 300);
            }

            @Override public void onStartOpen(SwipeLayout layout) {}
            @Override public void onStartClose(SwipeLayout layout) {}
            @Override public void onClose(SwipeLayout layout) {}
            @Override public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}
            @Override public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
        };

        switch (feedModel.getType()) {
            case AppData.FEED_TYPE_IMAGE:
                ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                parseText(feedModel.getText(), imageViewHolder.mBinding.feedImageTxtText, feedModel);

                imageViewHolder.mBinding.setModel((FeedImageModel) feedModel);
                imageViewHolder.mBinding.setClick(feedClickHandler);
                imageViewHolder.mBinding.feedImageBottomWrapper.addSwipeListener(swipeListener);
                break;

            case AppData.FEED_TYPE_VIDEO:
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                parseText(feedModel.getText(), videoViewHolder.mBinding.feedVideoTxtText, feedModel);

                videoViewHolder.mBinding.setModel((FeedVideoModel) feedModel);
                videoViewHolder.mBinding.setClick(feedClickHandler);
                videoViewHolder.mBinding.feedVideoBottomWrapper.addSwipeListener(swipeListener);
                break;

            case AppData.FEED_TYPE_TEXT:
                TextViewHolder textViewHolder = (TextViewHolder) holder;
                parseText(feedModel.getText(), textViewHolder.mBinding.feedTextTxtText, feedModel);

                textViewHolder.mBinding.setModel((FeedTextModel) feedModel);
                textViewHolder.mBinding.setClick(feedClickHandler);
                textViewHolder.mBinding.feedTextBottomWrapper.addSwipeListener(swipeListener);
                break;

            case AppData.FEED_TYPE_GIF:
                GifViewHolder gifViewHolder = (GifViewHolder) holder;
                parseText(feedModel.getText(), gifViewHolder.mBinding.feedGifTxtText, feedModel);

                gifViewHolder.mBinding.setModel((FeedGifModel) feedModel);
                gifViewHolder.mBinding.setClick(feedClickHandler);
                gifViewHolder.mBinding.feedGifBottomWrapper.addSwipeListener(swipeListener);
                break;

            case AppData.FEED_TYPE_QUOTE:
                QuoteViewHolder quoteViewHolder = (QuoteViewHolder) holder;
                parseText(feedModel.getText(), quoteViewHolder.mBinding.feedQuoteTxtText, feedModel);

                quoteViewHolder.mBinding.setModel((FeedQuoteModel) feedModel);
                quoteViewHolder.mBinding.setClick(feedClickHandler);
                quoteViewHolder.mBinding.feedQuoteBottomWrapper.addSwipeListener(swipeListener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mFeedModels.size();
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final CellFeedImageBinding mBinding;

        ImageViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final CellFeedVideoBinding mBinding;

        VideoViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    private static class GifViewHolder extends RecyclerView.ViewHolder {
        private final CellFeedGifBinding mBinding;

        GifViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    private static class TextViewHolder extends RecyclerView.ViewHolder {
        private final CellFeedTextBinding mBinding;

        TextViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    private static class QuoteViewHolder extends RecyclerView.ViewHolder {
        private final CellFeedQuoteBinding mBinding;

        QuoteViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    /**
     * Setup colors to each element of cell for text view holder
     * @param context - context
     * @param holder - textViewHolder
     * @param feedModel - input model
     */
    private void setupTextViewHolderColors(Context context, TextViewHolder holder, FeedModel feedModel) {
        boolean isNightEnabled = App.getInstance().isNightEnabled();
        holder.mBinding.feedTextMain.setBackgroundColor(context.getResources().getColor(
                feedModel.isExpand() ? isNightEnabled ? R.color.dark_background_secondary_color : R.color.light_background_secondary_color
                        : feedModel.isHighlighted() ?
                                isNightEnabled ? R.color.dark_background_highlight_color : R.color.light_background_highlight_color :
                                isNightEnabled ? R.color.dark_background_color : R.color.light_background_color


        ));
    }

    /**
     * Setup colors to each element of cell for gif view holder
     * @param context - context
     * @param holder - gifViewHolder
     * @param feedModel - input model
     */
    private void setupGifViewHolderColors(Context context, GifViewHolder holder, FeedModel feedModel) {
        boolean isNightEnabled = App.getInstance().isNightEnabled();
        holder.mBinding.feedGifMain.setBackgroundColor(context.getResources().getColor(
                feedModel.isExpand() ? isNightEnabled ? R.color.dark_background_secondary_color : R.color.light_background_secondary_color
                        : feedModel.isHighlighted() ?
                        isNightEnabled ? R.color.dark_background_highlight_color : R.color.light_background_highlight_color :
                        isNightEnabled ? R.color.dark_background_color : R.color.light_background_color


        ));
    }

    /**
     * Setup colors to each element of cell for quote view holder
     * @param context - context
     * @param holder - quoteViewHolder
     * @param feedModel - input model
     */
    private void setupQuoteViewHolderColors(Context context, QuoteViewHolder holder, FeedModel feedModel) {
        boolean isNightEnabled = App.getInstance().isNightEnabled();
        holder.mBinding.feedQuoteMain.setBackgroundColor(context.getResources().getColor(
                feedModel.isExpand() ? isNightEnabled ? R.color.dark_background_secondary_color : R.color.light_background_secondary_color
                        : feedModel.isHighlighted() ?
                        isNightEnabled ? R.color.dark_background_highlight_color : R.color.light_background_highlight_color :
                        isNightEnabled ? R.color.dark_background_color : R.color.light_background_color


        ));
    }

    /**
     * Setup colors to each element of cell for image view holder
     * @param context - context
     * @param holder - imageViewHolder
     * @param feedModel - input model
     */
    private void setupImageViewHolderColors(Context context, ImageViewHolder holder, FeedModel feedModel) {
        boolean isNightEnabled = App.getInstance().isNightEnabled();
        holder.mBinding.feedImageMain.setBackgroundColor(context.getResources().getColor(
                feedModel.isExpand() ? isNightEnabled ? R.color.dark_background_secondary_color : R.color.light_background_secondary_color
                        : feedModel.isHighlighted() ?
                        isNightEnabled ? R.color.dark_background_highlight_color : R.color.light_background_highlight_color :
                        isNightEnabled ? R.color.dark_background_color : R.color.light_background_color


        ));
    }

    /**
     * Setup colors to each element of cell for video view holder
     * @param context - context
     * @param holder - videoViewHolder
     * @param feedModel - input model
     */
    private void setupVideoViewHolderColors(Context context, VideoViewHolder holder, FeedModel feedModel) {
        boolean isNightEnabled = App.getInstance().isNightEnabled();
        holder.mBinding.feedVideoMain.setBackgroundColor(context.getResources().getColor(
                feedModel.isExpand() ? isNightEnabled ? R.color.dark_background_secondary_color : R.color.light_background_secondary_color
                        : feedModel.isHighlighted() ?
                        isNightEnabled ? R.color.dark_background_highlight_color : R.color.light_background_highlight_color :
                        isNightEnabled ? R.color.dark_background_color : R.color.light_background_color


        ));
    }

    private void setupProfileDialog(final MaterialDialog materialDialog, final FeedModel feedModel,
                                    String url) {
        View view = materialDialog.getView();
        materialDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView title = (TextView) view.findViewById(R.id.dialog_user_title);
        title.setText(url + " follows you");

        view.findViewById(R.id.dialog_reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AppData.CURRENT_COMPOSE = AppData.COMPOSE_REPLY;
                AppData.CURRENT_TWEET_MODEL = (feedModel);
                mActivity.startActivity(new Intent(mContext, ComposeActivity.class));
                materialDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.startActivity(new Intent(mContext, ChatActivity.class));
                materialDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_disable_retweets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Tweet was disabled", Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_mute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "User was muted", Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "User followed by you", Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        });
    }

    private void parseText(final String text, final TextView textView, final FeedModel feedModel) {
        final SpannableString spannableString = new SpannableString(text);
        String[] words  = text.split(" ");

        Pattern urlPattern = Pattern.compile(
                "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        for (final String word : words) {
            if (word.startsWith("#")) {
                WordSpan wordSpan = new WordSpan(0, App.getInstance().isNightEnabled() ?
                        R.color.dark_tag_color : R.color.light_tag_color,
                        word, mContext);

                spannableString.setSpan(wordSpan, text.indexOf(word), text.indexOf(word) + word.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (word.startsWith("@")) {
                WordSpan wordSpan = new WordSpan(0, App.getInstance().isNightEnabled() ?
                        R.color.dark_highlight_color : R.color.light_highlight_color,
                        word, mContext);

                spannableString.setSpan(wordSpan, text.indexOf(word), text.indexOf(word) + word.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (urlPattern.matcher(word).matches() || (word.contains(".") && word.contains("/"))) {
                WordSpan wordSpan = new WordSpan(0, App.getInstance().isNightEnabled() ?
                        R.color.dark_highlight_color : R.color.light_highlight_color,
                        word, mContext);

                spannableString.setSpan(wordSpan, text.indexOf(word), text.indexOf(word) + word.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        textView.setText(spannableString);
    }

    private void performShare(String text) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        mContext.startActivity(Intent.createChooser(sharingIntent, mContext.getString(R.string.app_name)));
    }

    private void performReply(FeedModel feedModel) {
        AppData.CURRENT_TWEET_MODEL = (feedModel);
//        AppData.CURRENT_COMPOSE = AppData.COMPOSE_REPLY;
        mActivity.startActivity(new Intent(mContext, ComposeActivity.class));
    }

    private void performMore(View v, final FeedModel feedModel) {
        PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_tweet_more, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.more_copy:
                        ClipboardManager clipboard = (ClipboardManager)
                                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(mContext.getString(R.string.app_name), feedModel.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, mContext.getString(R.string.successfully_copied), Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.more_delete:
                        Toast.makeText(mContext, "Perform to delete tweet", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.more_details:
                        mActivity.startActivity(new Intent(mContext, DetailActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case R.id.more_link:
                        performShare("http://google.com");
                        break;

                    case R.id.more_translate:
                        Toast.makeText(mContext, "Perform to google translate", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void performContent(FeedModel feedModel) {
        AppData.CURRENT_TWEET_MODEL = (feedModel);
        mActivity.startActivity(new Intent(mContext, ContentActivity.class));
        mActivity.overridePendingTransition(R.anim.fade_in, R.anim.slide_out_no_animation);
    }

    private void performRetweet(View v, final FeedModel feedModel) {
        PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_retweet, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tweet_retweet:
                        Toast.makeText(mContext, "Perform retweet", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.tweet_quote:
//                        AppData.CURRENT_COMPOSE = AppData.COMPOSE_QUOTE;
                        AppData.CURRENT_TWEET_MODEL = (feedModel);
                        mActivity.startActivity(new Intent(mContext, ComposeActivity.class));
                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

}

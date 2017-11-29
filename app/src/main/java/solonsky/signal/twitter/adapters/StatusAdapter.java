package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import solonsky.signal.twitter.activities.MVPProfileActivity;
import solonsky.signal.twitter.activities.MediaActivity;
import solonsky.signal.twitter.api.ActionsApiFactory;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.ShareData;
import solonsky.signal.twitter.dialogs.HashtagDialog;
import solonsky.signal.twitter.dialogs.MediaDialog;
import solonsky.signal.twitter.dialogs.ProfileDialog;
import solonsky.signal.twitter.dialogs.UrlDialog;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.helpers.Flags;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.common.net.MediaType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.databinding.CellStatusBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.DateConverter;
import solonsky.signal.twitter.helpers.ImageAnimation;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.helpers.Styling;
import solonsky.signal.twitter.helpers.TweetActions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.Animator;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.libs.ShareContent;
import solonsky.signal.twitter.libs.TargetChosenReceiver;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkTextView;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.ImageModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.overlays.ImageActionsOverlay;
import twitter4j.AsyncTwitter;

/**
 * Created by neura on 22.05.17.
 */

public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = StatusAdapter.class.getSimpleName();
    private final ArrayList<StatusModel> mStatusModels;
    private final Context mContext;
    private final AppCompatActivity mActivity;

    private final int expandDuration = 150;
    private final int alphaDuration = 150;
    private final int bottomSize = 48;
    private final StatusClickListener mStatusClickListener;
    private final boolean canRetweet;
    private final TweetActions.MoreCallback moreCallback;
    private boolean isLoading;

    private final int VIEW_CELL = 0;
    private final int VIEW_FOOTER = 1;

    private StatusModel previousExpand = null;
    private CellStatusBinding lastHolder = null;
    private final boolean canExpand;
    private final boolean isNight = App.getInstance().isNightEnabled();
    private final Animator animator;

    public interface StatusClickListener {
        void onSearch(String searchText, View v);
    }

    public StatusAdapter(ArrayList<StatusModel> statusModels, AppCompatActivity mActivity,
                         boolean canExpand, boolean canRetweet,
                         StatusClickListener statusClickListener, TweetActions.MoreCallback moreCallback) {
        this.mStatusModels = statusModels;
        this.mStatusClickListener = statusClickListener;
        this.mActivity = mActivity;
        this.canExpand = canExpand;
        this.canRetweet = canRetweet;
        this.isLoading = false;
        this.moreCallback = moreCallback;
        this.animator = new Animator(mActivity.getApplicationContext());
        this.mContext = mActivity.getApplicationContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_FOOTER) {
            View view = inflater.inflate(R.layout.cell_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            CellStatusBinding binding = CellStatusBinding.inflate(inflater, parent, false);

            StatusViewHolder statusViewHolder = new StatusViewHolder(binding.getRoot());
            int adapterPosition = statusViewHolder.getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Load image to avatar && preview
            }

            return statusViewHolder;
        }
    }

    private boolean isStartOpen = false;

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder genericHolder, int position) {
        /* Setup next, previous and current model */
        if (genericHolder instanceof FooterViewHolder) {

        } else {
            final StatusViewHolder holder = (StatusViewHolder) genericHolder;

            final StatusModel statusModel = mStatusModels.get(holder.getAdapterPosition());
            final StatusModel previousModel = holder.getAdapterPosition() == 0 ? null : mStatusModels.get(holder.getAdapterPosition() - 1);
            final StatusModel nextModel = holder.getAdapterPosition() == mStatusModels.size() - 1 ? null : mStatusModels.get(holder.getAdapterPosition() + 1);
            final boolean isNextHighlighted = nextModel != null && nextModel.isHighlighted();
            final boolean isRetweet = statusModel.getRetweetedStatus() != null;

            final DateConverter dateConverter = new DateConverter(mContext);
            String createdAt = AppData.appConfiguration.isRelativeDates() ?
                    dateConverter.parseTime(new LocalDateTime(statusModel.getCreatedAt())) :
                    dateConverter.parseAbsTime(new LocalDateTime(statusModel.getCreatedAt()));
            String tweetText = isRetweet ? statusModel.getRetweetedStatus().getText() : statusModel.getText();
            String username = isRetweet ? AppData.appConfiguration.isRealNames() ?
                    statusModel.getRetweetedStatus().getUser().getName() :
                    statusModel.getRetweetedStatus().getUser().getScreenName() :
                    AppData.appConfiguration.isRealNames() ?
                            statusModel.getUser().getName() :
                            statusModel.getUser().getScreenName();

            holder.mBinding.setModel(statusModel);
            holder.mBinding.statusBottomWrapper.setOnTouchListener(new View.OnTouchListener() {
                long thisTouchTime;
                long previousTouchTime = 0;
                long buttonHeldTime;
                float initialX = 0;
                float initialY = 0;
                boolean clickHandled = false;
                final float DELTA_THRESHOLD = 5;
                final long DOUBLE_CLICK_INTERVAL = 200;
                final long LONG_HOLD_TIMEOUT = 500;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isStartOpen = false;
                            initialX = event.getRawX();
                            initialY = event.getRawY();
                            thisTouchTime = System.currentTimeMillis();
                            if (thisTouchTime - previousTouchTime <= DOUBLE_CLICK_INTERVAL) {
                                // Double click detected
                                clickHandled = true;
                                performDoubleTap(statusModel);
                            } else {
                                // Defer event handling until later
                                clickHandled = false;
                            }
                            previousTouchTime = thisTouchTime;
                            break;

                        case MotionEvent.ACTION_UP:
                            if (!clickHandled) {
                                buttonHeldTime = System.currentTimeMillis() - thisTouchTime;
                                if (buttonHeldTime > LONG_HOLD_TIMEOUT) {
                                    clickHandled = true;
                                    if (!isStartOpen)
                                        performLongTap(statusModel);
                                } else {
                                    Handler clickHandler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (!clickHandled) {
                                                clickHandled = true;
                                                if (!isStartOpen)
                                                    performSingleTap(statusModel, previousModel, nextModel, holder);
                                            }
                                        }
                                    };
                                    Message m = new Message();
                                    clickHandler.sendMessageDelayed(m, DOUBLE_CLICK_INTERVAL);
                                }
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
//                        if (Math.abs(event.getRawX() - initialX) > DELTA_THRESHOLD ||
//                                Math.abs(event.getRawY() - initialY) > DELTA_THRESHOLD) {
//                            clickHandled = true;
//                        }
                            break;
                    }
                    return false;
                }
            });

        /* Load data to cell */
            holder.mBinding.statusImgAvatar.setVisibility(AppData.appConfiguration.isRoundAvatars() ?
                    View.GONE : View.VISIBLE);
            holder.mBinding.statusCivAvatar.setVisibility(AppData.appConfiguration.isRoundAvatars() ?
                    View.VISIBLE : View.GONE);

            applyStyle(holder);

            if (AppData.appConfiguration.isRoundAvatars()) {
                Picasso.with(mContext).load(isRetweet ?
                        statusModel.getRetweetedStatus().getUser().getOriginalProfileImageURL() :
                        statusModel.getUser().getOriginalProfileImageURL())
                        .into(holder.mBinding.statusCivAvatar);
            } else {
                Picasso.with(mContext).load(isRetweet ?
                        statusModel.getRetweetedStatus().getUser().getOriginalProfileImageURL() :
                        statusModel.getUser().getOriginalProfileImageURL())
                        .resize((int) Utilities.convertDpToPixel(40, mContext),
                                (int) Utilities.convertDpToPixel(40, mContext))
                        .centerCrop()
                        .transform(new CirclePicasso(
                                Utilities.convertDpToPixel(4, mContext),
//                                Utilities.convertDpToPixel(0.5f, mContext),
                                0, 0, R.color.black))
                        .into(holder.mBinding.statusImgAvatar);
            }

//        holder.mBinding.statusReplyTop.setVisibility(statusModel.isReplyStart() ? View.VISIBLE : View.INVISIBLE);
//        holder.mBinding.statusReplyBottom.setVisibility(statusModel.isReplyEnd() ? View.VISIBLE : View.INVISIBLE);

            if (statusModel.getMediaEntities().size() > 0) {
                if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_SMALL) {
                    holder.mBinding.statusRlMedia.setVisibility(View.VISIBLE);
                    holder.mBinding.statusRlPreviewBig.setVisibility(View.GONE);

                    String type = statusModel.getMediaEntities().get(0).getAsJsonObject().get("type").getAsString();
                    switch (type) {
                        case Flags.MEDIA_PHOTO:
                            holder.mBinding.statusImgSmallBadge.setVisibility(View.GONE);
                            break;
                        case Flags.MEDIA_GIF:
                            holder.mBinding.statusImgSmallBadge.setVisibility(View.VISIBLE);
                            holder.mBinding.statusImgSmallBadge.setImageDrawable(mContext.getDrawable(R.drawable.ic_badges_media_gif));
                            break;
                        case Flags.MEDIA_VIDEO:
                            holder.mBinding.statusImgSmallBadge.setVisibility(View.VISIBLE);
                            holder.mBinding.statusImgSmallBadge.setImageDrawable(mContext.getDrawable(R.drawable.ic_badges_media_video));
                            break;

                        case Flags.MEDIA_YOUTUBE:
                            holder.mBinding.statusImgSmallBadge.setVisibility(View.VISIBLE);
                            holder.mBinding.statusImgSmallBadge.setImageDrawable(mContext.getDrawable(R.drawable.ic_badges_media_youtube));
                            break;
                    }

                    Picasso.with(mContext).load(statusModel.getMediaEntities().get(0).getAsJsonObject()
                            .get("mediaURLHttps").getAsString())
                            .resize((int) Utilities.convertDpToPixel(64, mContext),
                                    (int) Utilities.convertDpToPixel(64, mContext))
                            .centerCrop()
                            .transform(new CirclePicasso(
                                    Utilities.convertDpToPixel(4, mContext),
                                    Utilities.convertDpToPixel(0.5f, mContext),
                                    25, R.color.black))
                            .into(holder.mBinding.statusImgPreview);
                } else if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_BIG) {
                    holder.mBinding.statusRlPreviewBig.setVisibility(View.VISIBLE);
                    holder.mBinding.statusRlMedia.setVisibility(View.GONE);

                    int width = (int) (Utilities.getScreenWidth(mActivity) - Utilities.convertDpToPixel(88, mContext));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mBinding.statusRecyclerBigMedia.getLayoutParams();
                    params.height = (int) (width * 0.617);
                    holder.mBinding.statusRecyclerBigMedia.setLayoutParams(params);

                    final ArrayList<ImageModel> imageModels = new ArrayList<>();
                    for (int i = 0; i < statusModel.getMediaEntities().size(); i++) {
                        ImageModel imageModel = new ImageModel(statusModel.getMediaEntities().get(i)
                                .getAsJsonObject().get("mediaURLHttps").getAsString());
                        String type = statusModel.getMediaEntities().get(i).getAsJsonObject().get("type").getAsString();
                        switch (type) {
                            case Flags.MEDIA_PHOTO:
                                imageModel.setMediaType(Flags.MEDIA_TYPE.IMAGE);
                                break;

                            case Flags.MEDIA_VIDEO:
                                imageModel.setMediaType(Flags.MEDIA_TYPE.VIDEO);
                                break;

                            case Flags.MEDIA_YOUTUBE:
                                imageModel.setMediaType(Flags.MEDIA_TYPE.YOUTUBE);
                                imageModel.setPreviewUrl(statusModel.getMediaEntities().get(i)
                                        .getAsJsonObject().get("expandedURL").getAsString());
                                imageModel.setImageUrl(statusModel.getMediaEntities().get(i)
                                        .getAsJsonObject().get("mediaURLHttps").getAsString());
                                break;

                            case Flags.MEDIA_GIF:
                                imageModel.setMediaType(Flags.MEDIA_TYPE.GIF);
                                break;
                        }

                        imageModels.add(imageModel);
                    }

                    ImageStaggeredAdapter imageStaggeredAdapter = new ImageStaggeredAdapter(imageModels,
                            mActivity, new ImageStaggeredAdapter.ImageStaggeredListener() {
                        @Override
                        public void onClick(ImageModel imageModel, View v) {
                            switch (imageModel.getMediaType()) {
                                case YOUTUBE:
                                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                            (imageModel.getPreviewUrl())));
                                    break;

                                default:
                                    performContent(statusModel, holder, imageModels.indexOf(imageModel));
                                    break;
                            }
                        }
                    }, width);

                    GridLayoutManager manager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
                    manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return (position == 0 ? (imageModels.size() % 2 == 0) ? 1 : 2 : 1);
                        }
                    });

//                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mActivity.getApplicationContext(), 2);
                    holder.mBinding.statusRecyclerBigMedia.setHasFixedSize(true);
                    holder.mBinding.statusRecyclerBigMedia.setNestedScrollingEnabled(false);
                    holder.mBinding.statusRecyclerBigMedia.setLayoutManager(manager);
                    holder.mBinding.statusRecyclerBigMedia.setAdapter(imageStaggeredAdapter);
                } else {
                    holder.mBinding.statusRlMedia.setVisibility(View.GONE);
                    holder.mBinding.statusRlPreviewBig.setVisibility(View.GONE);
                }
            } else {
                holder.mBinding.statusRlMedia.setVisibility(View.GONE);
                holder.mBinding.statusRlPreviewBig.setVisibility(View.GONE);
            }

            holder.mBinding.statusTxtText.addAutoLinkMode(
                    AutoLinkMode.MODE_HASHTAG,
                    AutoLinkMode.MODE_MENTION,
                    AutoLinkMode.MODE_URL,
                    AutoLinkMode.MODE_SHORT
            );

            String[] urls = new String[statusModel.getUrlEntities().size()];
            for (int i = 0; i < statusModel.getUrlEntities().size(); i++) {
                urls[i] = ((JsonObject) statusModel.getUrlEntities().get(0)).get("displayURL").getAsString();
            }

            holder.mBinding.statusTxtText.setShortUrls(urls);

            holder.mBinding.statusTxtMediaCount.setText(isRetweet ?
                    String.valueOf(statusModel.getRetweetedStatus().getMediaEntities().size()) :
                    String.valueOf(statusModel.getMediaEntities().size()));

            holder.mBinding.statusTxtText.setHashtagModeColor(ContextCompat.getColor(mContext, isNight ?
                    R.color.dark_tag_color : R.color.light_tag_color));
            holder.mBinding.statusTxtText.setMentionModeColor(ContextCompat.getColor(mContext, isNight ?
                    R.color.dark_highlight_color : R.color.light_highlight_color));
            holder.mBinding.statusTxtText.setUrlModeColor(ContextCompat.getColor(mContext, isNight ?
                    R.color.dark_highlight_color : R.color.light_highlight_color));
            holder.mBinding.statusTxtText.setSelectedStateColor(ContextCompat.getColor(mContext, isNight ?
                    R.color.dark_secondary_text_color : R.color.light_secondary_text_color));

            holder.mBinding.statusTxtText.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                @Override
                public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                    if (autoLinkMode.equals(AutoLinkMode.MODE_HASHTAG)) {
                        mStatusClickListener.onSearch(matchedText, holder.mBinding.statusTxtText);
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_MENTION)) {
                        Intent profileIntent = new Intent(mContext, MVPProfileActivity.class);
                        profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, matchedText);
                        mActivity.startActivity(profileIntent);
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_SHORT)) {
                        for (JsonElement jsonElement : statusModel.getUrlEntities()) {
                            if (jsonElement.getAsJsonObject().get("displayURL").getAsString().equals(matchedText)) {
                                Utilities.openLink(jsonElement.getAsJsonObject().get("expandedURL").getAsString(), mActivity);
                            }
                        }
                    } else {
                        performSingleTap(statusModel, previousModel, nextModel, holder);
                    }

                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                    if (autoLinkMode.equals(AutoLinkMode.MODE_HASHTAG)) {
                        new HashtagDialog(matchedText.replaceAll("\\s+", ""), mActivity).show();
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_MENTION)) {
                        new ProfileDialog(matchedText, mActivity).show();
                    } else if (autoLinkMode.equals(AutoLinkMode.MODE_SHORT)) {
                        for (JsonElement jsonElement : statusModel.getUrlEntities()) {
                            if (jsonElement.getAsJsonObject().get("displayURL").getAsString().equals(matchedText)) {
                                new UrlDialog(jsonElement.getAsJsonObject().get("expandedURL").getAsString(), mActivity).show();
                            }
                        }
                    }

                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }
            });

            holder.mBinding.statusTxtText.setRegularTextViewClick(new AutoLinkTextView.RegularTextViewClick() {
                @Override
                public void onTextClicked(View v) {
                    performSingleTap(statusModel, previousModel, nextModel, holder);
                }

                @Override
                public void onLongTextClicked(View v) {
                    if (!isStartOpen)
                        performLongTap(statusModel);
                }

                @Override
                public void onDoubleTapClicked(View v) {
                    performDoubleTap(statusModel);
                }
            });

            if (tweetText.equals("") && AppData.appConfiguration.getThumbnails() != ConfigurationModel.THUMB_SMALL) {
                holder.mBinding.statusLlText.setVisibility(View.GONE);
            } else {
                holder.mBinding.statusLlText.setVisibility(View.VISIBLE);
            }

            holder.mBinding.statusTxtText.setAutoLinkText(tweetText); // Load status text
            holder.mBinding.statusTxtUsername.setText(username); // Load username
            holder.mBinding.statusTxtCreatedAt.setText(createdAt); // Load status time
            holder.mBinding.statusLlBottom.setVisibility(statusModel.isExpand() ? View.VISIBLE : View.GONE);
            holder.mBinding.statusDivider.setBackgroundColor(mContext.getResources()
                    .getColor(isNextHighlighted ? isNight ? R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color
                            : statusModel.isHighlighted() ? isNight ? R.color.dark_divider_highlight_color : R.color.light_divider_highlight_color
                            : isNight ? R.color.dark_divider_color : R.color.light_divider_color));

        /* Setup quoted state */
            if (statusModel.getQuotedStatus() != null) {
                createdAt = AppData.appConfiguration.isRelativeDates() ?
                        dateConverter.parseTime(new LocalDateTime(statusModel.getQuotedStatus().getCreatedAt())) :
                        dateConverter.parseAbsTime(new LocalDateTime(statusModel.getQuotedStatus().getCreatedAt()));

                holder.mBinding.statusLlQuote.setVisibility(View.VISIBLE);
                holder.mBinding.statusTxtQuoteText.setText(statusModel.getQuotedStatus().getText());
                holder.mBinding.statusTxtQuoteUsername.setText(
                        AppData.appConfiguration.isRealNames() ?
                                statusModel.getQuotedStatus().getUser().getName() :
                                statusModel.getQuotedStatus().getUser().getScreenName());
                holder.mBinding.statusTxtQuoteCreatedAt.setText(createdAt);

                if (statusModel.getQuotedStatus().getMediaEntities().size() > 0) {
                    if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_BIG
                            || AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_SMALL) {

                        String type = statusModel.getQuotedStatus().getMediaEntities().get(0)
                                .getAsJsonObject().get("type").getAsString();

                        switch (type) {
                            case Flags.MEDIA_PHOTO:
                                holder.mBinding.statusImgQuoteBadge.setVisibility(View.GONE);
                                break;
                            case Flags.MEDIA_GIF:
                                holder.mBinding.statusImgQuoteBadge.setVisibility(View.VISIBLE);
                                holder.mBinding.statusImgQuoteBadge.setImageDrawable(mContext.getDrawable(R.drawable.ic_badges_media_gif));
                                break;
                            case Flags.MEDIA_VIDEO:
                                holder.mBinding.statusImgQuoteBadge.setVisibility(View.VISIBLE);
                                holder.mBinding.statusImgQuoteBadge.setImageDrawable(mContext.getDrawable(R.drawable.ic_badges_media_video));
                                break;
                        }

                        holder.mBinding.statusFlQuote.setVisibility(View.VISIBLE);
                        Picasso.with(mContext).load(statusModel.getQuotedStatus().getMediaEntities().get(0).getAsJsonObject()
                                .get("mediaURLHttps").getAsString())
                                .resize((int) Utilities.convertDpToPixel(64, mContext),
                                        (int) Utilities.convertDpToPixel(64, mContext))
                                .transform(new CirclePicasso(
                                        Utilities.convertDpToPixel(4, mContext),
                                        Utilities.convertDpToPixel(0.5f, mContext),
                                        25, R.color.black))
                                .centerCrop().into(holder.mBinding.statusImgQuote);
                    } else {
                        holder.mBinding.statusFlQuote.setVisibility(View.GONE);
                    }

                    if (statusModel.getQuotedStatus().getMediaEntities().size() > 1) {
                        holder.mBinding.statusQuoteImgMediaCount.setVisibility(View.VISIBLE);
                        holder.mBinding.statusQuoteTxtMediaCount.setVisibility(View.VISIBLE);
                        holder.mBinding.statusQuoteTxtMediaCount.setText(
                                String.valueOf(statusModel.getQuotedStatus().getMediaEntities().size()));
                    } else {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mBinding.statusRlQuoteImage.getLayoutParams();
                        params.rightMargin = (int) Utilities.convertDpToPixel(12, mContext);
                        holder.mBinding.statusRlQuoteImage.setLayoutParams(params);

                        holder.mBinding.statusQuoteImgMediaCount.setVisibility(View.GONE);
                        holder.mBinding.statusQuoteTxtMediaCount.setVisibility(View.GONE);
                    }
                } else {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.mBinding.statusRlQuoteImage.getLayoutParams();
                    params.rightMargin = (int) Utilities.convertDpToPixel(12, mContext);
                    holder.mBinding.statusRlQuoteImage.setLayoutParams(params);

                    holder.mBinding.statusQuoteImgMediaCount.setVisibility(View.GONE);
                    holder.mBinding.statusQuoteTxtMediaCount.setVisibility(View.GONE);
                }
            } else {
                holder.mBinding.statusLlQuote.setVisibility(View.GONE);
                holder.mBinding.statusFlQuote.setVisibility(View.GONE);
            }

        /* Setup retweet state */
            if (isRetweet && canRetweet) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mBinding.statusLlRetweet.getLayoutParams();
//            params.topMargin = holder.mBinding.statusTxtText.getText().toString().equals("") ?
//                    (int) -Utilities.convertDpToPixel(16, mContext) :
//                    (int) Utilities.convertDpToPixel(9, mContext);
                holder.mBinding.statusLlRetweet.setVisibility(View.VISIBLE);
                holder.mBinding.statusTxtRetweet.addAutoLinkMode(AutoLinkMode.MODE_CUSTOM);
                holder.mBinding.statusTxtRetweet.setCustomModeColor(mContext.getResources().getColor(isNight ?
                        R.color.dark_secondary_text_color : R.color.light_secondary_text_color));
                holder.mBinding.statusTxtRetweet.setCustomRegex("^[\\p{L} .'-]+$");
                holder.mBinding.statusTxtRetweet.setAutoLinkText(statusModel.getUser().getId() == AppData.ME.getId() ?
                        mContext.getString(R.string.you) : AppData.appConfiguration.isRealNames() ?
                        statusModel.getUser().getName() : statusModel.getUser().getScreenName());
                holder.mBinding.statusTxtRetweet.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                    @Override
                    public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                        Intent profileIntent = new Intent(mContext, MVPProfileActivity.class);
                        if (matchedText.toLowerCase().equals("You".toLowerCase())) {
                            profileIntent.putExtra(Flags.PROFILE_DATA, AppData.ME);
                        } else {
                            profileIntent.putExtra(Flags.PROFILE_DATA, statusModel.getUser());
                        }

                        mActivity.startActivity(profileIntent);
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                        if (matchedText.toLowerCase().equals("you".toLowerCase())) {
                            new ProfileDialog(AppData.ME, mActivity).show();
                        } else {
                            new ProfileDialog(statusModel.getUser(), mActivity).show();
                        }
                    }
                });
            } else {
                holder.mBinding.statusLlRetweet.setVisibility(View.GONE);
            }

        /* Setup cell handlers */
            setupSwipe(statusModel, holder.mBinding.statusBottomWrapper, holder.mBinding.statusImgOpen);
            holder.mBinding.setClick(new StatusModel.StatusClickHandler() {
                @Override
                public void onShareClick(View v) {
                    TweetActions.share(statusModel, mActivity);
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onLikeClick(View v) {
                    statusModel.setFavorited(!statusModel.isFavorited());
                    TweetActions.favorite(statusModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                        @Override
                        public void onException(String error) {
                            statusModel.setFavorited(!statusModel.isFavorited());
                        }
                    });
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onRetweetClick(View v) {
                    TweetActions.retweetPopup(mActivity, v, statusModel.isRetweet() || statusModel.getRetweetedStatus() != null ?
                            statusModel.getRetweetedStatus() : statusModel, new TweetActions.ActionCallback() {
                        @Override
                        public void onException(String error) {
                            Toast.makeText(mContext, "Error while loading data - " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onReplyClick(View v) {
                    TweetActions.reply(statusModel.isRetweet() || statusModel.getRetweetedStatus() != null ?
                            statusModel.getRetweetedStatus() : statusModel, mActivity);
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onMoreClick(View v) {
                    TweetActions.morePopup(mActivity, v, statusModel, moreCallback);
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onContentClick(View v) {
                    performContent(statusModel, holder, 0);
                    if (mActivity instanceof LoggedActivity)
                        ((LoggedActivity) mActivity).hidePopup();
                }

                @Override
                public void onProfileClick(View v) {
//                    AppData.CURRENT_USER = statusModel.isRetweet() && statusModel.getRetweetedStatus() != null ?
//                            statusModel.getRetweetedStatus().getUser() : statusModel.getUser();
//                    Flags.userDirection = Flags.Directions.FROM_RIGHT;
//                    Flags.userSource = Flags.UserSource.data;
//                    Flags.homeUser = AppData.CURRENT_USER.getId() == AppData.ME.getId();
//                    mActivity.startActivity(new Intent(mContext, ProfileActivity.class));
//                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//                    if (mActivity instanceof LoggedActivity)
//                        ((LoggedActivity) mActivity).hidePopup();

                    User user = statusModel.isRetweet() && statusModel.getRetweetedStatus() != null ?
                            statusModel.getRetweetedStatus().getUser() : statusModel.getUser();
                    Intent profileIntent = new Intent(mContext, MVPProfileActivity.class);
                    profileIntent.putExtra(Flags.PROFILE_DATA, user);
                    mActivity.startActivity(profileIntent);
                }

                @Override
                public void onQuoteClick(View v) {
                    if (statusModel.getQuotedStatus() != null) {
                        AppData.CURRENT_STATUS_MODEL = statusModel.getQuotedStatus();
                        mActivity.startActivity(new Intent(mContext, DetailActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }

                @Override
                public void onQuoteUserClick(View v) {
                    if (statusModel.getQuotedStatus() != null) {
                        Intent profileIntent = new Intent(mContext, MVPProfileActivity.class);
                        profileIntent.putExtra(Flags.PROFILE_DATA, statusModel.getQuotedStatus().getUser());
                        mActivity.startActivity(profileIntent);
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        if (mActivity instanceof LoggedActivity)
                            ((LoggedActivity) mActivity).hidePopup();
                    }
                }

                @Override
                public void onQuoteContentClick(View v) {
                    performContent(statusModel.getQuotedStatus(), holder, 0);
                }

                @Override
                public boolean longUserClick(View v) {
                    new ProfileDialog(statusModel.isRetweet() ?
                            statusModel.getRetweetedStatus().getUser() : statusModel.getUser(), mActivity).show();
                    return true;
                }

                @Override
                public boolean longContentClick(View v) {
                    if (statusModel.isRetweet() && statusModel.getRetweetedStatus() != null) {
                        StatusModel retweeted = statusModel.getRetweetedStatus();
                        if (retweeted.getMediaEntities().size() > 0) {
                            if (retweeted.getMediaEntities().get(0).getAsJsonObject().get("type").getAsString().equals("photo")) {
                                new MediaDialog(retweeted.getMediaEntities().get(0).getAsJsonObject().get("mediaURL").getAsString(),
                                        mActivity, false).show();
                            } else {
                                if (retweeted.getMediaEntities().get(0)
                                        .getAsJsonObject().get("videoVariants").getAsJsonArray().size() > 0) {
                                    new MediaDialog(retweeted.getMediaEntities().get(0)
                                            .getAsJsonObject().get("videoVariants").getAsJsonArray()
                                            .get(0).getAsJsonObject().get("url").getAsString(), mActivity, true).show();
                                }
                            }
                        }
                    } else {
                        if (statusModel.getMediaEntities().size() > 0) {
                            if (statusModel.getMediaEntities().get(0).getAsJsonObject().get("type").getAsString().equals("photo")) {
                                new MediaDialog(statusModel.getMediaEntities().get(0).getAsJsonObject().get("mediaURL").getAsString(),
                                        mActivity, false).show();
                            } else {
                                if (statusModel.getMediaEntities().get(0)
                                        .getAsJsonObject().get("videoVariants").getAsJsonArray().size() > 0) {
                                    new MediaDialog(statusModel.getMediaEntities().get(0)
                                            .getAsJsonObject().get("videoVariants").getAsJsonArray()
                                            .get(0).getAsJsonObject().get("url").getAsString(), mActivity, true).show();
                                }
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean longQuoteContentClick(View v) {
                    if (statusModel.getQuotedStatus().getMediaEntities().size() > 0) {
                        if (statusModel.getQuotedStatus().getMediaEntities().get(0).getAsJsonObject().get("type").getAsString().equals("photo")) {
                            new MediaDialog(statusModel.getQuotedStatus().getMediaEntities().get(0).getAsJsonObject().get("mediaURL").getAsString(),
                                    mActivity, false).show();
                        } else {
                            if (statusModel.getQuotedStatus().getMediaEntities().get(0)
                                    .getAsJsonObject().get("videoVariants").getAsJsonArray().size() > 0) {
                                new MediaDialog(statusModel.getQuotedStatus().getMediaEntities().get(0)
                                        .getAsJsonObject().get("videoVariants").getAsJsonArray()
                                        .get(0).getAsJsonObject().get("url").getAsString(), mActivity, true).show();
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mStatusModels == null) {
            return 0;
        }

        if (mStatusModels.size() == 0 && isLoading) {
            return 1;
        }

        return isLoading ? mStatusModels.size() + 1 : mStatusModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mStatusModels.size() && isLoading) {
            return VIEW_FOOTER;
        }

        return VIEW_CELL;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        private AVLoadingIndicatorView mCpvFooter;

        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        private final CellStatusBinding mBinding;
        private final boolean isNight = App.getInstance().isNightEnabled();
        private String TAG = StatusViewHolder.class.getSimpleName();

        StatusViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);

            /* Set borders and colors to avatar */
            mBinding.statusCivAvatar.setBorderOverlay(!isNight);
            mBinding.statusCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            mBinding.statusCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, itemView.getContext()));

            mBinding.statusRecyclerBigMedia.addItemDecoration(
                    new ListConfig.SpacesItemDecoration((int) Utilities.convertDpToPixel(2,
                            itemView.getContext())));
        }

        public CellStatusBinding getmBinding() {
            return mBinding;
        }
    }

    private void setupSwipe(final StatusModel statusModel, SwipeLayout v, final ImageView imageView) {
        v.setTag(statusModel.getId());
        v.addSwipeListener(new SwipeLayout.SwipeListener() {
            boolean isOpen = false;
            int accelerate = 0;
            int oldoffset = 0;

            @Override
            public void onStartOpen(final SwipeLayout layout) {
                isStartOpen = true;
            }

            @Override
            public void onOpen(final SwipeLayout layout) {
                if (!isOpen) {
                    isOpen = true;
                    AppData.CURRENT_STATUS_MODEL = statusModel.isRetweet() ? statusModel.getRetweetedStatus() : statusModel;
                    mActivity.startActivity(new Intent(mContext, DetailActivity.class));
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout.close();
                            isOpen = false;
                        }
                    }, 800);
                }
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(final SwipeLayout layout, int leftOffset, int topOffset) {
                int arrowWidth = (int) (imageView.getWidth() + Utilities.convertDpToPixel(16, mContext));
                if (Math.abs(leftOffset) < arrowWidth) {
                    imageView.animate().setDuration(0).translationX((float) (-leftOffset / 2)
                            - Utilities.convertDpToPixel(0, mContext)).start();
                } else {
                    if (leftOffset < oldoffset && accelerate <= Utilities.convertDpToPixel(16, mContext)) {
                        accelerate += Utilities.convertDpToPixel(0.4f, mContext);
                    } else if (leftOffset > oldoffset && accelerate >= 0) {
                        accelerate -= Utilities.convertDpToPixel(0.4f, mContext);
                    }
                    imageView.animate().setDuration(0).translationX((float) (-leftOffset / 2)
                            - accelerate).start();
                }

                oldoffset = leftOffset;
//                imageView.setAlpha((float) -leftOffset / 750f);
                int diff = (0 - leftOffset) / 10;
                ImageAnimation.setupStatusArrow(imageView, diff);
            }

            @Override
            public void onHandRelease(final SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    private void changeActions(StatusModel statusModel, StatusModel previousModel, StatusModel nextModel,
                               StatusViewHolder holder) {
        if (canExpand) {
            boolean isNight = App.getInstance().isNightEnabled();

            if (previousExpand != null && previousExpand != statusModel && previousExpand.isExpand()) {
                previousExpand.setExpand(false);
                if (lastHolder != null) {
                    lastHolder.statusLlBottom.animate().alpha(0).setDuration(alphaDuration).start();
                    animator.changeHeight((int) Utilities.convertDpToPixel(bottomSize, mContext), 0,
                            expandDuration, lastHolder.statusLlBottom);
                }
            }

            statusModel.setExpand(!statusModel.isExpand());

            if (statusModel.isExpand()) {
                holder.mBinding.statusLlBottom.setVisibility(View.VISIBLE);
                animator.changeHeight(0, (int) Utilities.convertDpToPixel(bottomSize, mContext),
                        expandDuration, holder.mBinding.statusLlBottom);
                holder.mBinding.statusLlBottom.animate().alpha(1).setDuration(alphaDuration).start();
                lastHolder = holder.mBinding;
            } else {
                holder.mBinding.statusLlBottom.animate().alpha(0).setDuration(alphaDuration).start();
                animator.changeHeight((int) Utilities.convertDpToPixel(bottomSize, mContext), 0,
                        expandDuration, holder.mBinding.statusLlBottom);

                if (previousExpand != null && previousExpand.getId() == statusModel.getId())
                    lastHolder = null;
            }

            previousExpand = statusModel;

            statusModel.setDivideState(statusModel.isExpand() ?
                    nextModel == null ? Flags.DIVIDER_LONG :
                            (nextModel.isHighlighted() && statusModel.isHighlighted()) ?
                                    Flags.DIVIDER_SHORT : isNight ? Flags.DIVIDER_NONE : Flags.DIVIDER_LONG
                    : nextModel == null ? Flags.DIVIDER_LONG :
                    (nextModel.isHighlighted() && statusModel.isHighlighted()) ||
                            (!nextModel.isHighlighted() && !statusModel.isHighlighted()) ? Flags.DIVIDER_SHORT :
                            isNight ? Flags.DIVIDER_NONE : Flags.DIVIDER_LONG);

            if (previousModel != null) {
                previousModel.setDivideState(statusModel.isExpand() ?
                        (previousModel.isHighlighted() && statusModel.isHighlighted()) ? Flags.DIVIDER_SHORT :
                                App.getInstance().isNightEnabled() ? Flags.DIVIDER_NONE : Flags.DIVIDER_LONG
                        :
                        (statusModel.isHighlighted() && previousModel.isHighlighted())
                                || (!statusModel.isHighlighted() && !previousModel.isHighlighted()) ?
                                Flags.DIVIDER_SHORT : isNight ? Flags.DIVIDER_NONE : Flags.DIVIDER_LONG);
            }
        }
    }

    private void performSingleTap(StatusModel statusModel, StatusModel previousModel, StatusModel nextModel,
                                  StatusViewHolder holder) {
        if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
        switch (AppData.appConfiguration.getShortTap()) {
            case ConfigurationModel.TAP_SHOW_ACTIONS:
                changeActions(statusModel, previousModel, nextModel, holder);
                break;

            case ConfigurationModel.TAP_VIEW_DETAILS:
                AppData.CURRENT_STATUS_MODEL = statusModel.isRetweet() ? statusModel.getRetweetedStatus() : statusModel;
                mActivity.startActivity(new Intent(mContext, DetailActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case ConfigurationModel.TAP_OPEN_MEDIA:
                if (statusModel.getMediaEntities().size() > 0) {
                    holder.mBinding.getClick().onContentClick(holder.mBinding.statusLlMain);
                } else {
                    changeActions(statusModel, previousModel, nextModel, holder);
                }
                break;

            case ConfigurationModel.TAP_GO_TO_LINK:
                if (statusModel.getUrlEntities().size() > 0) {
                    Utilities.openLink(statusModel.getUrlEntities().get(0).getAsJsonObject()
                            .get("expandedURL").getAsString(), mActivity);
                } else {
                    changeActions(statusModel, previousModel, nextModel, holder);
                }
                break;
        }
    }

    private void performLongTap(StatusModel statusModel) {
        if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
        switch (AppData.appConfiguration.getLongTap()) {
            case ConfigurationModel.TAP_LAST_SHARING:
                if (!ShareData.getInstance().isCacheLoaded()) {
                    ShareData.getInstance().loadCache();
                }

                ShareContent shareContent = new ShareContent(mActivity);

                if (ShareData.getInstance().getShares().size() > 0) {
                    String packageName = ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", "");
                    String packageActivity = ShareData.getInstance().getShares().get(0).split("/")[1].replace("}", "");
                    shareContent.shareTextWithApp(statusModel.getText(), packageName, packageActivity);
                } else {
                    shareContent.shareText(statusModel.getText(), "", new TargetChosenReceiver.IntentCallback() {
                        @Override
                        public void getComponentName(String componentName) {
                            ShareData.getInstance().addShare(componentName);
                            ShareData.getInstance().saveCache();
                        }
                    });
                }
                break;

            case ConfigurationModel.TAP_READ_LATER:
                Toast.makeText(mContext, "Read later", Toast.LENGTH_SHORT).show();
                break;

            case ConfigurationModel.TAP_TRANSLATE:
                ActionsApiFactory.translate(statusModel.getText(), mActivity);
                break;

            case ConfigurationModel.TAP_SHARE:
                if (mActivity instanceof LoggedActivity) {
                    ((LoggedActivity) mActivity).shareContent.shareText("Text", "Url", new TargetChosenReceiver.IntentCallback() {
                        @Override
                        public void getComponentName(String componentName) {
                            ShareData.getInstance().addShare(componentName);
                            ShareData.getInstance().saveCache();
                        }
                    });
                }
                break;

        }
    }

    private void performDoubleTap(StatusModel statusModel) {
        if (mActivity instanceof LoggedActivity) ((LoggedActivity) mActivity).hidePopup();
        switch (AppData.appConfiguration.getDoubleTap()) {
            case ConfigurationModel.TAP_REPLY:
                TweetActions.reply(statusModel.getRetweetedStatus() != null ?
                        statusModel.getRetweetedStatus() : statusModel, mActivity);
                break;

            case ConfigurationModel.TAP_QUOTE:
                TweetActions.quote(mActivity, statusModel);
                break;

            case ConfigurationModel.TAP_RETWEET:
                TweetActions.retweet(statusModel, mContext, new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {

                    }
                });
                break;

            case ConfigurationModel.TAP_LIKE:
                statusModel.setFavorited(!statusModel.isFavorited());
                TweetActions.favorite(statusModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                    @Override
                    public void onException(String error) {

                    }
                });
                break;
        }
    }

    /**
     * Applies style to cell
     */
    private void applyStyle(final StatusViewHolder holder) {
        Styling styling = new Styling(mContext, Styling.convertFontToStyle(AppData.appConfiguration.getFontSize()));
        holder.mBinding.statusTxtText.setLineSpacing(styling.getTextExtra(), 1);
        holder.mBinding.statusTxtText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        holder.mBinding.statusTxtQuoteUsername.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getQuoteTextSize());
        holder.mBinding.statusTxtQuoteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getQuoteTextSize());
        holder.mBinding.statusTxtQuoteText.setLineSpacing(styling.getQuoteTextExtra(), 1);
        holder.mBinding.statusTxtUsername.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        holder.mBinding.statusTxtRetweet.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getRtTextSize());
        holder.mBinding.statusTxtCreatedAt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getCreatedAtSize());
        holder.mBinding.statusTxtQuoteCreatedAt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getCreatedAtSize());

        /* Set RT margin Top */
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mBinding.statusLlRetweet.getLayoutParams();
        params.topMargin = styling.getRtMarginTop();
        holder.mBinding.statusLlRetweet.setLayoutParams(params);

        /* Set Text margin Top */
        params = (LinearLayout.LayoutParams) holder.mBinding.statusLlText.getLayoutParams();
        params.topMargin = styling.getTextMarginTop();
        holder.mBinding.statusLlText.setLayoutParams(params);

        /* Set base margin elements */
        holder.mBinding.statusLlBody.setPadding(0, 0, 0, styling.getBaseMargin());

        // Square avatar
        LinearLayout.LayoutParams baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusImgAvatar.getLayoutParams();
//        baseParams.leftMargin = styling.getBaseMargin();
//        baseParams.bottomMargin = styling.getBaseMargin();
        baseParams.topMargin = styling.getSquareAvatarMarginTop();
        holder.mBinding.statusImgAvatar.setLayoutParams(baseParams);

        // Round avatar
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusCivAvatar.getLayoutParams();
//        baseParams.leftMargin = styling.getBaseMargin();
        baseParams.topMargin = styling.getBaseMargin();
//        baseParams.bottomMargin = styling.getBaseMargin();
        holder.mBinding.statusCivAvatar.setLayoutParams(baseParams);

        // Container for username, text and media
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusLlContainer.getLayoutParams();
//        baseParams.leftMargin = styling.getBaseMargin();
        baseParams.topMargin = styling.getBaseMargin();
        holder.mBinding.statusLlContainer.setLayoutParams(baseParams);

        // Username
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusLlUsername.getLayoutParams();
//        baseParams.rightMargin = styling.getBaseMargin();
        holder.mBinding.statusLlUsername.setLayoutParams(baseParams);

        // Small preview
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusRlMedia.getLayoutParams();
//        baseParams.leftMargin = -(styling.getBaseMargin() / 2);
//        baseParams.rightMargin = styling.getBaseMargin() / 4;
        baseParams.topMargin = styling.getSmallPreviewMarginTop();
        holder.mBinding.statusRlMedia.setLayoutParams(baseParams);

        // Quote margin top
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusLlQuote.getLayoutParams();
        baseParams.topMargin = styling.getQuoteMarginTop();
        holder.mBinding.statusLlQuote.setLayoutParams(baseParams);

        // Big preview
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusRlPreviewBig.getLayoutParams();
        baseParams.topMargin = styling.getBigImageMarginTop();
        holder.mBinding.statusRlPreviewBig.setLayoutParams(baseParams);

        // Quote text
        baseParams = (LinearLayout.LayoutParams) holder.mBinding.statusLlQuoteBody.getLayoutParams();
        baseParams.topMargin = styling.getQuoteTextMarginTop();
        holder.mBinding.statusLlQuoteBody.setLayoutParams(baseParams);
    }

    /**
     * Shows Media on Full Screen
     *
     * @param statusModel - model with data
     * @param holder      - cell holder
     */
    private void performContent(final StatusModel statusModel, final StatusViewHolder holder, int startPosition) {
        if (statusModel.getMediaEntities().size() > 0) {
            String type = statusModel.getMediaEntities().get(0).getAsJsonObject().get("type").getAsString();
            if (type.equals(Flags.MEDIA_GIF)) {
                AppData.MEDIA_URL = statusModel.getMediaEntities().get(0).getAsJsonObject()
                        .get("videoVariants").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.GIF;
                mActivity.startActivity(new Intent(mContext, MediaActivity.class));
            } else if (type.equals(Flags.MEDIA_VIDEO)) {
                AppData.MEDIA_URL = statusModel.getMediaEntities().get(0).getAsJsonObject()
                        .get("videoVariants").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.VIDEO;
                mActivity.startActivity(new Intent(mContext, MediaActivity.class));
            } else if (type.equals(Flags.MEDIA_PHOTO)) {
                final ArrayList<String> urls = new ArrayList<>();

                for (JsonElement media : statusModel.getMediaEntities()) {
                    JsonObject mediaEntity = (JsonObject) media;
                    urls.add(mediaEntity.get("mediaURLHttps").getAsString());
                }

                final ImageActionsOverlay imageActionsOverlay = new ImageActionsOverlay(mActivity, urls, statusModel, startPosition);
                imageActionsOverlay.setImageActionsOverlayClickHandler(new ImageActionsOverlay.ImageActionsOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageActionsOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                        downloadFiles.saveFile(url, mActivity.getString(R.string.download_url));
                    }

                    @Override
                    public void onShareImageClick(View v, String url) {

                    }

                    @Override
                    public void onReplyClick(View v) {
                        holder.mBinding.getClick().onReplyClick(v);
                        imageActionsOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onRtClick(View v) {
                        TweetActions.morePopup(mActivity, v, statusModel, new TweetActions.MoreCallback() {
                            @Override
                            public void onDelete(StatusModel statusModel) {

                            }
                        });
                    }

                    @Override
                    public void onLikeClick(View v) {
                        statusModel.setFavorited(!statusModel.isFavorited());
                        imageActionsOverlay.setFavorited(statusModel.isFavorited());
                        imageActionsOverlay.changeFavorited();
                        TweetActions.favorite(statusModel.isFavorited(), statusModel.getId(), new TweetActions.ActionCallback() {
                            @Override
                            public void onException(String error) {
                                statusModel.setFavorited(!statusModel.isFavorited());
                                imageActionsOverlay.setFavorited(statusModel.isFavorited());
                                imageActionsOverlay.changeFavorited();
                            }
                        });
                    }

                    @Override
                    public void onShareClick(View v) {
                        holder.mBinding.getClick().onShareClick(v);
                    }

                    @Override
                    public void onMoreClick(View v) {
                        holder.mBinding.getClick().onMoreClick(v);
                    }
                });
            } else if (type.equals(Flags.MEDIA_YOUTUBE)) {
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        (statusModel.getMediaEntities().get(0).getAsJsonObject().get("url").getAsString())));
//                AppData.MEDIA_URL = statusModel.getMediaEntities().get(0).getAsJsonObject()
//                        .get("url").getAsString();
//                AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.YOUTUBE;
//                mActivity.startActivity(new Intent(mContext, YoutubeActivity.class));
            }
        }
    }
}

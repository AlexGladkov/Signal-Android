package solonsky.signal.twitter.libs.autoLinkTextView;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import solonsky.signal.twitter.helpers.App;

/**
 * Created by Chatikyan on 25.09.2016-18:53.
 */

public class AutoLinkTextView extends TextView {

    public interface RegularTextViewClick {
        void onTextClicked(View v);
        void onLongTextClicked(View v);
        void onDoubleTapClicked(View v);
    }

    static final String TAG = AutoLinkTextView.class.getSimpleName();

    private static final int MIN_PHONE_NUMBER_LENGTH = 8;

    private static final int DEFAULT_COLOR = App.getInstance().isNightEnabled() ?
            Color.parseColor("#96BEFF") : Color.parseColor("#4C88FF");

    private AutoLinkOnClickListener autoLinkOnClickListener;
    private RegularTextViewClick regularTextViewClick;

    private AutoLinkMode[] autoLinkModes;
    private String[] shortUrls;

    private String customRegex;

    private boolean isUnderLineEnabled = false;

    private int mentionModeColor = DEFAULT_COLOR;
    private int hashtagModeColor = DEFAULT_COLOR;
    private int urlModeColor = DEFAULT_COLOR;
    private int phoneModeColor = DEFAULT_COLOR;
    private int emailModeColor = DEFAULT_COLOR;
    private int customModeColor = DEFAULT_COLOR;
    private int defaultSelectedColor = Color.LTGRAY;

    public AutoLinkTextView(Context context) {
        super(context);
    }
    public AutoLinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setHighlightColor(int color) {
        super.setHighlightColor(App.getInstance().isNightEnabled() ?
                Color.parseColor("#1A96BEFF") : Color.parseColor("#1A4C88FF"));
    }

    public void setAutoLinkText(String text) {
        SpannableString spannableString = makeSpannableString(text);
        setText(spannableString);
        setMovementMethod(new LinkTouchMovementMethod(regularTextViewClick));
    }

    private SpannableString makeSpannableString(String text) {

        final SpannableString spannableString = new SpannableString(text);

        List<AutoLinkItem> autoLinkItems = matchedRanges(text);

        for (final AutoLinkItem autoLinkItem : autoLinkItems) {
            int currentColor = getColorByMode(autoLinkItem.getAutoLinkMode());
            int bgColor = getBackgroundColorByMode(autoLinkItem.getAutoLinkMode());

            TouchableSpan clickableSpan = new TouchableSpan(currentColor, defaultSelectedColor, isUnderLineEnabled,
                    bgColor) {
                @Override
                public void onLongClick(View widget) {
                    if (autoLinkOnClickListener != null) {
                        autoLinkOnClickListener.onAutoLinkLongTextClick(
                                autoLinkItem.getAutoLinkMode(),
                                autoLinkItem.getMatchedText());
                    }
                }

                @Override
                public void onClick(View widget) {
                    if (autoLinkOnClickListener != null)
                        autoLinkOnClickListener.onAutoLinkTextClick(
                                autoLinkItem.getAutoLinkMode(),
                                autoLinkItem.getMatchedText());
                }
            };

            spannableString.setSpan(
                    clickableSpan,
                    autoLinkItem.getStartPoint(),
                    autoLinkItem.getEndPoint(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    private List<AutoLinkItem> matchedRanges(String text) {

        List<AutoLinkItem> autoLinkItems = new LinkedList<>();

        if (autoLinkModes == null) {
            throw new NullPointerException("Please add at least one mode");
        }

        for (AutoLinkMode anAutoLinkMode : autoLinkModes) {
            String regex = Utils.getRegexByAutoLinkMode(anAutoLinkMode, customRegex);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

//            if (anAutoLinkMode.equals(AutoLinkMode.MODE_CUSTOM)) {
//                Log.e(TAG, "regex - " + regex);
//            }

            if (anAutoLinkMode == AutoLinkMode.MODE_PHONE) {
                while (matcher.find()) {
                    if (matcher.group().length() > MIN_PHONE_NUMBER_LENGTH)
                        autoLinkItems.add(new AutoLinkItem(
                                matcher.start(),
                                matcher.end(),
                                matcher.group(),
                                anAutoLinkMode));
                }
            } else if (anAutoLinkMode == AutoLinkMode.MODE_SHORT) {
                if (shortUrls != null) {
                    for (String match : shortUrls) {
                        pattern = Pattern.compile(match);
                        matcher = pattern.matcher(text);
                        while (matcher.find()) {
                            autoLinkItems.add(new AutoLinkItem(
                                    matcher.start(),
                                    matcher.end(),
                                    matcher.group(),
                                    anAutoLinkMode));
                        }
                    }
                }
            } else {
                while (matcher.find()) {
                    autoLinkItems.add(new AutoLinkItem(
                            matcher.start(),
                            matcher.end(),
                            matcher.group(),
                            anAutoLinkMode));
                }
            }
        }

        return autoLinkItems;
    }

    private int getBackgroundColorByMode(AutoLinkMode autoLinkMode) {
        switch (autoLinkMode) {
            case MODE_MENTION:
            case MODE_URL:
            case MODE_SHORT:
                return Color.parseColor(App.getInstance().isNightEnabled() ?
                       "#3396BEFF" : "#336791E6");

            default:
                return Color.parseColor("#337A8A99");
        }
    }

    private int getColorByMode(AutoLinkMode autoLinkMode) {
        switch (autoLinkMode) {
            case MODE_HASHTAG:
                return hashtagModeColor;
            case MODE_MENTION:
                return mentionModeColor;
            case MODE_URL:
                return urlModeColor;
            case MODE_SHORT:
                return urlModeColor;
            case MODE_PHONE:
                return phoneModeColor;
            case MODE_EMAIL:
                return emailModeColor;
            case MODE_CUSTOM:
                return customModeColor;
            default:
                return DEFAULT_COLOR;
        }
    }

    public void setMentionModeColor(@ColorInt int mentionModeColor) {
        this.mentionModeColor = mentionModeColor;
    }

    public void setHashtagModeColor(@ColorInt int hashtagModeColor) {
        this.hashtagModeColor = hashtagModeColor;
    }

    public void setUrlModeColor(@ColorInt int urlModeColor) {
        this.urlModeColor = urlModeColor;
    }

    public void setPhoneModeColor(@ColorInt int phoneModeColor) {
        this.phoneModeColor = phoneModeColor;
    }

    public void setEmailModeColor(@ColorInt int emailModeColor) {
        this.emailModeColor = emailModeColor;
    }

    public void setCustomModeColor(@ColorInt int customModeColor) {
        this.customModeColor = customModeColor;
    }

    public void setSelectedStateColor(@ColorInt int defaultSelectedColor) {
        this.defaultSelectedColor = defaultSelectedColor;
    }

    public void addAutoLinkMode(AutoLinkMode... autoLinkModes) {
        this.autoLinkModes = autoLinkModes;
    }

    public void setCustomRegex(String regex) {
        this.customRegex = regex;
    }

    public void setAutoLinkOnClickListener(AutoLinkOnClickListener autoLinkOnClickListener) {
        this.autoLinkOnClickListener = autoLinkOnClickListener;
    }

    public void enableUnderLine() {
        isUnderLineEnabled = true;
    }

    public void setRegularTextViewClick(RegularTextViewClick regularTextViewClick) {
        this.regularTextViewClick = regularTextViewClick;
    }

    public String[] getShortUrls() {
        return shortUrls;
    }

    public void setShortUrls(String[] shortUrls) {
        this.shortUrls = shortUrls;
    }
}

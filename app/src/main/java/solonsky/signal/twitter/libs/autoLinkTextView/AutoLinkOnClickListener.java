package solonsky.signal.twitter.libs.autoLinkTextView;

/**
 * Created by Chatikyan on 25.09.2016-21:24.
 */

public interface AutoLinkOnClickListener {

    void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText);
    void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText);
}
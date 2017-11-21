package solonsky.signal.twitter.libs.autoLinkTextView;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by Chatikyan on 26.09.2016-19:10.
 */

abstract class TouchableSpan extends ClickableSpan {

    public abstract void onLongClick(View widget);

    private boolean isPressed;
    private int normalTextColor;
    private int pressedTextColor;
    private final int pressedBackgroundColor;
    private boolean isUnderLineEnabled;

    TouchableSpan(int normalTextColor, int pressedTextColor, boolean isUnderLineEnabled, int pressedBackgroundColor) {
        this.normalTextColor = normalTextColor;
        this.pressedTextColor = pressedTextColor;
        this.isUnderLineEnabled = isUnderLineEnabled;
        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    void setPressed(boolean isSelected) {
        isPressed = isSelected;
    }
    public boolean isPressed() {
        return isPressed;
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
//        int textColor = isPressed ? pressedTextColor : normalTextColor;
        int textColor = normalTextColor;
        textPaint.setColor(textColor);
        textPaint.bgColor = isPressed ? pressedBackgroundColor : Color.TRANSPARENT;
        textPaint.setUnderlineText(isUnderLineEnabled);
    }
}

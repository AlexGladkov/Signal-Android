package solonsky.signal.twitter.libs.autoLinkTextView;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Chatikyan on 26.09.2016-19:09.
 */

class LinkTouchMovementMethod extends LinkMovementMethod {
    private final String TAG = LinkTouchMovementMethod.class.getSimpleName();
    private TouchableSpan pressedSpan;
    private AutoLinkTextView.RegularTextViewClick regularTextViewClick;

    private long thisTouchTime;
    private long previousTouchTime = 0;
    private long buttonHeldTime;
    private boolean longTouched;
    private float initialX = 0;
    private float initialY = 0;
    private boolean clickHandled = false;
    private final long DOUBLE_CLICK_INTERVAL = 200;
    private final long LONG_HOLD_TIMEOUT = 500;

    final float DELTA_THRESHOLD = 5;

    public LinkTouchMovementMethod(AutoLinkTextView.RegularTextViewClick regularTextViewClick) {
        this.regularTextViewClick = regularTextViewClick;
    }

    @Override
    public boolean onTouchEvent(final TextView textView, final Spannable spannable, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getRawX();
                initialY = event.getRawY();

                pressedSpan = getPressedSpan(textView, spannable, event);
                if (pressedSpan != null) {
                    pressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(pressedSpan),
                            spannable.getSpanEnd(pressedSpan));
                }

                thisTouchTime = System.currentTimeMillis();
                if (thisTouchTime - previousTouchTime <= DOUBLE_CLICK_INTERVAL) {
                    // Double click detected
                    clickHandled = true;
                    if (regularTextViewClick != null)
                        regularTextViewClick.onDoubleTapClicked(textView);
                } else {
                    // Defer event handling until later
                    clickHandled = false;
                }
                previousTouchTime = thisTouchTime;
                break;

            default:
                if (pressedSpan != null && pressedSpan.isPressed()) {
                    pressedSpan.setPressed(false);
                    pressedSpan = null;
                    Selection.removeSelection(spannable);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (pressedSpan != null && touchedSpan != pressedSpan) {
                    pressedSpan.setPressed(false);
                    pressedSpan = null;
                    Selection.removeSelection(spannable);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!clickHandled) {
                    buttonHeldTime = System.currentTimeMillis() - thisTouchTime;
                    if (buttonHeldTime > LONG_HOLD_TIMEOUT) {
                        clickHandled = true;
                        if (pressedSpan != null) {
                            pressedSpan.onLongClick(textView);
                            pressedSpan.setPressed(false);
                            pressedSpan = null;
                            Selection.removeSelection(spannable);
                        } else {
                            if (regularTextViewClick != null)
                                regularTextViewClick.onLongTextClicked(textView);
                        }
                    } else {
                        Handler clickHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (!clickHandled) {
                                    clickHandled = true;
                                    if (pressedSpan != null && pressedSpan.isPressed()) {
                                        pressedSpan.onClick(textView);
                                        pressedSpan.setPressed(false);
                                        pressedSpan = null;
                                        Selection.removeSelection(spannable);
                                    } else {
                                        if (regularTextViewClick != null)
                                            regularTextViewClick.onTextClicked(textView);
                                    }
                                }
                            }
                        };

                        Message m = new Message();
                        clickHandler.sendMessageDelayed(m, DOUBLE_CLICK_INTERVAL);
                    }
                }
                break;
        }

        return false;
    }

    private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int verticalLine = layout.getLineForVertical(y);
        int horizontalOffset = layout.getOffsetForHorizontal(verticalLine, x);

        TouchableSpan[] link = spannable.getSpans(horizontalOffset, horizontalOffset, TouchableSpan.class);
        TouchableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }
}

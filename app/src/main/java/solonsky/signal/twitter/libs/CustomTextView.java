package solonsky.signal.twitter.libs;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by kmoaz on 03.09.2017.
 */

public class CustomTextView extends androidx.appcompat.widget.AppCompatTextView {

    private Paint mTestPaint;
    private Handler measureHandler = new Handler();
    private Runnable requestLayout = new Runnable() {
        @Override
        public void run() {
            requestLayout();
        }
    };

    public CustomTextView(Context context) {
        super(context);
        initialise();
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise();
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //resize();
    }

    private void refitText(String text, int textWidth)
    {
        int maxwidth = Integer.MAX_VALUE == this.getMaxWidth() ? textWidth : this.getMaxWidth();

        if (textWidth <= 0) {
            return;
        }

        if (getLineCount() == 1) {
            return;
        }

        int targetWidth = maxwidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi + lo) / 2;
            mTestPaint.setTextSize(size);
            if (mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }

        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }*/

    private void resizeText () {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        //int height = getMeasuredHeight();
        //refitText(this.getText().toString(), parentWidth);
        //this.setMeasuredDimension(parentWidth, height);

        /*while (mTestPaint.measureText(getText().toString()) > getWidth() ) //maybe adjust for padding/margins etc
        {
            mTestPaint.setTextSize(getTextSize() - 1);
        }*/

        int a = getLineCount();
        if (a > 1) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() - 1);
            measureHandler.post(requestLayout);
        }


        /*final float maxWidth = getWidth();
        final float maxHeight = getHeight();
        if (maxWidth < 1.0f || maxHeight < 1.0f) {
            return;
        }

        int index = 0;
        int lineCount = 0;
        CharSequence text = getText();
        final TextPaint paint = getPaint();
        while (index < text.length()) {
            index += paint.breakText(text, index, text.length(), true, maxWidth, null);
            lineCount++;
        }
        final float height = lineCount * getLineHeight() + (lineCount > 0 ? (lineCount - 1) * paint.getFontSpacing() : 0);
        if (height > maxHeight) {
            final float textSize = getTextSize();
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize - 1);
            measureHandler.post(requestLayout);
        }*/
    }

    /*@Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }*/

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            //refitText(this.getText().toString(), w);
        }
    }

    private int getHeightOfMultiLineText(String text, float textSize) {
        Paint paint = new TextPaint();
        paint.setTextSize(textSize);
        int index = 0;
        int linecount = 0;

        while (index < text.length()) {
            index += paint.breakText(text, index, text.length(), true, getMaxWidth(), null);
            linecount++;
        }

        Rect bounds = new Rect();
        paint.getTextBounds("Yy", 0, 2, bounds);
        // obtain space between lines
        double lineSpacing = Math.max(0, (linecount - 1) * bounds.height() * 0.25);

        return (int) Math.floor(lineSpacing + linecount * bounds.height());
    }

    private void resize () {
        float textSize = getTextSize();
        int maxHeight = getHeight();
        int a = getLineCount();
        while(getHeightOfMultiLineText(getText().toString(), textSize) > maxHeight) {
            textSize--;
            setTextSize(textSize);
            invalidate();
            a = getLineCount();
        }
    }
}
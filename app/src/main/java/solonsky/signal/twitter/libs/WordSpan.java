package solonsky.signal.twitter.libs;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by neura on 31.05.17.
 */

public class WordSpan extends ClickableSpan {
    private int id;
    private int textColor;
    private Context context;
    private String text;
    private boolean marking = false;

    public WordSpan(int id, int textColor, String text, Context context) {
        this.id = id;
        this.textColor = textColor;
        this.context = context;
        this.text = text;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(context.getResources().getColor(textColor));
        ds.setUnderlineText(false);
        if (marking) {
            ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
    }

    @Override
    public void onClick(View widget) {

    }

    public void setMarking(boolean marking) {
        this.marking = marking;
    }

    public String getText() {
        return text;
    }
}

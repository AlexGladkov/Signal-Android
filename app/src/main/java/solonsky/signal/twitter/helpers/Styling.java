package solonsky.signal.twitter.helpers;

import android.content.Context;

import solonsky.signal.twitter.models.ConfigurationModel;

/**
 * Created by neura on 29.08.17.
 */

public class Styling {
    private Context mContext;
    private Flags.STYLE style;

    public Styling(Context mContext, Flags.STYLE style) {
        this.mContext = mContext;
        this.style = style;
    }

    public static Flags.STYLE convertFontToStyle(int text) {
        switch (text) {
            case ConfigurationModel.FONT_TINY:
                return Flags.STYLE.TINY;

            case ConfigurationModel.FONT_SMALL:
                return Flags.STYLE.SMALL;

            case ConfigurationModel.FONT_HUGE:
                return Flags.STYLE.HUGE;

            case ConfigurationModel.FONT_BIG:
                return Flags.STYLE.BIG;

            default:
                return Flags.STYLE.REGULAR;
        }
    }

    public int getCreatedAtSize() {
        switch (style) {
            case TINY:
                return 12;

            case SMALL:
                return 12;

            case REGULAR:
                return 12;

            case BIG:
                return 14;

            case HUGE:
                return 16;

            default: return 12;
        }
    }

    public int getSmallPreviewMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(5, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(6, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(6, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(7, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(8, mContext);

            default:
                return (int) Utilities.convertDpToPixel(6, mContext);
        }
    }

    /**
     * Gap between username and text
     * @return gap in pixels
     */
    public int getTextMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(1.75f, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(1.5f, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(1.25f, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(2.75f, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(4.5f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(2, mContext);
        }
    }

    public int getSquareAvatarMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(14, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(17, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(20, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(20, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(20.25f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(20, mContext);
        }
    }

    public int getQuoteMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(12.75f, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(14.5f, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(17, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(18, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(18.25f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(16, mContext);
        }
    }

    public int getBigImageMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(5, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(7, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(8, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(9.5f, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(12f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(8, mContext);
        }
    }

    public int getBaseMargin() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(12, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(14, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(16, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(16, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(16, mContext);

            default:
                return (int) Utilities.convertDpToPixel(16, mContext);
        }
    }

    public int getQuoteTextMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(1.5f, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(1.5f, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(1.5f, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(2f, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(2.75f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(1.5f, mContext);
        }
    }

    public float getQuoteTextExtra() {
        switch (style) {
            case TINY:
                return Utilities.convertDpToPixel(1.75f, mContext);

            case SMALL:
                return Utilities.convertDpToPixel(1.5f, mContext);

            case REGULAR:
                return Utilities.convertDpToPixel(1.5f, mContext);

            case BIG:
                return Utilities.convertDpToPixel(1.25f, mContext);

            case HUGE:
                return Utilities.convertDpToPixel(2.75f, mContext);

            default:
                return Utilities.convertDpToPixel(1.5f, mContext);
        }
    }

    public float getTextExtra() {
        switch (style) {
            case TINY:
                return Utilities.convertDpToPixel(1.75f, mContext);

            case SMALL:
                return Utilities.convertDpToPixel(1.5f, mContext);

            case REGULAR:
                return Utilities.convertDpToPixel(1.25f, mContext);

            case BIG:
                return Utilities.convertDpToPixel(2.75f, mContext);

            case HUGE:
                return Utilities.convertDpToPixel(4.5f, mContext);

            default:
                return Utilities.convertDpToPixel(1.25f, mContext);
        }
    }

    public int getRtMarginTop() {
        switch (style) {
            case TINY:
                return (int) Utilities.convertDpToPixel(6.75f, mContext);

            case SMALL:
                return (int) Utilities.convertDpToPixel(7.5f, mContext);

            case REGULAR:
                return (int) Utilities.convertDpToPixel(8, mContext);

            case BIG:
                return (int) Utilities.convertDpToPixel(9.75f, mContext);

            case HUGE:
                return (int) Utilities.convertDpToPixel(9.75f, mContext);

            default:
                return (int) Utilities.convertDpToPixel(8, mContext);
        }
    }

    public int getRtTextSize() {
        switch (style) {
            case TINY:
                return 12;

            case SMALL:
                return 14;

            case REGULAR:
                return 14;

            case BIG:
                return 16;

            case HUGE:
                return 18;

            default:
                return 14;
        }
    }

    public int getQuoteTextSize() {
        switch (style) {
            case TINY:
                return 12;

            case SMALL:
                return 14;

            case REGULAR:
                return 14;

            case BIG:
                return 16;

            case HUGE:
                return 18;

            default:
                return 14;
        }
    }

    /**
     * Get text size for style
     *
     * @return text size (dp)
     */
    public int getTextSize() {
        switch (style) {
            case TINY:
                return 12;

            case SMALL:
                return 14;

            case REGULAR:
                return 16;

            case BIG:
                return 18;

            case HUGE:
                return 20;

            default:
                return 16;
        }
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public Flags.STYLE getStyle() {
        return style;
    }

    public void setStyle(Flags.STYLE style) {
        this.style = style;
    }
}

package solonsky.signal.twitter.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Created by kmoaz on 29.08.2017.
 */

public class CirclePicasso implements com.squareup.picasso.Transformation {

    private float mRadius;
    private float mBorder;
    private int mAlpha;
    private int color;

    public CirclePicasso(final float mRadius, final float mBorder, final int mAlpha, int color) {
        this.mRadius = mRadius;
        this.mBorder = mBorder;
        this.mAlpha = mAlpha;
        this.color = color;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Rect rectImage = new Rect(0, 0, source.getWidth(), source.getHeight());
        final Paint paintImage = new Paint();
        final Paint paintBorder = new Paint();

        paintImage.setAntiAlias(true);
        paintImage.setFilterBitmap(true);
        paintImage.setDither(true);

        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(mBorder);
        paintBorder.setColor(color);
        paintBorder.setAlpha(mAlpha);

        paintBorder.setAntiAlias(true);
        paintBorder.setFilterBitmap(true);
        paintBorder.setDither(true);

        canvas.drawRoundRect(0, 0, source.getWidth(), source.getHeight(), mRadius, mRadius, paintImage);

        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, rectImage, rectImage, paintImage);

        paintBorder.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawRoundRect(0, 0, source.getWidth(), source.getHeight(), mRadius, mRadius, paintBorder);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "";
    }
}

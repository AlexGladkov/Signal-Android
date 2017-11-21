package solonsky.signal.twitter.draw;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

import solonsky.signal.twitter.helpers.App;

/**
 * Created by neura on 23.05.17.
 * Transfromation class for Picasso library to make image round
 */
public class CircleClassTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return null;
        }

        int borderWidth = App.getInstance().isNightEnabled() ? 0 : 20;
        int offset = App.getInstance().isNightEnabled() ? 0 : 20;

        final int width = source.getWidth() + offset;
        final int height = source.getHeight() + offset;

        Bitmap canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

//        PorterDuff.Mode mMode = PorterDuff.Mode.OVERLAY;
//        paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor(App.getInstance().isNightEnabled() ?
//            "#1FBEC8D2" : "#80FFFFFF"), mMode));

        Canvas canvas = new Canvas(canvasBitmap);
        float radius = width > height ? ((float) height) / 2f : ((float) width) / 2f;
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        //border code
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(App.getInstance().isNightEnabled() ? Color.parseColor("#00000000") :
                Color.parseColor("#1A000000"));
        paint.setStrokeWidth(borderWidth);
        canvas.drawCircle(width / 2, height / 2, radius - offset / 2, paint);
        //--------------------------------------

        if (canvasBitmap != source) {
            source.recycle();
        }

        return canvasBitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}

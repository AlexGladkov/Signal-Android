package solonsky.signal.twitter.draw;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

import java.util.Random;

/**
 * Created by neura on 13.07.17.
 */

public class ProfileClassTransform implements Transformation {
    private final int radius;
    private final int margin;  // dp
    private final String value;
    private final String TAG = ProfileClassTransform.class.getSimpleName();

    public ProfileClassTransform(int radius, int margin, String value) {
        this.radius = radius;
        this.margin = margin;
        this.value = value;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size/2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "profile" + new Random().nextInt() + new Random().nextDouble();
    }
}
